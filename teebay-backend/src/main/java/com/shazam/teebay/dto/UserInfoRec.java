package com.shazam.teebay.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserInfoRec
        (@NotBlank(message = "First name is required")
         String firstName,

         @NotBlank(message = "Last name is required")
         String lastName,

         @NotBlank(message = "Address is required")
         String address,

         @NotBlank(message = "Email is required")
         @Email(message = "Invalid email format")
         String email,

         @NotBlank(message = "Phone number is required")
         @Pattern(
                 regexp = "^(01)[0-9]{9}$",
                 message = "Phone number must be a valid 11-digit Bangladeshi number"
         )
         String phoneNumber,

         @NotBlank(message = "Password is required")
         @Size(min = 6, message = "Password must be at least 6 characters long")
         String password) {}
