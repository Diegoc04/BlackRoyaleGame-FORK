package com.escuelagaing.edu.co.dto;

public class UserDTO {
    private String email;
    private String name; 
    private String nickName;

    public UserDTO() {
    }

    public UserDTO(String email, String name) {
        this(email, name, null); // Llamada al constructor con todos los parámetros
    }

    public UserDTO(String email, String name, String nickName) {
        this.email = email;
        this.name = name;
        this.nickName = nickName;
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

}
