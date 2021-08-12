package com.jesperapps.email.api.message;


import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.Size;

import com.jesperapps.email.api.model.Attachment;
import com.jesperapps.email.api.model.Email;
import com.jesperapps.email.api.model.EmailConfiguration;
import com.jesperapps.email.api.model.ListTypeValues;

public class UserRequestEntity {

	private Integer id;
	private String firstName;
	private String lastName;
	private String userName;
	private String password;
	private Integer status;
	private String keyValue;
	private EmailConfiguration emailConfiguration;
	private Email email;
	private ListTypeValues listTypeValues;
	
	private String contactNumber;
	private String token;
	private String extraEmail; 
	
	private String oldPassword;
	private String newPassword;
	private String adminPassword;

	private LocalDateTime createDateTime;
	private LocalDateTime updateDateTime;
	protected String createdBy;
	protected String lastUpdatedBy;

	private Attachment attachment;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public String setPassword(String password) {
		return this.password = password;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public EmailConfiguration getEmailConfiguration() {
		return emailConfiguration;
	}
	public void setEmailConfiguration(EmailConfiguration emailConfiguration) {
		this.emailConfiguration = emailConfiguration;
	}
	public Email getEmail() {
		return email;
	}
	public void setEmail(Email email) {
		this.email = email;
	}
	public String getKeyValue() {
		return keyValue;
	}
	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}
	public ListTypeValues getListTypeValues() {
		return listTypeValues;
	}
	public void setListTypeValues(ListTypeValues listTypeValues) {
		this.listTypeValues = listTypeValues;
	}
	public String getContactNumber() {
		return contactNumber;
	}
	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getExtraEmail() {
		return extraEmail;
	}
	public void setExtraEmail(String extraEmail) {
		this.extraEmail = extraEmail;
	}
	public LocalDateTime getCreateDateTime() {
		return createDateTime;
	}
	public void setCreateDateTime(LocalDateTime createDateTime) {
		this.createDateTime = createDateTime;
	}
	public LocalDateTime getUpdateDateTime() {
		return updateDateTime;
	}
	public void setUpdateDateTime(LocalDateTime updateDateTime) {
		this.updateDateTime = updateDateTime;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}
	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}
	public Attachment getAttachment() {
		return attachment;
	}
	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public String getAdminPassword() {
		return adminPassword;
	}
	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}


	
	
}
