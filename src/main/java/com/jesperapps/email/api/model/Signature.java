package com.jesperapps.email.api.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.joda.time.DateTime;

import com.jesperapps.email.api.message.SignatureRequestEntity;
import com.jesperapps.email.api.message.SignatureResponseEntity;

import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;

@Entity
@Table(name = "EMP_SIGNATURE")
public class Signature extends AbstractAuditingEntity implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Integer id;
	private String name;
	private String body;
	private Integer status;
	private String associateemail;

//	@OneToOne(fetch = FetchType.LAZY, optional = false)
//	@JoinColumn(name = "user_id", nullable = false)
//	private User user;
	

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false, updatable=false )
	private User user;

	public Signature() {

	}

	public Signature(SignatureRequestEntity signatureRequestEntity) {
		super();
		this.id = signatureRequestEntity.getId();
		this.name = signatureRequestEntity.getName();
		this.body = signatureRequestEntity.getBody();
		this.status = signatureRequestEntity.getStatus();
		this.associateemail = signatureRequestEntity.getAssociateemail();
		this.user = signatureRequestEntity.getUser();
//		LocalDateTime now = LocalDateTime.now();
//		String date = now.toString();
//		DateTime dt = new DateTime(date);
//		System.out.println("dt" + dt);
//		this.createdOn = dt;
//		this.updatedOn = dt;
	}

	public Signature(SignatureRequestEntity signatureRequestEntity, SignatureRequestEntity signatureRequestEntity1) {
		super();
		this.id = signatureRequestEntity.getId();
		this.name = signatureRequestEntity.getName();
		this.body = signatureRequestEntity.getBody();
		this.status = signatureRequestEntity.getStatus();
		this.associateemail = signatureRequestEntity.getAssociateemail();
		this.user = signatureRequestEntity.getUser();
//		LocalDateTime now = LocalDateTime.now();
//		String date = now.toString();
//		DateTime dt = new DateTime(date);
//		System.out.println("dt" + dt);
//		this.createdOn = dt;
//		this.updatedOn = dt;
	}

	public Signature(Signature signature) {
		super();
		this.id = signature.getId();
		this.name = signature.getName();
		this.body = signature.getBody();
		this.status = signature.getStatus();
		this.associateemail = signature.getAssociateemail();
		this.user = signature.getUser();
//		LocalDateTime now = LocalDateTime.now();
//		String date = now.toString();
//		DateTime dt = new DateTime(date);
//		System.out.println("dt" + dt);
//		this.createdOn = dt;
//		this.updatedOn = dt;
	}




	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getAssociateemail() {
		return associateemail;
	}

	public void setAssociateemail(String associateemail) {
		this.associateemail = associateemail;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Signature [id=" + id + ", name=" + name + ", body=" + body + ", status=" + status + ", associateemail="
				+ associateemail + ", user=" + user + "]";
	}

}
