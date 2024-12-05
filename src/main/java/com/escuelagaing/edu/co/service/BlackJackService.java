package com.escuelagaing.edu.co.service;

import com.escuelagaing.edu.co.model.Game;
import com.escuelagaing.edu.co.model.Player;
import com.escuelagaing.edu.co.model.Room;
import com.escuelagaing.edu.co.repository.BlackJackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class BlackJackService {
    private static final Logger logger = LoggerFactory.getLogger(BlackJackService.class);

    private final BlackJackRepository roomRepository;
   

    // Constructor injection
    @Autowired
    public BlackJackService(BlackJackRepository roomRepository ) {
        this.roomRepository = roomRepository;
    
    }

    private String generateUniqueRoomId() {
        return "room-" + System.currentTimeMillis();
    }

    public Room createRoom() {
        Room room = new Room();
        room.setId(generateUniqueRoomId());
        return roomRepository.save(room); // Guardar en MongoDB
       
    }

    public Room getRoom(String roomId) {
        return roomRepository.findById(roomId).orElse(null); // Recuperar de MongoDB
    }

    public void setRooms(Room room, String id) {
        room.setId(id);
        roomRepository.save(room); // Guardar o actualizar en MongoDB
    }

    public Player getPlayerById(String playerId) {
        for (Room room : roomRepository.findAll()) {
            Player player = room.getPlayerById(playerId);
            if (player != null) {
                return player;
            }
        }
        return null;
    }

    public void addPlayerToRoom(String roomId, Player player) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        Room room = optionalRoom.orElseGet(() -> {
            Room newRoom = new Room();
            newRoom.setId(roomId);
            return newRoom;
        });
        room.addPlayer(player);
        roomRepository.save(room); // Guardar en MongoDB
        logger.info("Rooms saved to MongoDB: {}", roomRepository.findAll());
    }

    public void startGame(Room room) {
        Game game = new Game(room.getPlayers(), room.getRoomId());
        room.setGame(game);
        game.startGame();
        roomRepository.save(room); // Guardar el estado actualizado en MongoDB
    }

  

    public void placeBet(Player player, Double betAmount) {
        Room room = getRoom(player.getRoomId());
        if (room != null && room.getGame() != null && player.placeBet(betAmount)) {
            roomRepository.save(room); // Guardar el estado actualizado en MongoDB
        }
    }

    public void endGame(Room room) {
        if (room.getGame() != null) {
            room.getGame().endGame();
            roomRepository.save(room); // Guardar el estado actualizado en MongoDB
        }
    }
}
