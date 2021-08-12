package com.jesperapps.email.api.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import com.jesperapps.email.api.message.UserRequestEntity;

@Entity
@Table(name = "EMP_EMPLOYEE_MASTER")
public class User extends AbstractAuditingEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Integer id;

	@Column(name = "FIRST_NAME")
	@Size(max = 100)
	private String firstName;

	@Column(name = "LAST_NAME")
	@Size(max = 100)
	private String lastName;

	@Column(name = "USER_NAME")
	@Size(max = 100)
	private String userName;

	@Column(name = "PASSWORD")
	@Size(max = 100)
	private String password;

	@Column(name = "KEY_VALUE", updatable = false)
	private String keyValue;

	@Column(name = "STATUS")
	private Integer status;

	private String contactNumber;
	private String token;
	private String extraEmail;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
	private EmailConfiguration emailConfiguration;

	@OneToMany(mappedBy = "user")
	private List<Email> email;

	@ManyToOne
	@JoinColumn
	private ListTypeValues listTypeValues;

	@OneToMany(mappedBy = "user")
	private List<Signature> signature;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "user")
//	@Transient
	private Attachment attachment;

	@Column
	private String smsotp;
	
	@Transient
	private String oldPassword;

	@Transient
	private String newPassword;
	@Transient
	private String adminPassword;

//	private Email email;

	public User() {

	}

	public User(UserRequestEntity userRequestEntity) {
		super();
		this.id = userRequestEntity.getId();
		this.keyValue = userRequestEntity.getKeyValue();
		this.firstName = userRequestEntity.getFirstName();
		this.lastName = userRequestEntity.getLastName();
		this.userName = userRequestEntity.getUserName();
		this.password = userRequestEntity.getPassword();
		this.contactNumber = userRequestEntity.getContactNumber();
		this.extraEmail = userRequestEntity.getExtraEmail();
		this.status = 1;
		this.emailConfiguration = userRequestEntity.getEmailConfiguration();
		this.emailConfiguration.setUser(this);
		this.listTypeValues = userRequestEntity.getListTypeValues();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm:ss a");
		LocalDateTime now = LocalDateTime.now();
		this.createDateTime = now;
		this.updateDateTime = now;
	}

	public User(Integer userId2) {
		this.id = userId2;
	}

//	public User(User user, User user2, Integer id) {
//		super();
//		this.id = user.getId();
//		this.firstName = user.getFirstName();
//		this.lastName = user.getLastName();
//		this.userName = user.getUserName();
//		this.password = user.getPassword();
//		this.keyValue = user.getKeyValue();
//		this.status = user.getStatus();
//		this.emailConfiguration = user.getEmailConfiguration();
//	}

	public User(User user) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.password = user.getPassword();
		this.status = user.getStatus();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.contactNumber = user.getContactNumber();
		this.token = user.getToken();
	}

	public User(User user, User user2) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.password = user.getPassword();
		this.status = user.getStatus();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.contactNumber = user.getContactNumber();
		this.token = user.getToken();
		this.listTypeValues = user.getListTypeValues();
		this.createDateTime = user.getCreateDateTime();
		this.extraEmail = user.getExtraEmail();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm:ss a");
		LocalDateTime now = LocalDateTime.now();
		this.updateDateTime = now;
	}

	public User(User user, User user2, User user3) {
		this.id = user.getId();
		this.userName = user.getUserName();
	}

	public User(UserRequestEntity user, UserRequestEntity userRequestEntity2) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.password = user.getPassword();
		this.status = user.getStatus();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.contactNumber = user.getContactNumber();
		this.token = user.getToken();
		this.createDateTime = user.getCreateDateTime();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm:ss a");
		LocalDateTime now = LocalDateTime.now();
		this.updateDateTime = now;
		this.listTypeValues = user.getListTypeValues();
	}

	public User(User user, UserRequestEntity userRequestEntity) {
		super();
		this.id = user.getId();
		this.userName = user.getUserName();
		this.password = user.getPassword();
		this.status = userRequestEntity.getStatus();
		this.firstName = userRequestEntity.getFirstName();
		this.lastName = userRequestEntity.getLastName();
		this.contactNumber = userRequestEntity.getContactNumber();
		this.token = user.getToken();
		this.email = user.getEmail();
		this.createDateTime = user.getCreateDateTime();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm:ss a");
		LocalDateTime now = LocalDateTime.now();
		this.updateDateTime = now;
//		this.attachment = user.getAttachment();
	}

	public User(User user, UserRequestEntity userRequestEntity, UserRequestEntity userRequestEntity2) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.password = user.getPassword();
		this.status = user.getStatus();
		this.firstName = userRequestEntity.getFirstName();
		this.lastName = userRequestEntity.getLastName();
		this.contactNumber = userRequestEntity.getContactNumber();
		this.token = user.getToken();
		this.email = user.getEmail();
		this.createDateTime = user.getCreateDateTime();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm:ss a");
		LocalDateTime now = LocalDateTime.now();
		this.updateDateTime = now;
		this.listTypeValues = user.getListTypeValues();
	}

	public User(User user, UserRequestEntity userRequestEntity, User user2) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.password = userRequestEntity.getPassword();
		this.status = user.getStatus();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.contactNumber = user.getContactNumber();
		this.token = user.getToken();
		this.email = user.getEmail();
		this.createDateTime = user.getCreateDateTime();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm:ss a");
		LocalDateTime now = LocalDateTime.now();
		this.updateDateTime = now;
		this.listTypeValues = user.getListTypeValues();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	public List<Email> getEmail() {
		return email;
	}

	public void setEmail(List<Email> email) {
		this.email = email;
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

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public List<Signature> getSignature() {
		return signature;
	}

	public void setSignature(List<Signature> signature) {
		this.signature = signature;
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
	
	

	public String getSmsotp() {
		return smsotp;
	}

	public void setSmsotp(String smsotp) {
		this.smsotp = smsotp;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", userName=" + userName
				+ ", password=" + password + "]";
	}

}
