package com.dreamgames.backendengineeringcasestudy.tournamentservice.model;

import org.hibernate.internal.util.compare.ComparableComparator;

import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
public class TournamentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private TournamentGroup tournamentGroup;

    @ManyToOne
    private User user;

    private int score = 0;
    private boolean rewardClaimed = false;

	public TournamentEntry() {
	}

	public TournamentEntry(TournamentGroup tournamentGroup, User user) {
		this.tournamentGroup = tournamentGroup;
		this.user = user;
	}


    public boolean containsCountry(User.Country country) {
        return user.getCountry() == country;
    }

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public TournamentGroup getTournamentGroup() {
		return tournamentGroup;
	}
	public void setTournamentGroup(TournamentGroup tournamentGroup) {
		this.tournamentGroup = tournamentGroup;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public boolean isRewardClaimed() {
		return rewardClaimed;
	}
	public void setRewardClaimed(boolean rewardClaimed) {
		this.rewardClaimed = rewardClaimed;
	}
}
