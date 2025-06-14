package com.shazam.teebay.repository;

import com.shazam.teebay.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserInfo, Long> {

    public Optional<UserInfo> findByEmail(String username);
}
