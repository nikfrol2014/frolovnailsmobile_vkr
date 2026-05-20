package com.example.frolovnails.network.models.response;

public class ProfileResponse {
    private UserInfo user;
    private ClientInfo client;

    public ProfileResponse() {}

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    public ClientInfo getClient() { return client; }
    public void setClient(ClientInfo client) { this.client = client; }

    public static class UserInfo {
        private Long id;
        private String phone;
        private String role;
        private String createdAt;

        public UserInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    public static class ClientInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String birthDate;
        private String notes;
        private String createdAt;

        public ClientInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getBirthDate() { return birthDate; }
        public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}