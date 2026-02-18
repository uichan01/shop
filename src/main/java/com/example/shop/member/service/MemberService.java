package com.example.shop.member.service;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.dto.request.LoginRequest;
import com.example.shop.member.dto.request.SignUpRequest;
import com.example.shop.member.dto.request.UpdateRequest;
import com.example.shop.member.dto.response.MemberInfoResponse;

public interface MemberService {
    public void addMember(SignUpRequest request);

    public MemberInfoResponse getMember(String email);

    public void updateMember(String email, UpdateRequest request);

    public void deleteMember(String email);
}
