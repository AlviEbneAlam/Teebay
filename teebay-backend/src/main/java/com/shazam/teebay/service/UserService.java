package com.shazam.teebay.service;

import com.shazam.teebay.dto.RegisterResponse;
import com.shazam.teebay.dto.UserInfoRec;
import com.shazam.teebay.entity.UserInfo;
import com.shazam.teebay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder
                       /*JwtService jwtService*/) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        // this.jwtService = jwtService;
    }

    public RegisterResponse register(UserInfoRec userInfoRec) {
        UserInfo user = new UserInfo();
        user.setFirstName(userInfoRec.firstName());
        user.setLastName(userInfoRec.lastName());
        user.setAddress(userInfoRec.address());
        user.setPhoneNumber(userInfoRec.phoneNumber());
        user.setEmail(userInfoRec.email());
        user.setPassword(passwordEncoder.encode(userInfoRec.password()));

        userRepository.save(user);

        return new RegisterResponse(user.getId(), user.getEmail(), user.getFirstName());
    }
}
