package com.dreamgames.backendengineeringcasestudy.tournamentservice.repository;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.Tournament;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament,Long> {
    Optional<Tournament> findTournamentByStartTimeBeforeAndEndTimeAfter(LocalDateTime start, LocalDateTime end);
}