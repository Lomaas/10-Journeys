/**
 * 
 */
package com.main.activitys.domain;

/**
 * @author Simen
 *
 */
public class Msg {
	private String text;
	private int imageId = 0;
	private String username;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getImageId() {
		return imageId;
	}
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	
}
