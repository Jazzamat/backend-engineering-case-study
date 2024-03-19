package com.dreamgames.backendengineeringcasestudy.userservice.model;

import java.util.Random;

import com.dreamgames.backendengineeringcasestudy.tournamentservice.model.TournamentEntry;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String username;
	private int coins = 5000;
	private int level = 1;

	@Enumerated(EnumType.STRING)
	private Country country;

	// @OneToOne
	// private TournamentEntry currentEntry;

	public User() {
	}

	public User(String username, Country country) {
		this.username = username;
		this.country = country;
	}


	public static enum Country {
		TURKEY, USA, UK, FRANCE, GERMANY;
		public static Country getRandomCountry() {
			Random random = new Random();
			return values()[random.nextInt(values().length)];
		}
	}

	/**
	 * @precondition coins >= 1000
	 * @postconditoin coins >= 0
	*/
	public void deductFee() {
		this.coins = this.coins - 1000;
	}

	// public void increaseEntryScore() {
	// 	if (currentEntry != null) {
	// 		currentEntry.increaseEntryScore();
	// 	}
	// }

	// public void setCurrentEntry(TournamentEntry currentEntry) {
	// 	this.currentEntry = currentEntry;
	// }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getCoins() {
		return coins;
	}

	public void setCoins(int coins) {
		this.coins = coins;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (coins != other.coins)
			return false;
		if (level != other.level)
			return false;
		if (country != other.country)
			return false;
		return true;
	}


}
