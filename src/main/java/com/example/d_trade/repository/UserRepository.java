package com.example.d_trade.repository;

import com.example.d_trade.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    Optional<User> findByStudentId(String studentId);
    
    boolean existsByStudentId(String studentId);
    
    Optional<User> findByUsername(String username);
} 