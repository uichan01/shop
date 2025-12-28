package com.example.shop.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class SignInRequest {
    private String email;
    private String password;
}
