package com.borrowbuddy.backend.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "items")
@Data
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne @JoinColumn(name = "owner_id")
    private User owner;
    @ManyToOne @JoinColumn(name = "category_id")
    private Category category;
    private String title;
    private String description;
    @Column(name="\"condition\"")
    private String condition;
    private Boolean isAvailable = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}