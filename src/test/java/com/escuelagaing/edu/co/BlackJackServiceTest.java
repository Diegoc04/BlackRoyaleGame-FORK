package com.escuelagaing.edu.co;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.escuelagaing.edu.co.model.Game;
import com.escuelagaing.edu.co.model.Player;
import com.escuelagaing.edu.co.model.Room;
import com.escuelagaing.edu.co.repository.BlackJackRepository;
import com.escuelagaing.edu.co.service.BlackJackService;

@ExtendWith(MockitoExtension.class)
class BlackJackServiceTest {

    @Mock
    private BlackJackRepository roomRepository;

    @Mock
    private Game game;

    @InjectMocks
    private BlackJackService blackJackService;

    @BeforeEach
    void setUp() {
        blackJackService = new BlackJackService(roomRepository);
    }

    @Test
    void testCreateRoom() {
        Room room = new Room();
        room.setId("room-" + System.currentTimeMillis());

        when(roomRepository.save(any(Room.class))).thenReturn(room);

        Room createdRoom = blackJackService.createRoom();

        assertNotNull(createdRoom);
        assertTrue(createdRoom.getRoomId().startsWith("room-"));

        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testGetRoom_Found() {
        String roomId = "room-123";
        Room room = new Room();
        room.setId(roomId);
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        
        Room result = blackJackService.getRoom(roomId);
        
        assertNotNull(result);
        assertEquals(roomId, result.getRoomId());
        verify(roomRepository, times(1)).findById(roomId);
    }

    @Test
    void testGetRoom_NotFound() {
        String roomId = "room-123";
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        
        Room result = blackJackService.getRoom(roomId);
        
        assertNull(result);
        verify(roomRepository, times(1)).findById(roomId);
    }

    @Test
    void testAddPlayerToRoom() {
        String roomId = "room-123";
        Player player = new Player();
        Room room = new Room();
        room.setId(roomId);
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        
        blackJackService.addPlayerToRoom(roomId, player);
        
        assertTrue(room.getPlayers().contains(player));
        verify(roomRepository, times(1)).save(room);
    }

    @Test
    void testCreateRoom_Failure() {
        when(roomRepository.save(any(Room.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> blackJackService.createRoom());
    }

    @Test
    void testGetRoom_InvalidId() {
        String invalidRoomId = "";
        
        Room result = blackJackService.getRoom(invalidRoomId);
        
        assertNull(result);
    }

    @Test
    void testAddPlayerToRoom_NewRoom() {
        String roomId = "room-999";
        Player player = new Player();
        
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        blackJackService.addPlayerToRoom(roomId, player);
        
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testGetPlayerById_NotFound() {
        String playerId = "player-123";
        
        when(roomRepository.findAll()).thenReturn(Collections.emptyList()); // No hay salas
        
        Player result = blackJackService.getPlayerById(playerId);
        
        assertNull(result);
    }






}
