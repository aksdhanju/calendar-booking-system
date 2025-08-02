package com.company.calendar.repository.appointment;

import com.company.calendar.entity.Appointment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "appointment.repository", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryAppointmentRepository implements AppointmentRepository{

    private final Map<String, List<Appointment>> store = new ConcurrentHashMap<>();
    private final Map<String, Appointment> appointmentIdIndex = new ConcurrentHashMap<>();

    @Override
    public boolean existsById(String appointmentId) {
        return appointmentIdIndex.containsKey(appointmentId);
    }

    @Override
    public List<Appointment> findByOwnerIdAndDate(String ownerId, LocalDate date) {
        return store.getOrDefault(ownerId, Collections.emptyList())
                .stream()
                .filter(a -> a.getStartTime().toLocalDate().equals(date))
                .toList();
    }

    @Override
    public boolean existsByOwnerIdAndStartTime(String ownerId, LocalDateTime startTime) {
        return store.getOrDefault(ownerId, Collections.emptyList())
                .stream()
                .anyMatch(a -> a.getStartTime().equals(startTime));
    }

    @Override
    //@Transactional here or not?
    public void save(Appointment appointment) {
        store.computeIfAbsent(appointment.getOwnerId(), k -> new ArrayList<>())
                .add(appointment);
        appointmentIdIndex.put(appointment.getAppointmentId(), appointment);
    }

    @Override
    //You can add a new method to atomically check-and-insert an appointment in a thread-safe way:
    public boolean saveIfSlotFree(Appointment appointment) {
        String ownerId = appointment.getOwnerId();
        LocalDateTime startTime = appointment.getStartTime();

        return store.compute(ownerId, (key, existingList) -> {
            if (existingList == null) {
                existingList = new ArrayList<>();
            }

            boolean exists = existingList.stream()
                    .anyMatch(a -> a.getStartTime().equals(startTime));

            if (exists) {
                return existingList; // no changes, slot already taken
            }

            existingList.add(appointment);
            return existingList;
        }).stream().anyMatch(a ->
                a.getStartTime().equals(startTime) &&
                        a.getInviteeId().equals(appointment.getInviteeId()) &&
                        a.getAppointmentId().equals(appointment.getAppointmentId())
        );
    }

    @Override
    public List<Appointment> findByOwnerIdAfter(String ownerId, LocalDateTime after) {
        return store.getOrDefault(ownerId, Collections.emptyList())
                .stream()
                .filter(a -> a.getStartTime().isAfter(after))
                .sorted(Comparator.comparing(Appointment::getStartTime))
                .toList();
    }
}
