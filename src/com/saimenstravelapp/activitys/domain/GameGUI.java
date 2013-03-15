/**
 * 
 */
package com.saimenstravelapp.activitys.domain;

/**
 * @author Simen
 *
 */
public class GameGUI {
	int resourceId;
	int index;
	int currentCardId;
	int deletedCardId;
	boolean draggable;
	boolean isUpdated = false;
	boolean isSpecialCase = false;

	public GameGUI(int resourceId, int index, int cardId, boolean isUpdated, int deletedCardId, boolean isDraggable){
		this.resourceId = resourceId;
		this.index = index;
		this.currentCardId = cardId;
		this.isUpdated = isUpdated;
		this.deletedCardId = deletedCardId;
		this.draggable = isDraggable;
	}
	
	public boolean isSpecialCase() {
		return isSpecialCase;
	}

	public void setSpecialCase(boolean isSpecialCase) {
		this.isSpecialCase = isSpecialCase;
	}
	
	public boolean isUpdated() {
		return isUpdated;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}

	public int getDeletedCardId() {
		return deletedCardId;
	}

	public void setDeletedCardId(int deletedCardId) {
		this.deletedCardId = deletedCardId;
	}

	public int getResourceId() {
		return resourceId;
	}
	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getCurrentCardId() {
		return currentCardId;
	}
	public void setCurrentCardId(int cardId) {
		this.currentCardId = cardId;
	}
	
	public boolean isDraggable() {
		return draggable;
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}
}
