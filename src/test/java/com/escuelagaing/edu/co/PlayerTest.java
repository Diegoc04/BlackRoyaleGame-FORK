package com.escuelagaing.edu.co;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.escuelagaing.edu.co.model.Card;
import com.escuelagaing.edu.co.model.Player;
import com.escuelagaing.edu.co.model.User;

@ExtendWith(MockitoExtension.class)
class PlayerTest {

    @Mock
    private User user;

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(user, "Player 1", "room1", 100);
    }

    @Test
    void testCalculateScore_WithAce() {
        // Usamos Strings en lugar de CardRank
        List<Card> hand = Arrays.asList(
            new Card("HEARTS", "ACE", 11),   // Representación de un As
            new Card("HEARTS", "TEN", 10));  // Representación de un 10
        player.setHand(hand);

        int score = player.calculateScore();

        assertEquals(21, score);  // El puntaje debe ser 21 (10 + 11)
    }

    @Test
    void testCalculateScore_WithMultipleAces() {
        // Usamos Strings en lugar de CardRank
        List<Card> hand = Arrays.asList(
            new Card("HEARTS", "ACE", 11),  // Primer As
            new Card("HEARTS", "ACE", 11),  // Segundo As
            new Card("HEARTS", "NINE", 9)); // Nueve
        player.setHand(hand);

        int score = player.calculateScore();

        assertEquals(21, score);  // Dos ases, uno debería contar como 1
    }

    @Test
    void testCalculateScore_Over21() {
        // Usamos Strings en lugar de CardRank
        List<Card> hand = Arrays.asList(
            new Card("HEARTS", "ACE", 11),    // Primer As
            new Card("HEARTS", "QUEEN", 10),  // Reina
            new Card("HEARTS", "KING", 10));  // Rey
        player.setHand(hand);

        int score = player.calculateScore();

        assertEquals(21, score);  // El puntaje no debe superar 21, se debe ajustar el As
    }
}


