package com.escuelagaing.edu.co;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.escuelagaing.edu.co.model.ChatMessage;

class ChatMessageTest {

    @Test
    void testConstructorSinParametros() {
        ChatMessage message = new ChatMessage();
        assertNull(message.getSender());
        assertNull(message.getMessage());
        assertNull(message.getRoomId());
    }

    @Test
    void testConstructorConParametros() {
        String sender = "user1";
        String messageContent = "Hello, World!";
        String roomId = "room123";

        ChatMessage message = new ChatMessage(sender, messageContent, roomId);
        
        assertEquals(sender, message.getSender());
        assertEquals(messageContent, message.getMessage());
        assertEquals(roomId, message.getRoomId());
    }

    @Test
    void testSettersAndGetters() {
        ChatMessage message = new ChatMessage();

        message.setSender("user1");
        assertEquals("user1", message.getSender());

        message.setMessage("Hello!");
        assertEquals("Hello!", message.getMessage());

        message.setRoomId("room123");
        assertEquals("room123", message.getRoomId());
    }
}

