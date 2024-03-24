package com.dreamgames.backendengineeringcasestudy.tournamentservice.repository;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Tournament entities.
 * 
 * @author E. Omer Gul
 */
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /**
     * Finds a tournament that has a start time before the specified start time and an end time after the specified end time.
     *
     * @param start the start time
     * @param end the end time
     * @return an Optional containing the found Tournament, or an empty Optional if no Tournament is found
     */
    Optional<Tournament> findTournamentByStartTimeBeforeAndEndTimeAfter(LocalDateTime start, LocalDateTime end);
}