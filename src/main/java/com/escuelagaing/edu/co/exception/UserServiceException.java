package com.escuelagaing.edu.co.exception;


public class UserServiceException extends RuntimeException {
    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class UserNotFoundException extends UserServiceException {
        public UserNotFoundException(String userId) {
            super("Usuario no encontrado con ID: " + userId);
        }
    }

    public static class UserAlreadyExistsException extends UserServiceException {
        public UserAlreadyExistsException(String email) {
            super("El usuario con email " + email + " ya existe.");
        }
    }

    public static class GeneralUserServiceException extends UserServiceException {
        public GeneralUserServiceException(String message) {
            super(message);
        }
    }
}
