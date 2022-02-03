package com.krish.empower.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static com.krish.empower.util.Utils.sanitize;

@Entity
@Table(name = "useraccess", schema="restful")
public class UserAccess extends BaseObject {
	private String userAccessCode;
	
	private String userAccessCodeDesc;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="userId")
	private User user;

	public String getUserAccessCode() {
		return sanitize(userAccessCode);
	}

	public void setUserAccessCode(String userAccessCode) {
		this.userAccessCode = userAccessCode;
	}

	public String getUserAccessCodeDesc() {
		return sanitize(userAccessCodeDesc);
	}

	public void setUserAccessCodeDesc(String userAccessCodeDesc) {
		this.userAccessCodeDesc = userAccessCodeDesc;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	
}
