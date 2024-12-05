package com.escuelagaing.edu.co.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.data.annotation.Transient;

public class Game {
    private static final String JUGADOR_PREFIX = "Jugador ";
    private static final Logger logger = LoggerFactory.getLogger(Game.class);

    private List<Player> players;
    private Dealer dealer;
    private boolean isActive;
    private List<Player> winners;
    private int currentPlayerIndex;
    private static final String SCORE_TEXT = " - Puntuación: ";  

   
    @Transient
    private Deck deck;

    @Transient
    private CyclicBarrier barrier;

    public Game(List<Player> players, String roomId) {
        this.players = players;
        this.winners = new ArrayList<>();
        this.dealer = new Dealer(roomId);
        this.isActive = true;
        this.currentPlayerIndex = 0;
        this.barrier = new CyclicBarrier(players.size() + 1); 
  
    }



   
    public Dealer getDealer() {
        return dealer;
    }

    // FASE 3: Fase de Reparto de Cartas Iniciales
    public void startGame() {
        this.isActive = true;
        this.deck = new Deck(); // Crear un nuevo mazo
        this.deck.shuffle(); // Barajar el mazo
        resetHands();
        dealInitialCards();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void dealInitialCards() {
        logger.info("Repartiendo cartas iniciales...");
        for (Player player : players) {
            player.addCard(deck.drawCard());
            player.addCard(deck.drawCard());
            logger.info("{}{} - Cartas: {}{}{}", 
                    JUGADOR_PREFIX, player.getName(), player.getHand(), SCORE_TEXT, player.calculateScore());
        }
        dealer.addCard(deck.drawCard());
        dealer.addCard(deck.drawCard());
        logger.info("Dealer - Cartas: {}{}{}", dealer.getHand(), SCORE_TEXT, dealer.calculateScore());
        startPhaseTurns();
    }

 
    public void startPhaseTurns() {
        if (!players.isEmpty()) {
            Player firstPlayer = players.get(0);
            int attempts = players.size();
    
            while (firstPlayer.isDisconnected() && attempts > 0) {
                nextPlayer();
                firstPlayer = players.get(currentPlayerIndex);
                attempts--;
            }
    
            if (!firstPlayer.isDisconnected()) {
                firstPlayer.setInTurn(true);
                logger.info("Iniciando turno de {}", firstPlayer.getNickName());
            } else {
                logger.info("Todos los jugadores están desconectados. Finalizando el juego.");
                dealerTurn();
            }
        }
    }
    

    public void startPlayerTurn(Player player, String actionString) {
        if (player.isDisconnected()) {
            nextPlayer();
            return;
        }
        PlayerAction action;
        try {
            action = PlayerAction.valueOf(actionString.toUpperCase()); 
        } catch (IllegalArgumentException e) {
            logger.error("Acción no válida recibida: " + actionString, e);
            return; 
        }
    
        logger.info("Turno de: {} con acción: {}", player.getNickName(), action);
        try {
            decideAction(player, action); 
        } catch (Exception e) {
            logger.error("Error en el turno del jugador: " + player.getName(), e);
        } finally {
            if (player.isFinishTurn()) {
                player.setInTurn(false);
                nextPlayer(); // Llama a nextPlayer() para manejar correctamente la transición
            }
        }
    }
    


public void decideAction(Player player, PlayerAction action) {
    switch (action) {
        case HIT:
        player.addCard(deck.drawCard());
        int currentScore = player.calculateScore();
        logger.info("{} realizó HIT - Puntuación actual: {}", JUGADOR_PREFIX + player.getName(), currentScore);
        if (currentScore > 21) {
            logger.info("{} se pasó de 21. Fin de turno.", JUGADOR_PREFIX + player.getName());
            player.setfinishTurn(true);
            player.setInTurn(false);
        }
        break;
        case STAND:
            player.setfinishTurn(true);
            player.setInTurn(false);
            break;
        case DOUBLE:
            double additionalBet = player.getBet(); 
            player.placeBet(additionalBet); 
            player.addCard(deck.drawCard());
            List<Chip> currentChips = player.getChips();
            List<Chip> doubledChips = new ArrayList<>(currentChips);
            doubledChips.addAll(currentChips);  
            player.setChips(doubledChips); 
            
            player.setfinishTurn(true);
            player.setInTurn(false);
            break;
        default:
            player.setfinishTurn(true);
            player.setInTurn(false);
    }
}
    




    public void dealerTurn() {
        while (dealer.calculateScore() < 17) {
            dealer.addCard(deck.drawCard());
        }
    
        endGame();


    }

    public void resetGame() {
        for (Player player : players) {
            player.getHand().clear();
            player.setBet(0);
            player.setfinishTurn(false);
        }
        dealer.getHand().clear();
        this.currentPlayerIndex = 0;
        this.winners.clear();
        this.isActive = true;
    }

    // FASE 6: Fin del Juego y Distribución de Ganancias
    public void endGame() {
        this.isActive = false;
        deliverProfit(); 
    }

    public void deliverProfit() {
        winners = calculateWinners();
        for (Player player : players) {
            player.revenue(winners.contains(player)); // Simplificación del if-then-else
        }
    }
    

    public void deletePlayer(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId)) {
                players.remove(player); // Elimina al jugador encontrado
                return; 
            }
        }
    }
    

    
    public List<Player> calculateWinners() {
        int dealerScore = dealer.calculateScore();
        int highestScore = 0;
    
       
        winners.clear();
    
        logger.info("Puntuación del Dealer: {}", dealerScore);
    
        for (Player player : players) {
            int playerScore = player.calculateScore();
            logger.info("{} - Cartas: {}{}", player.getName(), player.getHand(), SCORE_TEXT);
    
            if (playerScore > 21) {
                logger.info("{} se pasó de 21.", JUGADOR_PREFIX + player.getName());
                continue; 
            }
    
            if (playerScore > dealerScore || dealerScore > 21) {
                if (playerScore > highestScore) {
                    highestScore = playerScore;
                    winners.clear(); 
                    winners.add(player);
                } else if (playerScore == highestScore) {
                    winners.add(player);
                }
            }
        }
        
        if (winners.isEmpty()) {
            logger.info("El Dealer gana con una puntuación de: {}", dealerScore);
            winners.add(dealer); 
        }

        return winners;
    }
    public Card dealCard() {
        Player currentPlayer = getCurrentPlayer();
        Card newCard = this.deck.drawCard();
        currentPlayer.addCard(newCard);
        return newCard;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextPlayer() {
        if (players.isEmpty()) {
            logger.info("No hay jugadores en la sala.");
            return;
        }
    
        int attempts = players.size(); 
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            attempts--;
        } while ((players.get(currentPlayerIndex).isDisconnected() || players.get(currentPlayerIndex).isFinishTurn()) && attempts > 0);
    
        if (attempts > 0) { 
            Player nextPlayer = players.get(currentPlayerIndex);
            nextPlayer.setInTurn(true);
            logger.info("Turno pasado a: {}", nextPlayer.getNickName());
        } else { 
            logger.info("Todos los jugadores han terminado su turno o están desconectados. Finalizando el juego.");
            dealerTurn();
        }
    }
    
    
    

    public void resetPlayerTurns() {
        for (Player player : players) {
            player.setfinishTurn(false);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void resetHands() {
        for (Player player : players) {
            player.resetHand();
            player.setfinishTurn(false);
        }
        dealer.resetHand();
    }

    public Deck getDeck() {
        return deck;
    }
}