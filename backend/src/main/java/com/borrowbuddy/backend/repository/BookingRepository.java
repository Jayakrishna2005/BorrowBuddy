package com.borrowbuddy.backend.repository;
import com.borrowbuddy.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
}