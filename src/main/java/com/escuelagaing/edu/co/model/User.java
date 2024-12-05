
package com.escuelagaing.edu.co.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.escuelagaing.edu.co.dto.RoomStateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@Document(collection = "User")
public class User {

    @Id
    private String email;

    @Field(name= "name")
    private String name; 

    @Field(name = "nickName")
    private String nickName; 
    
    @Field(name = "gameHistory")
    private List<Map<String, Object>> gameHistory = new ArrayList<>();

    @Field(name = "amount") 
    private double amount = 10000;

    public User() {
    }
    public User(String email, String name, String nickName) {
        this.email = email;
        this.name = name;
        this.nickName = nickName;
        this.amount = 10000;
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

      public double getAmount() {
        return amount;
    }

    public void increaseAmount(double amount) {
        this.amount += amount;
    }

    public void decreaseAmount(double amount) {
        if (this.amount >= amount) {
            this.amount -= amount;
        } else {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
    }

   public void addGameToHistory(RoomStateDTO gameState) {
    ObjectMapper objectMapper = new ObjectMapper();
    @SuppressWarnings("unchecked")
    Map<String, Object> gameData = objectMapper.convertValue(gameState, Map.class);
    this.gameHistory.add(gameData);
}

    
public List<Map<String, Object>> getGameHistory() {
    return this.gameHistory;
}

public void setAmount(double amount){
    this.amount = amount;
}
     



}
