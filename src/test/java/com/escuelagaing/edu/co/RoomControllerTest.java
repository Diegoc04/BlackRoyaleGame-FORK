package com.escuelagaing.edu.co;

import com.escuelagaing.edu.co.controller.RoomController;
import com.escuelagaing.edu.co.dto.RoomDTO;
import com.escuelagaing.edu.co.model.Room;
import com.escuelagaing.edu.co.model.RoomStatus;
import com.escuelagaing.edu.co.service.RoomService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    public RoomControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllRooms() {
        // Arrange
        Room room1 = new Room();
        room1.setId("1");
        room1.setStatus(RoomStatus.EN_ESPERA);

        Room room2 = new Room();
        room2.setId("2");
        room2.setStatus(RoomStatus.EN_ESPERA);

        when(roomService.getAllRooms()).thenReturn(List.of(room1, room2));

        // Act
        List<Room> rooms = roomController.getAllRooms();

        // Assert
        assertNotNull(rooms);
        assertEquals(2, rooms.size());
        assertEquals("1", rooms.get(0).getRoomId());
        assertEquals("2", rooms.get(1).getRoomId());
        verify(roomService, times(1)).getAllRooms();
    }

    @Test
    void testGetRoomById() {
        // Arrange
        String roomId = "1";
        Room room = new Room();
        room.setId(roomId);
        room.setStatus(RoomStatus.EN_ESPERA);

        when(roomService.getRoomById(roomId)).thenReturn(Optional.of(room));

        // Act
        ResponseEntity<Room> response = roomController.getRoomById(roomId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(roomId, response.getBody().getRoomId());
        verify(roomService, times(1)).getRoomById(roomId);
    }

    @Test
    void testGetRoomById_NotFound() {
        // Arrange
        String roomId = "1";
        when(roomService.getRoomById(roomId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Room> response = roomController.getRoomById(roomId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(roomService, times(1)).getRoomById(roomId);
    }

    @Test
    void testCreateRoom() {
        // Arrange
        String roomId = "1";
        Room room = new Room();
        room.setId(roomId);

        when(roomService.createRoom(roomId)).thenReturn(room);

        // Act
        Room createdRoom = roomController.createRoom(roomId);

        // Assert
        assertNotNull(createdRoom);
        assertEquals(roomId, createdRoom.getRoomId());
        verify(roomService, times(1)).createRoom(roomId);
    }

    @Test
    void testUpdateRoom() {
        // Arrange
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId("1");

        Room updatedRoom = new Room();
        updatedRoom.setId("1");
        updatedRoom.setStatus(RoomStatus.EN_JUEGO);

        when(roomService.updateRoom(roomDTO)).thenReturn(updatedRoom);

        // Act
        Room result = roomController.updateRoom(roomDTO);

        // Assert
        assertNotNull(result);
        assertEquals("1", result.getRoomId());
        assertEquals(RoomStatus.EN_JUEGO, result.getStatus());
        verify(roomService, times(1)).updateRoom(roomDTO);
    }

    @Test
    void testDeleteRoom() {
        // Arrange
        String roomId = "1";
        doNothing().when(roomService).deleteRoom(roomId);

        // Act
        ResponseEntity<Void> response = roomController.deleteRoom(roomId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(roomService, times(1)).deleteRoom(roomId);
    }
}
