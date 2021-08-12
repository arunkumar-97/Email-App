package com.jesperapps.email.api.model;

import javax.mail.Folder;

public class SubFolder {
	private String subFolderName;
	private boolean status;
	private User user;
	
	private EmailFolder folder;

	public SubFolder() {

	}

//	public SubFolder(String name) {
//		super();
//		this.subFolderName = name;
//		this.status = status;
//		this.user = user;
//	}



	public SubFolder(Folder folder2) {
		this.subFolderName = folder2.getName();
		if(folder2.isSubscribed() == true) {
			this.status = true;
		}else {
			this.status = false;
		}
	}

	public String getSubFolderName() {
		return subFolderName;
	}

	public void setSubFolderName(String subFolderName) {
		this.subFolderName = subFolderName;
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public EmailFolder getFolder() {
		return folder;
	}

	public void setFolder(EmailFolder folder) {
		this.folder = folder;
	}



}
