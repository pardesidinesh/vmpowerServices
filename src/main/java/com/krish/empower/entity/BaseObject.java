package com.krish.empower.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseObject implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	private Long id;
	private String createdBy;
	private String modifiedBy;
	@CreatedDate
	private Date createdDtm;
	@LastModifiedDate
	private Date modifiedDtm;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Date getCreatedDtm() {
		return createdDtm;
	}
	public void setCreatedDtm(Date createdDtm) {
		this.createdDtm = createdDtm;
	}
	public Date getModifiedDtm() {
		return modifiedDtm;
	}
	public void setModifiedDtm(Date modifiedDtm) {
		this.modifiedDtm = modifiedDtm;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime *result +((id==null)?0:id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( this== obj)
			return true;
		if( obj == null )
			return false;
		if(getClass() != obj.getClass())
			return false;
		BaseObject other  = (BaseObject) obj;
		if(id==null) {
			if(other.id != null)
				return false;
		}else if(!id.equals(other.id)){
			return false;
		}
		return true;
	}
	
}
