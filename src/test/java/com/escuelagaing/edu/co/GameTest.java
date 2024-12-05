package com.escuelagaing.edu.co;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import com.escuelagaing.edu.co.model.*;

class GameTest {

    @Test
    void testStartGame() {
        // Crear jugadores
        List<Player> players = createPlayers();
        
        // Crear la instancia del juego
        Game game = new Game(players, "room1");
        
        // Iniciar el juego
        game.startGame();
        
        // Verificar que el juego esté activo
        assertTrue(game.isActive(), "El juego debería estar activo");
        
        // Verificar que las manos de los jugadores tengan 2 cartas
        assertEquals(2, players.get(0).getHand().size(), "El jugador 1 debería tener 2 cartas");
        assertEquals(2, players.get(1).getHand().size(), "El jugador 2 debería tener 2 cartas");
        
        // Verificar que el dealer tenga 2 cartas
        assertEquals(2, game.getDealer().getHand().size(), "El dealer debería tener 2 cartas");
    }



    private List<Player> createPlayers() {
        List<Player> players = new ArrayList<>();
        User user1 = new User("1", "user1@example.com", "Player1");
        User user2 = new User("2", "user2@example.com", "Player2");
        Player player1 = new Player(user1, "pepe",  "room1", 10000);
        Player player2 = new Player(user2, "marco","room1", 10000);
        players.add(player1);
        players.add(player2);
        ArrayList<Chip> chipsPlayer1 = new ArrayList<>();
        chipsPlayer1.add(Chip.AMARILLO);
        chipsPlayer1.add(Chip.AZUL);
        chipsPlayer1.add(Chip.ROJO);

        player1.setChips(chipsPlayer1);

        ArrayList<Chip> chipsPlayer2 = new ArrayList<>();
        chipsPlayer2.add(Chip.VERDE);
        chipsPlayer2.add(Chip.NEGRO);

        player2.setChips(chipsPlayer2);
        return players;
    }

    void simulateGameEnd(Game game) {
        Player player1 = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        
        player1.addCard(new Card("Corazones", "10", 10));
        player1.addCard(new Card("Picas", "10", 10));
        
        player2.addCard(new Card("Diamantes", "5", 5));
        player2.addCard(new Card("Tréboles", "6", 6));
        
        game.getDealer().addCard(new Card("Corazones", "9", 9));
        game.getDealer().addCard(new Card("Picas", "10", 10));
        
        game.endGame();
    }
}