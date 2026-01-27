package com.example.shop.member.dto.response;

import com.example.shop.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
public class MemberInfoResponse {
    private String email;
    private String name;
    private Role role;
    private LocalDateTime created_at;
}
