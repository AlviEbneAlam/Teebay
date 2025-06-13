package com.shazam.teebay.dto;

public record UserInfoRec
        (String firstName,
         String lastName,
         String address,
         String email,
         String phoneNumber,
         String password) {}
