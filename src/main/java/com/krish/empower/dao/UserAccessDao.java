package com.krish.empower.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krish.empower.entity.UserAccess;

public interface UserAccessDao extends JpaRepository<UserAccess, Long> {
	List<UserAccess> findByUserId(long userId);
}
