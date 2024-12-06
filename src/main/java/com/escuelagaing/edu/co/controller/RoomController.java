package com.escuelagaing.edu.co.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@Tag(name = "Room Controller", description = "Endpoints para la gestión de salas. Permite crear, actualizar, obtener y eliminar salas en el sistema educativo.")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "Obtener todas las salas", description = "Este endpoint devuelve una lista de todas las salas disponibles en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de salas obtenida con éxito")
    })
    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @Operation(summary = "Obtener una sala por ID", description = "Este endpoint devuelve los detalles de una sala específica basada en su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala encontrada"),
            @ApiResponse(responseCode = "404", description = "Sala no encontrada")
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoomById(@PathVariable String roomId) {
        return roomService.getRoomById(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear una sala", description = "Este endpoint crea una nueva sala con el ID especificado. Devuelve los detalles de la sala creada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sala creada con éxito")
    })
    @PostMapping("/{roomId}")
    public Room createRoom(@PathVariable String roomId) {
        return roomService.createRoom(roomId);
    }

    @Operation(summary = "Actualizar una sala", description = "Este endpoint permite actualizar los detalles de una sala existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala actualizada con éxito")
    })
    @PutMapping
    public Room updateRoom(@RequestBody RoomDTO roomDTO) {
        // Usa el servicio para actualizar la entidad
        return roomService.updateRoom(roomDTO);
    }

    @Operation(summary = "Eliminar una sala", description = "Este endpoint elimina una sala específica por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sala eliminada con éxito"),
            @ApiResponse(responseCode = "404", description = "Sala no encontrada")
    })
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}
