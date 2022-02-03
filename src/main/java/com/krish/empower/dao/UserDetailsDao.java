package com.krish.empower.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.krish.empower.entity.User;

public interface UserDetailsDao extends JpaRepository<User, Long>{
	
	Optional<User> findByUserName(String userName);
	
	Optional<User> findByUuid(String uuid);
	
	@Query("select u from User u where u.email = :email")
	List<User> findByEmail(@Param("email") String email);
	
	
}
