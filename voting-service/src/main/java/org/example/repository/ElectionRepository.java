package org.example.repository;

import org.example.dto.ElectionDTO;
import org.example.entities.Election;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ElectionRepository extends JpaRepository<Election, Long> {
    List<Election> findByActiveTrue();
    List<Election> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime now1, LocalDateTime now2);
    List<Election> findByEndTimeBefore(LocalDateTime now);
    List<Election> findByStartTimeAfter(LocalDateTime now);
}