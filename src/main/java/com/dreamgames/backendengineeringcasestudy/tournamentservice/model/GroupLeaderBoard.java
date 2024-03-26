package com.dreamgames.backendengineeringcasestudy.tournamentservice.model;

import java.util.List;

import org.javatuples.Pair;

import com.dreamgames.backendengineeringcasestudy.userservice.model.User;

public class GroupLeaderBoard { 
   
    private List<Pair<User,Integer>> leaderboard;
    private Long groupId;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leaderboard == null) ? 0 : leaderboard.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupLeaderBoard other = (GroupLeaderBoard) obj;
        if (leaderboard == null) {
            if (other.leaderboard != null)
                return false;
        } else if (!leaderboard.equals(other.leaderboard))
            return false;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;
        return true;
    }

    


}


