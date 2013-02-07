/**
 * 
 */
package com.main.activitys.domain;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;

/**
 * @author Simen
 * Game object that holds information about the game
 */
public class Game {
	
	private int gameId;
	private ArrayList<String> userNameOpponent;
	private String lastAction;
	private String timeSinceLastMove;
	private int state;
	private int openCard;
	private int playersTurn;
	private JSONArray yourCards;
	private JSONArray openCards;
	private JSONArray openCardParents;

	private int opponentId;
	private int type;
	private int finished = 0;
	private String dateCreated;
	private int imageId = 0;
	private int score = 0;
	
	
	public int getImageId() {
		return imageId;
	}

	public void setImageId(int image) {
		this.imageId = image;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public int getScore() {
		return score;
	}

	public void setScore(int type) {
		this.score = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	public int getPlayersTurn() {
		return playersTurn;
	}
	
	public JSONArray getOpenCardParents() {
		return openCardParents;
	}

	public void setOpenCardParents(JSONArray openCardParents) {
		this.openCardParents = openCardParents;
	}


	public void setPlayersTurn(int playersTurn) {
		this.playersTurn = playersTurn;
	}

	public ArrayList<String> getUserNameOpponent() {
		return userNameOpponent;
	}

	public void setUserNameOpponent(ArrayList<String> userNameOpponent) {
		this.userNameOpponent = userNameOpponent;
	}

	public int getOpponentId() {
		return opponentId;
	}

	public void setOpponentId(int opponentId) {
		this.opponentId = opponentId;
	}
	
	public int isFinished() {
		return finished;
	}

	public void setFinished(int finished) {
		this.finished = finished;
	}

	public JSONArray getOpenCards() {
		return openCards;
	}

	public void setOpenCards(JSONArray openCards) {
		this.openCards = openCards;
	}

	public JSONArray getYourCards() {
		return yourCards;
	}

	public void setYourCards(JSONArray jsonArray) {
		this.yourCards = jsonArray;
	}

	public int getOpenCard() {
		return openCard;
	}

	public void setOpenCard(int unopenCard) {
		this.openCard = unopenCard;
	}

	public Game(){
		this.userNameOpponent = new ArrayList<String>();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getGameId(){
		return gameId;
	}
	
	public void setGameId(int gameid){
		this.gameId = gameid;
	}
	
	public ArrayList<String> getOpponentsUsername(){
		return userNameOpponent;
	}
	
	public void setOpponentsUsername(String username){
		this.userNameOpponent.add(username);
	}
	
	public String getLastAction(){
		return this.lastAction;
	}
	
	public void setLastAction(String lastAction){
		this.lastAction = lastAction;
	}
	
	public void setTimeSinceLastMove(String lastUpdate){
		this.timeSinceLastMove = lastUpdate;
	}
	
	public String getTimeSinceLastMove(){
		return this.timeSinceLastMove;
	}
	
}
