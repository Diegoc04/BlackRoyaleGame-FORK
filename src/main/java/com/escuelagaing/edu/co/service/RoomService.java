package com.escuelagaing.edu.co.service;
import com.escuelagaing.edu.co.dto.RoomDTO;
import com.escuelagaing.edu.co.model.Room;
import com.escuelagaing.edu.co.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(String roomId) {
        return roomRepository.findById(roomId);
    }
    public Room saveRoomState(Room room) {
        return roomRepository.save(room); 
    }

    public void resetRoom(Room room) {
        room.resetRoom(); 
    }

    public Room createRoom(String roomId) {
        return roomRepository.findById(roomId).orElseGet(() -> {
            Room newRoom = new Room();
            newRoom.setId(roomId);
            return roomRepository.save(newRoom);
        });
    }

    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }


    public void deleteRoom(String roomId) {
        roomRepository.deleteById(roomId);
    }

    public Room updateRoom(RoomDTO roomDTO) {
        // Busca la sala por su ID
        Room room = roomRepository.findById(roomDTO.getId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Actualiza solo los campos permitidos
        room.setStatus(roomDTO.getStatus());

        // Guarda la entidad actualizada
        return roomRepository.save(room);
    }
}
