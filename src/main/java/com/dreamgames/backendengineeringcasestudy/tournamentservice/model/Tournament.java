package com.dreamgames.backendengineeringcasestudy.tournamentservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.transaction.Transactional;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

@Entity
public class Tournament {

    @Id 
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
 

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL,fetch = FetchType.EAGER) //TODO see if you can make this lazy 
    private Set<TournamentGroup> groups = new HashSet<>(); 

	public Tournament(LocalDateTime startTime, LocalDateTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

    public Tournament() {
	}

	public TournamentGroup addUser(User user) throws Exception {
        if (groups.isEmpty()) {
            TournamentGroup newGroup = new TournamentGroup(this);
            newGroup.addUser(user);
            return newGroup;
        }
        return MatchMake(user);
    }

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

    public Long getId() {
        return id;
    }
}
