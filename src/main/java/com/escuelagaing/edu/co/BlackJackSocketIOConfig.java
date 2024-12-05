// BlackJackSocketIOConfig.java
package com.escuelagaing.edu.co;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.escuelagaing.edu.co.dto.*;
import com.escuelagaing.edu.co.model.*;
import com.escuelagaing.edu.co.service.RoomService;
import com.escuelagaing.edu.co.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BlackJackSocketIOConfig {
    private static final String SALA_PREFIX = "Sala ";
    private static final String ERROR_PREFIX = "error";
    private static final String BETERROR_PREFIX = "betError";
    private final SocketIOServer server;
    private final UserService userService;
    private final RoomService roomService;
    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, String> socketToPlayerId = new HashMap<>();
    private final Map<String, String> socketToRoomId = new HashMap<>();

    @Autowired
    public BlackJackSocketIOConfig(SocketIOServer server, UserService userService, RoomService roomService) {
        this.server = server;
        this.userService = userService;
        this.roomService = roomService;

        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("joinRoom", JoinRoomPayload.class, joinRoomListener());
        server.addEventListener("leaveRoom", String.class, leaveRoomListener());
        server.addEventListener("playerBet", BetPayload.class, playerBetListener());
        server.addEventListener("playerAction", PlayerActionPayload.class, playerActionListener());
        server.addEventListener("restartGame", RestartGamePayload.class, restartGameListener());
        server.addEventListener("sendMessage", ChatMessage.class, chatMessageListener());

    }

    private ConnectListener onConnected() {
        return client -> {
            String playerId = client.getHandshakeData().getSingleUrlParam("id");
    
            if (playerId == null || playerId.isEmpty()) {
                System.out.println("[ERROR] Conexión rechazada: Player ID no proporcionado.");
                client.disconnect();
                return;
            }
    
            // Si ya existe una conexión para este jugador, desconectar la anterior
            socketToPlayerId.values().removeIf(existingPlayerId -> existingPlayerId.equals(playerId));
    
            System.out.println("Player connected! Player ID: " + playerId);
            socketToPlayerId.put(client.getSessionId().toString(), playerId);
    
            // Inicializar salas si no están cargadas
            if (rooms.isEmpty()) {
                System.out.println("[INFO] Inicializando salas desde RoomService en la conexión.");
                initializeRooms();
            }
    
            // Enviar las salas disponibles al cliente que se conecta
            sendRoomsUpdateToClient(client);
        };
    }
    

private void initializeRooms() {
    List<Room> initialRooms = roomService.getAllRooms(); // Llamar al servicio para obtener las salas
    if (initialRooms != null && !initialRooms.isEmpty()) {
        for (Room room : initialRooms) {
            rooms.put(room.getRoomId(), room);
        }
        System.out.println("[INFO] Salas inicializadas desde RoomService: " + rooms.keySet());
    } else {
        System.out.println("[INFO] No hay salas disponibles para inicializar.");
    }
}

private void sendRoomsUpdateToClient(SocketIOClient client) {
    List<RoomStateDTO> allRoomsState = rooms.values().stream()
            .map(Room::buildRoomState)
            .collect(Collectors.toList());

    client.sendEvent("roomsUpdate", allRoomsState);
    System.out.println("Salas enviadas al cliente: " + allRoomsState);
}

    


private DisconnectListener onDisconnected() {
    return client -> {
        String sessionId = client.getSessionId().toString();
        String playerId = socketToPlayerId.remove(sessionId);
        String roomId = socketToRoomId.remove(sessionId);

        if (playerId == null || roomId == null) {
            System.out.println("[ERROR] No se encontró información para el session ID: " + sessionId);
            return;
        }

        Room room = rooms.get(roomId);
        if (room != null) {
            Player player = room.getPlayerById(playerId);

            if (player != null) {
                player.setDisconnected(true);
                System.out.println("Player " + player.getId() + " marked as disconnected.");

                if (room.getStatus() != RoomStatus.EN_JUEGO) {
                    System.out.println("Eliminando jugador desconectado durante la fase " + room.getStatus() + ": " + playerId);
                    room.removePlayer(player);

                } else {
                    // Si está en juego
                    if (player.getInTurn()) {
                        System.out.println("Player was in turn. Passing to next player.");
                        player.setInTurn(false);
                        room.getGame().nextPlayer();
                    } 
                }

                // Si después de eliminar, la sala queda vacía
                if (room.getPlayers().isEmpty()) {
                    room.resetRoom();
                    System.out.println(SALA_PREFIX + roomId + " reiniciada y puesta en estado EN_ESPERA.");
                }

                sendRoomUpdate(roomId);
            } else {
                System.err.println("[ERROR] Jugador no encontrado en la sala. Player ID: " + playerId);
            }
        } else {
            System.err.println("[ERROR] Sala no encontrada para Room ID: " + roomId);
        }
    };
}


    

private DataListener<JoinRoomPayload> joinRoomListener() {
    return (client, data, ackSender) -> {
        String playerId = data.getUserId();
        String roomId = data.getRoomId();

        if (playerId == null || roomId == null) {
            client.sendEvent(ERROR_PREFIX, "Datos insuficientes para unirse a la sala.");
            return;
        }

        // Verificar si el jugador ya está en la sala
        Room room = rooms.get(roomId);
        if (room != null && room.getPlayerById(playerId) != null) {
            System.out.println("El jugador ya está en la sala, no se puede unir de nuevo.");
            return;
        }

        // Obtener el jugador desde el servicio de usuarios
        Optional<User> userOptional = userService.getUserById(playerId);
        if (!userOptional.isPresent()) {
            client.sendEvent(ERROR_PREFIX, "Usuario no encontrado con ID: " + playerId);
            return;
        }

        User user = userOptional.get();
        String playerName = user.getNickName();

        System.out.println("Player: " + playerName + " con ID: " + playerId + " se unió a la sala " + roomId);
        client.joinRoom(roomId);

        // Actualizar el mapeo socketToRoomId
        socketToRoomId.put(client.getSessionId().toString(), roomId);

        if (room == null) {
            room = new Room();
            room.setId(roomId);
            rooms.put(roomId, room);
        }

        if (room.getStatus() == RoomStatus.EN_JUEGO) {
            client.sendEvent(ERROR_PREFIX, "No puedes unirte. El juego ya ha comenzado.");
            return;
        } else if (room.getStatus() == RoomStatus.FINALIZADO) {
            client.sendEvent(ERROR_PREFIX, "El juego ha finalizado.");
            return;
        }

        Player player = new Player(user, playerName, roomId, user.getAmount());

        System.out.println("Jugador creado: ID=" + playerId + ", Nombre=" + playerName + ", RoomId=" + roomId + ", Saldo=" + player.getAmount());

        if (room.addPlayer(player)) {
            sendRoomUpdate(roomId);
            System.out.println("Enviando actualización de la sala después de que el jugador se una");
        } else {
            client.sendEvent(ERROR_PREFIX, "La sala ya está llena o no se puede unir.");
        }
    };
}



private DataListener<ChatMessage> chatMessageListener() {
    return (client, message, ackSender) -> {
        String roomId = message.getRoomId(); // ID de la sala del mensaje
        String sender = message.getSender(); // Nombre del remitente

        // Verificar que la sala existe
        Room room = rooms.get(roomId);
        if (room == null) {
            client.sendEvent(ERROR_PREFIX, "La sala no existe.");
            return;
        }

        // Construir el mensaje de chat y retransmitirlo a todos en la sala
        ChatMessage chatMessage = new ChatMessage(sender, message.getMessage(), roomId);
        server.getRoomOperations(roomId).sendEvent("receiveMessage", chatMessage);

        System.out.println("Mensaje retransmitido en sala " + roomId + " por " + sender + ": " + message.getMessage());
    };
}


private DataListener<String> leaveRoomListener() {
    return (client, roomId, ackSender) -> {
        System.out.println("Client left Blackjack room: " + roomId);
        client.leaveRoom(roomId); // El cliente abandona la sala.

        Room room = rooms.get(roomId); // Obtén la sala del mapa.
        if (room != null) {
            // Identifica al jugador basado en el mapeo o parámetros del socket.
            String playerName = client.getHandshakeData().getSingleUrlParam("name");
            Player player = room.getPlayerByName(playerName);

            if (player != null) {
                room.removePlayer(player); // Remueve al jugador de la sala.
                System.out.println("Jugador " + playerName + " eliminado de la sala " + roomId);
            }

            // Si la sala queda vacía, reiníciala para que esté disponible nuevamente.
            if (room.getPlayers().isEmpty()) {
                room.resetRoom();
                System.out.println(SALA_PREFIX + roomId + " reiniciada y puesta en estado EN_ESPERA.");
            } else if (room.getPlayers().size() < room.getMinPlayers() && room.getStatus() == RoomStatus.EN_JUEGO) {
                room.setStatus(RoomStatus.FINALIZADO); // Cambiar estado si no hay jugadores suficientes.
                System.out.println(SALA_PREFIX + roomId + " marcada como FINALIZADA.");
            }

            sendRoomUpdate(roomId); // Notifica a los clientes sobre el cambio en la sala.
        }
    };
}


    private DataListener<BetPayload> playerBetListener() {
        return (client, data, ackSender) -> {
            String roomId = data.getRoomId();
            List<String> chipColors = data.getFichas();

            System.out.println("Evento playerBet recibido con las fichas: " + chipColors);
            System.out.println("roomId recibido: " + roomId);

            String playerId = socketToPlayerId.get(client.getSessionId().toString());

            Room room = rooms.get(roomId);
            if (room != null && room.getStatus() == RoomStatus.EN_APUESTAS) {
                Player player = room.getPlayerById(playerId);
                if (player != null) {
                    if (player.placeBet(chipColors)) {
                        player.setHasCompletedBet(true);
                        System.out.println("Apuesta realizada por " + player.getName() + ": " + player.getBet());
                        System.out.println("Saldo después de apuesta: " + player.getAmount());
                        sendRoomUpdate(roomId);
                        boolean allPlayersCompletedBet = room.getPlayers().stream().allMatch(Player::hasCompletedBet);
                        if (allPlayersCompletedBet) {
                            room.endBetting();
                            sendRoomUpdate(roomId);
                        }

                    } else {
                        client.sendEvent(BETERROR_PREFIX, "Saldo insuficiente o color de ficha no válido para esta apuesta.");
                    }
                } else {
                    client.sendEvent(BETERROR_PREFIX, "Jugador no encontrado en la sala.");
                }
            } else {
                client.sendEvent(BETERROR_PREFIX, "No se puede realizar la apuesta en este momento.");
            }
        };
    }

    private DataListener<PlayerActionPayload> playerActionListener() {
        return (client, data, ackSender) -> {
            String roomId = data.getRoomId();
            String actionType = data.getType();
            String playerId = socketToPlayerId.get(client.getSessionId().toString());
            
            System.out.println("Acción del jugador recibida: " + actionType + " en la sala " + roomId);

            Room room = rooms.get(roomId);
            if (room != null && room.getStatus() == RoomStatus.EN_JUEGO) {
                Player player = room.getPlayerById(playerId);
                if (player != null && player.getInTurn()) {
                    Game game = room.getGame();
                    game.startPlayerTurn(player, actionType);
                    if (!room.getGame().isActive()) {
                        RoomStateDTO gameState = room.buildRoomState();
                        saveGameStateForPlayers(gameState, room.getPlayers());
                    }
                    sendRoomUpdate(roomId);
                } else {
                    client.sendEvent("actionError", "No es el turno del jugador o el jugador no existe en la sala.");
                }
            } else {
                client.sendEvent("actionError", "La sala no está en el estado de juego.");
            }
        };
    }

    private DataListener<RestartGamePayload> restartGameListener() {
        return (client, data, ackSender) -> {
            String roomId = data.getRoomId();
            Room room = rooms.get(roomId);
            if (room != null) {
                room.resetRoom();
                sendRoomUpdate(roomId);
            } else {
                client.sendEvent("restartError", "Sala no encontrada o inválida.");
            }
        };
    }

    private void saveGameStateForPlayers(RoomStateDTO gameState, List<Player> players) {
        if (gameState != null) {
            for (Player player : players) {
                String userId = player.getUser().getEmail();
                userService.saveGameToUserHistory(userId, gameState);
                userService.updateUser(userId, player.getUser());
            }
        }
    }

    public void sendRoomUpdate(String roomId) {
        Room room = rooms.get(roomId);
        if (room != null) {
            RoomStateDTO roomState = room.buildRoomState();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(roomState);
                System.out.println("JSON enviado a roomUpdate: " + jsonString);
            } catch (Exception e) {
                System.err.println("Error al convertir RoomStateDTO a JSON: " + e.getMessage());
            }
            server.getRoomOperations(roomId).sendEvent("roomUpdate", roomState);
        }
        sendRoomsUpdate();
    }

    public void sendRoomsUpdate() {
        List<RoomStateDTO> allRoomsState = rooms.values().stream()
                .map(Room::buildRoomState)
                .collect(Collectors.toList());
    
        server.getBroadcastOperations().sendEvent("roomsUpdate", allRoomsState);
        System.out.println("Broadcast de salas actualizado: " + allRoomsState);
    }
    
    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }
}
