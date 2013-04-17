package com.saimenstravelapp.activitys.domain;

public class Player {
	private String username = null;
	private int rank  = 0;
	private int wins  = 0;
	private int loss  = 0;
	private int score  = 0;
	private int averageScore = 0;
	private int imageId = 1;
	
	public int getImageId() {
		return imageId;
	}
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getWins() {
		return wins;
	}
	public void setWins(int wins) {
		this.wins = wins;
	}
	public int getLoss() {
		return loss;
	}
	public void setLoss(int loss) {
		this.loss = loss;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getAverageScore() {
		return averageScore;
	}
	public void setAverageScore(int averageScore) {
		this.averageScore = averageScore;
	}
	
	


}
