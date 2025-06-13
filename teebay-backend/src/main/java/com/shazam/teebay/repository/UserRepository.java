package com.shazam.teebay.repository;

import com.shazam.teebay.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserInfo, Long> {

    public UserInfo findByEmail(String username);
}
