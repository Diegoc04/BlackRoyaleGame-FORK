package com.escuelagaing.edu.co.model;

import com.escuelagaing.edu.co.dto.RoomStateDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.annotation.Transient;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Document(collection = "Room") 

public class Room {

    private static final Logger logger = LoggerFactory.getLogger(Room.class);

    @Id
    private String id;
       
    @Transient
    private List<Player> players;
       
    @Transient
    private Game game;
       
    @Transient
    private RoomStatus status;  
       
    @Transient
    private int maxPlayers = 5;
       
    @Transient
    private int minPlayers = 2;
    
   
    // Constructor
    public Room() {
        this.players = Collections.synchronizedList(new ArrayList<>());

        this.status = RoomStatus.EN_ESPERA;  
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public RoomStateDTO buildRoomState() {
        List<Player> winners = (game != null && !game.isActive()) ? game.calculateWinners() : List.of();
        List<Card> dealerHand = (game != null) ? game.getDealer().getHand() : List.of();
        return new RoomStateDTO(id, status, maxPlayers, players, winners, dealerHand);
    }

    public String getRoomId() {  
        return id;
    }

    public void setId(String id) {
        this.id = id; 
    }

    public synchronized List<Player> getPlayers() {
        return new ArrayList<>(players); 
    }

    public synchronized void setPlayers(List<Player> players) {
        this.players = players;
    }
    

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public synchronized boolean addPlayer(Player player) {
        if (players.size() < maxPlayers) {
            players.add(player);
            if (players.size() == minPlayers) {
                status = RoomStatus.EN_APUESTAS;
                startBetting();
            }
            return true;
        }
        return false;
    }


    public Player getPlayerById(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                return player;
            }
        }
        return null; 
    }

    public synchronized boolean removePlayer(Player player) {

        if (players.contains(player)) {
            players.remove(player);
            game.deletePlayer(player.getId());
        }
        return false;
    }

    public void startGame() {
        if (players.size() >= minPlayers) {
            this.game = new Game(players, id);
            this.status = RoomStatus.EN_JUEGO;
            game.startGame();
        }
    }

    public void endGame() {
        if (game != null) {
            game.endGame();
            this.status = RoomStatus.FINALIZADO;
        }
    }

    public void startBetting() {
        this.status = RoomStatus.EN_APUESTAS;
    }
    

    public void endBetting() {
        logger.info("Fase de apuestas finalizada."); // Usar el logger aquí
        this.status = RoomStatus.EN_JUEGO;
        startGame(); // Iniciar el juego después de la fase de apuestas
    }

    public void resetRoom() {
        if (game != null) {
            game.resetGame();  
            players.clear(); 
            game = new Game(players, id);
        }
        this.status = RoomStatus.EN_ESPERA;
    }

    // Método para saber si la sala está llena
    public boolean isFull() {
        return players.size() == maxPlayers;
    }

    public Player getPlayerByName(String playerName) {
        for (Player player : players) {
            if (player.getName().equals(playerName)) {
                return player; // Retornar el jugador si se encuentra el nombre
            }
        }
        return null; // Retornar null si no se encuentra el jugador con el nombre dado
    }
    
    public int getMinPlayers() {
        return minPlayers;
    }
}