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

    @Test
    void testFullGameFlow() {
        List<Player> players = createPlayers();
        Game game = new Game(players, "room1");

        game.startGame();

        assertTrue(game.isActive(), "El juego debería estar activo");

        for (Player player : players) {
            assertEquals(2, player.getHand().size(), "El jugador debería tener 2 cartas");
        }
        assertEquals(2, game.getDealer().getHand().size(), "El dealer debería tener 2 cartas");

        for (Player player : players) {
            game.startPlayerTurn(player, "HIT");
            game.startPlayerTurn(player, "STAND");
        }

        game.dealerTurn();

        game.endGame();

        assertFalse(game.isActive(), "El juego debería estar inactivo");
    }

    @Test
    void testDealerTurn() {
        List<Player> players = createPlayers();
        Game game = new Game(players, "room1");

        game.startGame();

        game.getDealer().addCard(new Card("Corazones", "5", 5));
        game.getDealer().addCard(new Card("Picas", "6", 6));
        game.dealerTurn();

        assertTrue(game.getDealer().calculateScore() >= 17, "El dealer debería tener una puntuación de al menos 17");

        assertTrue(game.getDealer().getHand().size() > 2, "El dealer debería haber tomado más cartas");
    }

    @Test
    void testDeletePlayer() {
        List<Player> players = createPlayers();
        Game game = new Game(players, "room1");

        assertEquals(2, game.getPlayers().size(), "Debe haber 2 jugadores al inicio");

        game.deletePlayer("1");

        assertEquals(1, game.getPlayers().size(), "Debe quedar 1 jugador después de eliminar uno");
        assertNull(game.getPlayers().stream().filter(player -> player.getId().equals("1")).findFirst().orElse(null), "El jugador 1 debe ser eliminado");
    }

    @Test
    void testAllPlayersBust() {
        List<Player> players = createPlayers();
        Game game = new Game(players, "room1");

        game.startGame();

        Player player1 = game.getPlayers().get(0);
        player1.addCard(new Card("Corazones", "10", 10));
        player1.addCard(new Card("Picas", "10", 10));

        Player player2 = game.getPlayers().get(1);
        player2.addCard(new Card("Diamantes", "5", 5));
        player2.addCard(new Card("Tréboles", "6", 6));

        game.dealerTurn();

        game.endGame();


        List<Player> winners = game.calculateWinners();
        assertEquals(1, winners.size(), "El dealer debería ser el único ganador");
        assertEquals(game.getDealer(), winners.get(0), "El dealer debe ganar si todos los jugadores se pasaron de 21");
    }

}