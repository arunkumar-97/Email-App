package com.jesperapps.email.api.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "EMP_LIST_TYPE_VALUES")
public class ListTypeValues extends AbstractAuditingEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "LIST_TYPE_VALUE_ID", unique = true, nullable = false, updatable = false)
	private Integer listTypeValueId;

	@Column(name = "LIST_TYPE_VALUE_NAME")
	@Size(max = 100)
	private String listTypeValueName;

	@Column(name = "DESCRIPTION")
	@Size(max = 250)
	private String description;

	private Integer status;

	@ManyToOne
	@JoinColumn
	private ListTypes listTypes;
	
	@OneToMany(mappedBy = "listTypeValues", cascade = CascadeType.ALL)
	private List<User> user;

	public ListTypeValues() {
		// TODO Auto-generated constructor stub
	}

	public ListTypeValues(Integer listTypeValueId2) {
		super();
		this.listTypeValueId = listTypeValueId2;
	}

	public ListTypeValues(ListTypeValues listTypeValues) {
		super();
		this.listTypeValueId = listTypeValues.getListTypeValueId();
		this.listTypeValueName = listTypeValues.getListTypeValueName();
		this.description = listTypeValues.getDescription();
		this.status = listTypeValues.getStatus();
	}

	public Integer getListTypeValueId() {
		return listTypeValueId;
	}

	public void setListTypeValueId(Integer listTypeValueId) {
		this.listTypeValueId = listTypeValueId;
	}

	public String getListTypeValueName() {
		return listTypeValueName;
	}

	public void setListTypeValueName(String listTypeValueName) {
		this.listTypeValueName = listTypeValueName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public ListTypes getListTypes() {
		return listTypes;
	}

	public void setListTypes(ListTypes listTypes) {
		this.listTypes = listTypes;
	}

}
