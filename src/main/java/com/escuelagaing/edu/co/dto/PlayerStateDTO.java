
package com.escuelagaing.edu.co.dto;

import com.escuelagaing.edu.co.model.Card;
import com.escuelagaing.edu.co.model.Chip;
import com.escuelagaing.edu.co.model.Player;

import java.util.List;

import org.springframework.data.annotation.PersistenceConstructor;

public class PlayerStateDTO {

    private String nickName;
    private double amount;
    private List<Card> hand;
    private List<Chip> chips;
    private double bet;
    private boolean isInTurn;

    @SuppressWarnings("deprecation")
    @PersistenceConstructor
    public PlayerStateDTO(String nickName, double amount, List<Card> hand, List<Chip> chips, double bet, boolean isInTurn) {
        this.nickName = nickName;
        this.amount = amount;
        this.hand = hand;
        this.chips = chips;
        this.bet = bet;
        this.isInTurn = isInTurn;
    }

    public PlayerStateDTO(Player player) {
        this.nickName = player.getUser().getNickName();
        this.amount = player.getUser().getAmount();
        this.bet = (player.getBet() != 0) ? player.getBet() : 0.0;
        this.hand = (player.getHand() != null) ? player.getHand() : List.of();
        this.chips = (player.getChips() != null) ? player.getChips() : List.of();
        this.isInTurn = player.getInTurn(); 
    }

    public static Player toPlayer(PlayerStateDTO dto) {
        Player player = new Player();
        player.setNickName(dto.getNickName());
        player.setAmount(dto.getAmount());
        player.setBet(dto.getBet());
        player.setHand(dto.getHand());
        player.setChips(dto.getChips());
        player.setInTurn(dto.isInTurn());
        return player;
    }
    

    public String getNickName() {
        return nickName;
    }

    public double getAmount() {
        return amount;
    }

    public double getBet() {
        return bet;
    }

    public List<Card> getHand() {
        return hand;
    }

    public List<Chip> getChips() {
        return chips;
    }

    public boolean isInTurn() {
        return isInTurn;
    }
}
