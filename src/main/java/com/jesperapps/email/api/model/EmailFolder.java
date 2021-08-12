package com.jesperapps.email.api.model;

import java.util.List;

import javax.mail.Folder;

import org.joda.time.DateTime;

public class EmailFolder {

	private String id;
	private String folderName;
	private boolean status;

	private DateTime createdOn;
	private DateTime updatedOn;
	
	private User user;
	
	private List<SubFolder> subFolder;
	
	
	
	public EmailFolder() {
		super();
	}

	public EmailFolder(Folder fd) {
		super();
		this.folderName = fd.getName();
		if(fd.isSubscribed()== true) {
			this.status = true;
		}else {
			this.status = false;
		}

	}

	public EmailFolder(Folder fd, List<SubFolder> subFolderList) {
		this.folderName = fd.getName();
		if(fd.isSubscribed()== true) {
			this.status = true;
		}else {
			this.status = false;
		}
		this.subFolder = subFolderList;
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
	public boolean getStatus() {
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
