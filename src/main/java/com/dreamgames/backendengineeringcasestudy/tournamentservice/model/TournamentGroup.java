package com.dreamgames.backendengineeringcasestudy.tournamentservice.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

@Entity
public class TournamentGroup {
  
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tournament tournament;

    @OneToMany(mappedBy = "tournamentGroup", cascade = CascadeType.ALL,fetch = FetchType.EAGER) 
    @OrderBy("score DESC")
    private List<TournamentEntry> entries = new ArrayList<TournamentEntry>();

    public TournamentGroup() {
    }

    public TournamentGroup(Tournament tournament) {
        this.tournament = tournament;
    }

    public boolean addUser(User user) {
        if (entries.size() == 5) {
            return false;
        }
        User.Country country = user.getCountry();

        for (TournamentEntry entry : entries) {
            if (entry.containsCountry(country)) {
                return false;
            }
        }

        // TODO check for duplicate entries too

        TournamentEntry newEntry = new TournamentEntry(this, user);
        entries.add(newEntry);
        return true;
    }

    // public List<User> getGroupLeaderboard() {
    //    List<User> users = new ArrayList<>();    
    //    entries.stream().sorted().forEach((x) -> users.add(x.getUser()));
    //    return users;
    // }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Tournament getTournament() {
		return tournament;
	}

	public void setTournament(Tournament tournament) {
		this.tournament = tournament;
	}

	public List<TournamentEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<TournamentEntry> entries) {
		this.entries = entries;
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TournamentGroup other = (TournamentGroup) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (tournament == null) {
            if (other.tournament != null)
                return false;
        } else if (!tournament.equals(other.tournament))
            return false;
        if (entries == null) {
            if (other.entries != null)
                return false;
        } else if (!entries.equals(other.entries))
            return false;
        return true;
    }
   
    

}
