package com.example.shop.member.service;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.dto.request.LoginRequest;
import com.example.shop.member.dto.request.SignUpRequest;

public interface MemberService {
    public Boolean addMember(SignUpRequest request);

    public MemberEntity getMember(Long memberId);

    public Long deleteMember(Long memberId);

    public boolean signIn(LoginRequest request);
}
