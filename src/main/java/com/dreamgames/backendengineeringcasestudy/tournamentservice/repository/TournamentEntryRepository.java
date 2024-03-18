package com.dreamgames.backendengineeringcasestudy.tournamentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;

@Repository
public interface TournamentEntryRepository extends JpaRepository<TournamentEntry, Long> {}