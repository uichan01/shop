package com.example.shop.member.dto.response;

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
    private LocalDateTime created_at;
}
