package com.escuelagaing.edu.co;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.escuelagaing.edu.co.dto.PlayerStateDTO;
import com.escuelagaing.edu.co.model.Card;
import com.escuelagaing.edu.co.model.Chip;

class PlayerStateDTOTest {

    @Test
    void testConstructorWithParams() {
        // Crear una mano de cartas
        List<Card> hand = Arrays.asList(new Card("HEART", "A", 1), new Card("SPADE", "K", 10));
        
        // Crear fichas de tipo Chip (enum)
        List<Chip> chips = Arrays.asList(Chip.ROJO, Chip.AZUL);

        // Crear un objeto PlayerStateDTO usando las cartas y las fichas
        PlayerStateDTO playerState = new PlayerStateDTO("Player1", 100.0, hand, chips, 20.0, true);

        // Verificar que los valores del DTO son correctos
        assertEquals("Player1", playerState.getNickName());
        assertEquals(100.0, playerState.getAmount(), 0.0);
        assertEquals(20.0, playerState.getBet(), 0.0);
        assertEquals(hand, playerState.getHand());
        assertEquals(chips, playerState.getChips());
        assertTrue(playerState.isInTurn());
    }
}



