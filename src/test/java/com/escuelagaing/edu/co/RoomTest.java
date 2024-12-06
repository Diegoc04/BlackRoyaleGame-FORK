package com.escuelagaing.edu.co;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.escuelagaing.edu.co.model.Player;
import com.escuelagaing.edu.co.model.Room;
import com.escuelagaing.edu.co.model.RoomStatus;


class RoomTest {

    private Room room;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        room = new Room();
        player1 = mock(Player.class);
        player2 = mock(Player.class);
    }

    @Test
    void testAddPlayer() {
        // Verificar que un jugador se pueda agregar
        assertTrue(room.addPlayer(player1));
        assertEquals(1, room.getPlayers().size());
        
        // Verificar que el estado cambia cuando el número de jugadores alcanza el mínimo
        room.addPlayer(player2);
        assertEquals(RoomStatus.EN_APUESTAS, room.getStatus());
    }


    @Test
    void testStartBetting() {
        room.startBetting();

        // Verificar que el estado de la sala es "EN_APUESTAS"
        assertEquals(RoomStatus.EN_APUESTAS, room.getStatus());
    }

    @Test
    void testIsFull() {
        room.addPlayer(player1);
        room.addPlayer(player2);

        assertFalse(room.isFull());

        // Rellenar la sala
        for (int i = 0; i < room.getMaxPlayers() - 2; i++) {
            room.addPlayer(mock(Player.class));
        }

        assertTrue(room.isFull());
    }

    @Test
    void testGetPlayerById() {
        room.addPlayer(player1);
        room.addPlayer(player2);

        when(player1.getId()).thenReturn("1");
        when(player2.getId()).thenReturn("2");

        Player foundPlayer = room.getPlayerById("1");
        assertEquals(player1, foundPlayer);

        foundPlayer = room.getPlayerById("2");
        assertEquals(player2, foundPlayer);

        foundPlayer = room.getPlayerById("3");
        assertNull(foundPlayer);
    }

    @Test
    void testGetPlayerByName() {
        room.addPlayer(player1);
        room.addPlayer(player2);

        when(player1.getName()).thenReturn("Player One");
        when(player2.getName()).thenReturn("Player Two");

        Player foundPlayer = room.getPlayerByName("Player One");
        assertEquals(player1, foundPlayer);

        foundPlayer = room.getPlayerByName("Player Two");
        assertEquals(player2, foundPlayer);

        foundPlayer = room.getPlayerByName("Player Three");
        assertNull(foundPlayer);
    }
}
