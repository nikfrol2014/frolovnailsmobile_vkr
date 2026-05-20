package com.example.frolovnails.network.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String phone;
    private String password;
    private String firstName;
    private String lastName;
}