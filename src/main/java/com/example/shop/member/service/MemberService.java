package com.example.shop.member.service;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.dto.request.LoginRequest;
import com.example.shop.member.dto.request.SignUpRequest;
import com.example.shop.member.dto.response.MemberInfoResponse;

public interface MemberService {
    public Boolean addMember(SignUpRequest request);

    public MemberInfoResponse getMember(String email);

    public void deleteMember(String email);
}
