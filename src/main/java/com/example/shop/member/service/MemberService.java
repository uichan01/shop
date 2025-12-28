package com.example.shop.member.service;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.dto.request.SignInRequest;
import com.example.shop.member.dto.request.SignUpRequest;
import com.example.shop.member.dto.response.SignInResponse;

public interface MemberService {
    public Boolean addMember(SignUpRequest request);

    public MemberEntity getMember(Long memberId);

    public Long deleteMember(Long memberId);

    public SignInResponse signIn(SignInRequest request);
}
