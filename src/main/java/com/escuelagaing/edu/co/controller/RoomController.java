package com.escuelagaing.edu.co.controller;

import com.escuelagaing.edu.co.dto.RoomDTO;
import com.escuelagaing.edu.co.model.Room;
import com.escuelagaing.edu.co.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoomById(@PathVariable String roomId) {
        return roomService.getRoomById(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{roomId}")
    public Room createRoom(@PathVariable String roomId) {
        return roomService.createRoom(roomId);
    }

    @PutMapping
    public Room updateRoom(@RequestBody RoomDTO roomDTO) {
        // Usa el servicio para actualizar la entidad
        return roomService.updateRoom(roomDTO);
    }


    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}
