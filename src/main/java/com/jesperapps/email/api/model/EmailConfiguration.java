package com.jesperapps.email.api.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "EMP_EMAIL_CONFIGURATION")
public class EmailConfiguration extends AbstractAuditingEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "EMAIL_CONFIGURATION_ID")
	private Integer emailConfigurationId;

	@Column(name = "INCOMING_HOST")
	@Size(max = 100)
	private String incomingHost;

	@Column(name = "OUTGOING_HOST")
	@Size(max = 100)
	private String outgoingHost;

	@Column(name = "INCOMING_PORT")
	@Size(max = 100)
	private Integer incomingPort;

	@Column(name = "OUTGOING_PORT")
	@Size(max = 100)
	private Integer outgoingPort;

	@Column(name = "INCOMING_PROTOCOL")
	@Size(max = 100)
	private String incomingProtocol;

	@Column(name = "OUTGOING_PROTOCOL")
	@Size(max = 100)
	private String outgoingProtocol;

	@Column(name = "SECURITY")
	@Size(max = 100)
	private boolean security;

	@Column(name = "AUTHENTICATION")
	@Size(max = 100)
	private boolean authentication;
	
	
	 @OneToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "id", nullable = false)
	    private User user;
		

	public EmailConfiguration() {
		// TODO Auto-generated constructor stub
	}

	public EmailConfiguration(EmailConfiguration emailConfiguration) {
		super();
		this.emailConfigurationId = emailConfiguration.getEmailConfigurationId();
		this.incomingHost = emailConfiguration.getIncomingHost();
		this.outgoingHost = emailConfiguration.getOutgoingHost();
		this.incomingPort = emailConfiguration.getIncomingPort();
		this.outgoingPort = emailConfiguration.getOutgoingPort();
		this.incomingProtocol = emailConfiguration.getIncomingProtocol();
		this.outgoingProtocol = emailConfiguration.getOutgoingProtocol();
		this.security = emailConfiguration.isSecurity();
		this.authentication = emailConfiguration.isAuthentication();
	}

	public Integer getEmailConfigurationId() {
		return emailConfigurationId;
	}

	public void setEmailConfigurationId(Integer emailConfigurationId) {
		this.emailConfigurationId = emailConfigurationId;
	}

	public String getIncomingHost() {
		return incomingHost;
	}

	public void setIncomingHost(String incomingHost) {
		this.incomingHost = incomingHost;
	}

	public String getOutgoingHost() {
		return outgoingHost;
	}

	public void setOutgoingHost(String outgoingHost) {
		this.outgoingHost = outgoingHost;
	}

	public Integer getIncomingPort() {
		return incomingPort;
	}

	public void setIncomingPort(Integer incomingPort) {
		this.incomingPort = incomingPort;
	}

	public Integer getOutgoingPort() {
		return outgoingPort;
	}

	public void setOutgoingPort(Integer outgoingPort) {
		this.outgoingPort = outgoingPort;
	}

	public String getIncomingProtocol() {
		return incomingProtocol;
	}

	public void setIncomingProtocol(String incomingProtocol) {
		this.incomingProtocol = incomingProtocol;
	}

	public String getOutgoingProtocol() {
		return outgoingProtocol;
	}

	public void setOutgoingProtocol(String outgoingProtocol) {
		this.outgoingProtocol = outgoingProtocol;
	}



	public boolean isSecurity() {
		return security;
	}

	public void setSecurity(boolean security) {
		this.security = security;
	}

	public boolean isAuthentication() {
		return authentication;
	}

	public void setAuthentication(boolean authentication) {
		this.authentication = authentication;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "EmailConfiguration [emailConfigurationId=" + emailConfigurationId + ", incomingHost=" + incomingHost
				+ ", outgoingHost=" + outgoingHost + ", incomingPort=" + incomingPort + ", outgoingPort=" + outgoingPort
				+ ", incomingProtocol=" + incomingProtocol + ", outgoingProtocol=" + outgoingProtocol + ", security="
				+ security + ", authentication=" + authentication + "]";
	}

}
