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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BlackJackSocketIOConfig {
    private static final Logger logger = LoggerFactory.getLogger(BlackJackSocketIOConfig.class);
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
                logger.error("Conexión rechazada: Player ID no proporcionado.");
                client.disconnect();
                return;
            }
    
            // Si ya existe una conexión para este jugador, desconectar la anterior
            socketToPlayerId.values().removeIf(existingPlayerId -> existingPlayerId.equals(playerId));
    
            logger.info("Player connected! Player ID: {}", playerId);
            socketToPlayerId.put(client.getSessionId().toString(), playerId);
    
            // Inicializar salas si no están cargadas
            if (rooms.isEmpty()) {
                logger.info("Inicializando salas desde RoomService en la conexión.");
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
        logger.info("Salas inicializadas desde RoomService: {}", rooms.keySet());
    } else {
        logger.info("No hay salas disponibles para inicializar.");
    }
}

private void sendRoomsUpdateToClient(SocketIOClient client) {
    List<RoomStateDTO> allRoomsState = rooms.values().stream()
            .map(Room::buildRoomState)
            .collect(Collectors.toList());

    client.sendEvent("roomsUpdate", allRoomsState);
    logger.info("Salas enviadas al cliente: {}", allRoomsState);
}

    


private DisconnectListener onDisconnected() {
    return client -> {
        String sessionId = client.getSessionId().toString();
        String playerId = socketToPlayerId.remove(sessionId);
        String roomId = socketToRoomId.remove(sessionId);

        if (playerId == null || roomId == null) {
            logger.error("No se encontró información para el session ID: {}", sessionId);
            return;
        }

        Room room = rooms.get(roomId);
        if (room != null) {
            Player player = room.getPlayerById(playerId);

            if (player != null) {
                player.setDisconnected(true);
                logger.info("Player {} marked as disconnected.", player.getId());

                if (room.getStatus() != RoomStatus.EN_JUEGO) {
                    logger.info("Eliminando jugador desconectado durante la fase {}: {}", room.getStatus(), playerId);
                    room.removePlayer(player);

                } else {
                    // Si está en juego
                    if (player.getInTurn()) {
                        logger.info("Player was in turn. Passing to next player.");
                        player.setInTurn(false);
                        room.getGame().nextPlayer();
                    } 
                }

                // Si después de eliminar, la sala queda vacía
                if (room.getPlayers().isEmpty()) {
                    room.resetRoom();
                    logger.info("{}{} reiniciada y puesta en estado EN_ESPERA.", SALA_PREFIX, roomId);
                }

                sendRoomUpdate(roomId);
            } else {
                logger.error("Jugador no encontrado en la sala. Player ID: {}", playerId);
            }
        } else {
            logger.error("Sala no encontrada para Room ID: {}", roomId);
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
            logger.info("El jugador ya está en la sala, no se puede unir de nuevo.");
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

        logger.info("Player: {} con ID: {} se unió a la sala {}", playerName, playerId, roomId);
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

        logger.info("Jugador creado: ID={}, Nombre={}, RoomId={}, Saldo={}", playerId, playerName, roomId, player.getAmount());

        if (room.addPlayer(player)) {
            sendRoomUpdate(roomId);
            logger.info("Enviando actualización de la sala después de que el jugador se una");
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

        logger.info("Mensaje retransmitido en sala {} por {}: {}", roomId, sender, message.getMessage());
    };
}


private DataListener<String> leaveRoomListener() {
    return (client, roomId, ackSender) -> {
        logger.info("Client left Blackjack room: {}", roomId);
        client.leaveRoom(roomId); // El cliente abandona la sala.

        Room room = rooms.get(roomId); // Obtén la sala del mapa.
        if (room != null) {
            // Identifica al jugador basado en el mapeo o parámetros del socket.
            String playerName = client.getHandshakeData().getSingleUrlParam("name");
            Player player = room.getPlayerByName(playerName);

            if (player != null) {
                room.removePlayer(player); // Remueve al jugador de la sala.
                logger.info("Jugador {} eliminado de la sala {}", playerName, roomId);
            }

            // Si la sala queda vacía, reiníciala para que esté disponible nuevamente.
            if (room.getPlayers().isEmpty()) {
                room.resetRoom();
                logger.info("{}{} reiniciada y puesta en estado EN_ESPERA.", SALA_PREFIX, roomId);
            } else if (room.getPlayers().size() < room.getMinPlayers() && room.getStatus() == RoomStatus.EN_JUEGO) {
                room.setStatus(RoomStatus.FINALIZADO); // Cambiar estado si no hay jugadores suficientes.
                logger.info("{}{} marcada como FINALIZADA.", SALA_PREFIX, roomId);
            }

            sendRoomUpdate(roomId); // Notifica a los clientes sobre el cambio en la sala.
        }
    };
}


    private DataListener<BetPayload> playerBetListener() {
        return (client, data, ackSender) -> {
            String roomId = data.getRoomId();
            List<String> chipColors = data.getFichas();

            logger.info("Evento playerBet recibido con las fichas: {}", chipColors);
            logger.info("roomId recibido: {}", roomId);

            String playerId = socketToPlayerId.get(client.getSessionId().toString());

            Room room = rooms.get(roomId);
            if (room != null && room.getStatus() == RoomStatus.EN_APUESTAS) {
                Player player = room.getPlayerById(playerId);
                if (player != null) {
                    if (player.placeBet(chipColors)) {
                        player.setHasCompletedBet(true);
                        logger.info("Apuesta realizada por {}: {}", player.getName(), player.getBet());
                        logger.info("Saldo después de apuesta: {}", player.getAmount());
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
            
            logger.info("Acción del jugador recibida: {} en la sala {}", actionType, roomId);


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
                logger.debug("JSON enviado a roomUpdate: {}", jsonString);
            } catch (Exception e) {
                logger.error("Error al convertir RoomStateDTO a JSON: {}", e.getMessage(), e);
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
        logger.info("Broadcast de salas actualizado: {}", allRoomsState);
    }
    
    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }
}
