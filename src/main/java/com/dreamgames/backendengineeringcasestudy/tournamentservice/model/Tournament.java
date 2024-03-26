package com.dreamgames.backendengineeringcasestudy.tournamentservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

/**
 * Represents a tournament.
 * 
 * @author E. Omer Gul
 */
@OptimisticLocking(type=OptimisticLockType.ALL)
@Entity
public class Tournament {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private Set<TournamentGroup> groups = new HashSet<>();

    @Version
    private int version;
    
     /**
     * Constructs an empty tournament.
     */
    public Tournament() {
    }

    /**
     * Constructs a tournament with the specified start and end time.
     * 
     * @param startTime the start time of the tournament
     * @param endTime the end time of the tournament
     */
    public Tournament(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    /**
     * Adds a user to the tournament and returns the group the user is added to.
     * If no groups exist, a new group is created and the user is added to it.
     * 
     * @param user the user to add to the tournament
     * @return the group the user is added to
     * @throws Exception if the user cannot be added to any group
     */
    public TournamentGroup addUser(User user) throws Exception {
        if (groups.isEmpty()) {
            TournamentGroup newGroup = new TournamentGroup(this);
            newGroup.addUser(user);
            groups.add(newGroup);
            return newGroup;
        }
        return MatchMake(user);
    }
    
    /**
     * Attempts to add a user to an existing group in the tournament.
     * If no group can accommodate the user, a new group is created and the user is added to it.
     * 
     * @param user the user to add to the tournament
     * @return the group the user is added to
     * @throws Exception if the user cannot be added to any group
     */
    public TournamentGroup MatchMake(User user) throws Exception {
        for (TournamentGroup group : groups) {
            if (group.addUser(user)) {
                return group;
            }
        }
        TournamentGroup newGroup = new TournamentGroup(this);
        if (newGroup.addUser(user)) {
            return newGroup;
        } else {
            throw new Exception("cant add to group for some reason");
        }
    }

    /**
     * Checks if the tournament has ended.
     * 
     * @return true if the tournament has ended, false otherwise
     */
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    /**
     * Checks if the tournament has begun.
     * 
     * @return true if the tournament has begun, false otherwise
     */
    public boolean hasBegun() {
        return LocalDateTime.now().isAfter(startTime);
    }
    
    /**
     * Returns the ID of the tournament.
     * 
     * @return the ID of the tournament
     */
    public Long getId() {
        return id;
    }

    public void endTournament() {
        this.endTime = LocalDateTime.now();
    }

}
