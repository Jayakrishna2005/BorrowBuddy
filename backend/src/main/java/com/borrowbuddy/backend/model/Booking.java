package com.borrowbuddy.backend.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne @JoinColumn(name = "item_id")
    private Item item;
    @ManyToOne @JoinColumn(name = "borrower_id")
    private User borrower;
    private LocalDateTime scheduledPickup;
    private LocalDateTime scheduledReturn;
    private String status; // PENDING, ACCEPTED, REJECTED, ACTIVE, COMPLETED
    private LocalDateTime createdAt = LocalDateTime.now();
}