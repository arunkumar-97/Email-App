package com.jesperapps.email.api.message;

import java.util.List;

import org.joda.time.DateTime;

import com.jesperapps.email.api.model.SubFolder;
import com.jesperapps.email.api.model.User;

public class FolderRequestEntity {

	private String id;
	private String folderName;
	private boolean status;

	private DateTime createdOn;
	private DateTime updatedOn;
	
	private User user;
	
	private List<SubFolder> subFolder;
	
	
	
	

	public FolderRequestEntity() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public DateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(DateTime updatedOn) {
		this.updatedOn = updatedOn;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<SubFolder> getSubFolder() {
		return subFolder;
	}

	public void setSubFolder(List<SubFolder> subFolder) {
		this.subFolder = subFolder;
	}

	
	
}
