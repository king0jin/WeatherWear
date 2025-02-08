package com.naver.et0709.weatherwear.repository;

import com.naver.et0709.weatherwear.entity.user_info;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<user_info, UUID> {
    boolean existsByEmail(String email);
}
