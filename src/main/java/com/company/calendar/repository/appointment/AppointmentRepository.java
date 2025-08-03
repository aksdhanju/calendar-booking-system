package com.company.calendar.repository.appointment;

import com.company.calendar.entity.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentRepository {
    List<Appointment> findByOwnerIdAndDate(String ownerId, LocalDate date);
    boolean existsByOwnerIdAndStartTime(String ownerId, LocalDateTime startTime);
    void save(Appointment appointment);
    boolean saveIfSlotFree(Appointment appointment);
    Page<Appointment> findByOwnerIdAndStartTimeAfter(String ownerId, LocalDateTime after, Pageable pageable);
}
