package com.lucas.lexcontrol.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Size(max = 120)
    public String name;

    @NotBlank
    @Email
    @Size(max = 160)
    public String email;

    @NotBlank
    @Size(min = 8, max = 72)
    public String password;
}
