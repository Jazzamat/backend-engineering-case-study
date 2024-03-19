package com.dreamgames.backendengineeringcasestudy.tournamentservice.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;

@Repository
public interface TournamentEntryRepository extends JpaRepository<TournamentEntry, Long> {
    @Query("SELECT COUNT(te) > 0 FROM TournamentEntry te WHERE te.user.id = :userId AND te.tournamentGroup.tournament.id = :tournamentId")
    boolean existsByUserIdAndTournamentId(@Param("userId") Long userId, @Param("tournamentId") Long tournamentId);

    @Query("SELECT te FROM TournamentEntry te WHERE te.user.id = :userId AND te.tournamentGroup.tournament.id = :tournamentId")
    Optional<TournamentEntry> findByUserIdAndTournamentId(@Param("userId") Long userId, @Param("tournamentId") Long tournamentId);

    List<TournamentEntry> findByTournamentGroupIdOrderByScoreDesc(Long groupId);

}