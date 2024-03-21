package com.dreamgames.backendengineeringcasestudy.tournamentservice.model;

import java.util.List;

import org.javatuples.Pair;

import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

public class GroupLeaderBoard { // TODO make sure this aint just a data class
   
    public List<Pair<User,Integer>> leaderboard;
    public Long groupId;

    public GroupLeaderBoard(List<Pair<User, Integer>> leaderboard, Long groupId) {
        this.leaderboard = leaderboard;
        this.groupId = groupId;
    }

    public List<Pair<User, Integer>> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<Pair<User, Integer>> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}


