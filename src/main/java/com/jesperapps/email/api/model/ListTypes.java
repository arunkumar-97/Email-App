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
@Table(name = "EMP_LIST_TYPES")
public class ListTypes extends AbstractAuditingEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "LIST_TYPE_ID", unique = true , nullable = false, updatable = false)
	private Integer listTypeId;
	
	@Column(name = "LIST_TYPE_NAME")
	@Size(max = 100)
	private String listTypeName;
	
	@Column(name = "DESCRIPTION")
	@Size(max = 250)
	private String description;

	@Column(name = "STATUS")
	private Integer status;

	@OneToMany(mappedBy = "listTypes", cascade = CascadeType.ALL)
	private List<ListTypeValues> listTypeValues;

	public ListTypes() {
		super();
	}

	public ListTypes(Integer listTypesId) {
		this.listTypeId = listTypesId;
	}

	public Integer getListTypeId() {
		return listTypeId;
	}

	public void setListTypeId(Integer listTypeId) {
		this.listTypeId = listTypeId;
	}

	public String getListTypeName() {
		return listTypeName;
	}

	public void setListTypeName(String listTypeName) {
		this.listTypeName = listTypeName;
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

	public List<ListTypeValues> getListTypeValues() {
		return listTypeValues;
	}

	public void setListTypeValues(List<ListTypeValues> listTypeValues) {
		this.listTypeValues = listTypeValues;
	}

}
