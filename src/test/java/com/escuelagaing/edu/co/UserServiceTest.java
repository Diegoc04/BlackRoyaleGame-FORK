package com.escuelagaing.edu.co;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.escuelagaing.edu.co.exception.UserServiceException;
import com.escuelagaing.edu.co.model.User;
import com.escuelagaing.edu.co.repository.UserRepository;
import com.escuelagaing.edu.co.service.UserService;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("test@example.com", "Test", "TestNickName");
    }

    @Test
    void testCreateUser_WhenUserDoesNotExist_ShouldCreateUser() {
        when(userRepository.existsById(user.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getName());
        assertEquals("TestNickName", result.getNickName());
    }

    @Test
    void testCreateUser_WhenUserAlreadyExists_ShouldThrowException() {
        when(userRepository.existsById(user.getEmail())).thenReturn(true);

        UserServiceException.UserAlreadyExistsException exception = assertThrows(
            UserServiceException.UserAlreadyExistsException.class,
            () -> userService.createUser(user)
        );

        // Verifica que el mensaje de la excepción sea el esperado
        assertEquals("El usuario con email " + user.getEmail() + " ya existe.", exception.getMessage());
}

    @Test
    void testGetUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(user.getEmail())).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(user.getEmail());

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("Test", result.get().getName());
    }

    @Test
    void testGetUserGameHistory_WhenUserNotFound_ShouldThrowException() {
        String userId = "test@example.com";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserServiceException.UserNotFoundException exception = assertThrows(
            UserServiceException.UserNotFoundException.class,
            () -> userService.getUserGameHistory(userId)
        );

        assertEquals("Usuario no encontrado con ID: " + userId, exception.getMessage());
    }

    @Test
    void testUpdateUser_WhenUserExists_ShouldUpdateUser() {
        String userId = "test@example.com";
        User updatedUser = new User("test@example.com", "Test Updated", "UpdatedNickName");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(userId, updatedUser);

        assertNotNull(result);
        assertEquals("Test Updated", result.getName());
        assertEquals("UpdatedNickName", result.getNickName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testUpdateUser_WhenUserNotFound_ShouldThrowException() {
        String userId = "test@example.com";
        User updatedUser = new User("test@example.com", "Test Updated", "UpdatedNickName");
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserServiceException.UserNotFoundException exception = assertThrows(
            UserServiceException.UserNotFoundException.class,
            () -> userService.updateUser(userId, updatedUser)
        );

        assertEquals("Usuario no encontrado con ID: " + userId, exception.getMessage());
    }

    @Test
    void testDeleteUser_WhenUserExists_ShouldDeleteUser() {
        String userId = "test@example.com";
        
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testDeleteUser_WhenUserNotFound_ShouldThrowException() {
        String userId = "test@example.com";
        
        when(userRepository.existsById(userId)).thenReturn(false);

        UserServiceException.UserNotFoundException exception = assertThrows(
            UserServiceException.UserNotFoundException.class,
            () -> userService.deleteUser(userId)
        );

        assertEquals("Usuario no encontrado con ID: " + userId, exception.getMessage());
    }

}
