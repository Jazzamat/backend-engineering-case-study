package com.dreamgames.backendengineeringcasestudy.tournamentservice.model;


import com.dreamgames.backendengineeringcasestudy.userservice.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

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

	public void incrementScore() {
		this.score++;
	}

	public boolean groupHasBegun() {
		return this.tournamentGroup.hasBegun();
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


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TournamentEntry other = (TournamentEntry) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (tournamentGroup == null) {
			if (other.tournamentGroup != null)
				return false;
		} else if (!tournamentGroup.equals(other.tournamentGroup))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (score != other.score)
			return false;
		if (rewardClaimed != other.rewardClaimed)
			return false;
		return true;
	}

	
}
