package com.shazam.teebay.controller;

import com.shazam.teebay.dto.RegisterResponse;
import com.shazam.teebay.dto.UserInfoRec;
import com.shazam.teebay.service.UserService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserController {

    private final UserService authService;

    public UserController(UserService authService) {
        this.authService = authService;
    }

    @MutationMapping
    public RegisterResponse register(@Argument @Valid  UserInfoRec userInfo) {
        return authService.register(userInfo);
    }
}
