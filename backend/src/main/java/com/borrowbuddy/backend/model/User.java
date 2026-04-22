package com.borrowbuddy.backend.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String registrationNumber;
    private String passwordHash;
    private String fullName;
    private String email;
    private Integer trustScore = 50;
    private Integer itemsLent = 0;
    private Integer itemsBorrowed = 0;
    private Integer gratitudeCount = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
}