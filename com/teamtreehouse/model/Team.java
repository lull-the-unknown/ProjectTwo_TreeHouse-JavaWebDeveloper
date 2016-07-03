package com.teamtreehouse.model;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class Team implements Comparable<Team> {
    private String mName;
    private String mCoach;
    private Set<Player> mPlayers;
    
    public Team( String teamName, String coachName){
        mName= teamName;
        mCoach = coachName;
        mPlayers = new TreeSet();
    }

    public String getName() { return mName; }
    public String getCoach() { return mCoach; }
    public Set<Player> getPlayers() { return mPlayers; }
    
    public boolean addPlayer( Player newPlayer){
        return mPlayers.add(newPlayer);
    }
    public boolean removePlayer( Player player){
        return mPlayers.remove(player);        
    }
    
    public Map<Integer,Set<Player>> getPlayersGroupedByHeight(){
        Map<Integer,Set<Player>> results = new TreeMap<>();
        int height;
        Set<Player> playerSet;
        
        for( Player player : mPlayers){
            height = player.getHeightInInches();
            playerSet = results.get(height);
            if (playerSet == null){
                playerSet = new TreeSet<>();
                results.put(height, playerSet);
            }
            playerSet.add(player);
        }
        return results;
    }

    @Override
    public int compareTo(Team that) {
        if( this.equals(that))
            return 0;
        return mName.compareTo(that.mName);
    }
    
    public double getAverageHeight(){
        if (mPlayers.size() < 1 )
            return 0d;
        double sum = 0d;
        for( Player player : mPlayers)
            sum += player.getHeightInInches();
        return sum / mPlayers.size();
    }
    
    public int getCount_AllPlayers(){
        return mPlayers.size();
    }
    
    public int getCount_ExperiencedPlayers(){
        int count = 0;
        for( Player player : mPlayers)
            if (player.isPreviousExperience())
                count++;
        return count;
    }
    public int getCount_InexperiencedPlayers(){
        return mPlayers.size() - getCount_ExperiencedPlayers();
    }
}
