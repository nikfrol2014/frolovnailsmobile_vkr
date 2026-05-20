package com.example.frolovnails.network.models.response;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String phone;
    private String role;
    private String firstName;
    private String lastName;

    // Пустой конструктор (нужен для Retrofit)
    public AuthResponse() {}

    // Геттеры
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Long getUserId() { return userId; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    // Сеттеры
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}