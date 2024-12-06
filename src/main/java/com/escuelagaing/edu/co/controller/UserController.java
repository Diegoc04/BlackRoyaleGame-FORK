package com.escuelagaing.edu.co.controller;

import com.escuelagaing.edu.co.dto.UserDTO;
import com.escuelagaing.edu.co.model.User;
import com.escuelagaing.edu.co.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin("*")
@RequestMapping("api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDTO userDto) {
        try {
            logger.info("Solicitud recibida para crear un nuevo usuario.");

            User user = new User();
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
            user.setNickName("NULL");

            logger.info("Creando usuario en el sistema.");

            User createdUser = userService.createUser(user);

            logger.info("Usuario creado exitosamente.");

            return ResponseEntity.ok(createdUser);
        } catch (RuntimeException e) {
            logger.error("Error al crear el usuario.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    public ResponseEntity<List<Map<String, Object>>> getUserGameHistory(@PathVariable String id) {
        try {
            List<Map<String, Object>> gameHistory = userService.getUserGameHistory(id);
            return ResponseEntity.ok(gameHistory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
    }

    // Obtener un usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Actualizar un usuario
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody UserDTO userDetails) {
        try {
            // Convertir el DTO a la entidad
            User user = new User();
            
            user.setNickName(userDetails.getNickName());
            // Mapear otros campos según sea necesario
            // Llamar al servicio de actualización
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    

    // Eliminar un usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build(); // Retorna 204 No Content
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
