package com.escuelagaing.edu.co.dto;

import com.escuelagaing.edu.co.model.Card;
import com.escuelagaing.edu.co.model.Player;
import com.escuelagaing.edu.co.model.RoomStatus;
import com.escuelagaing.edu.co.model.Dealer;
import java.util.List;

public class RoomStateDTO {

    private String roomId;
    private String status; // Estado de la sala
    private int maxPlayers;
    private int currentPlayers;
    private List<PlayerStateDTO> players;
    private List<String> winners;
    private List<Card> dealerHand;

    public RoomStateDTO(String roomId, RoomStatus status, int maxPlayers, List<Player> players, List<Player> winners, List<Card> dealerHand) {
        this.roomId = roomId;
        this.status = (status != null) ? status.name() : "DESCONOCIDO";
        this.maxPlayers = maxPlayers;
        this.currentPlayers = (players != null) ? players.size() : 0;
    
        // Procesar players
        this.players = (players != null) 
            ? players.stream().map(PlayerStateDTO::new).toList() 
            : List.of();
    
        // Procesar winners
        if (winners != null) {
            this.winners = winners.stream()
                .map(player -> player instanceof Dealer 
                    ? "Dealer" 
                    : player.getUser().getNickName())
                .toList();
        } else {
            this.winners = List.of();
        }
    
        // Procesar dealerHand
        this.dealerHand = (dealerHand != null) ? dealerHand : List.of();
    }
    
    

    public String getRoomId() {
        return roomId;
    }

    public String getStatus() {
        return status;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public List<PlayerStateDTO> getPlayers() {
        return players;
    }

    public List<String> getWinners() {
        return winners;
    }

    public List<Card> getDealerHand() {
        return dealerHand;
    }
}
