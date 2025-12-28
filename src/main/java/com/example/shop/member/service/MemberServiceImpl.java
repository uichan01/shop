package com.example.shop.member.service;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.dto.request.SignInRequest;
import com.example.shop.member.dto.request.SignUpRequest;
import com.example.shop.member.dto.response.SignInResponse;
import com.example.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Boolean addMember(SignUpRequest request) {
        String name = request.getName();
        String email = request.getEmail();
        String password = request.getPassword();

        Boolean isExist = memberRepository.existsByEmail(email);

        if (isExist) {
            return false;
        }

        MemberEntity memberEntity = MemberEntity.builder()
                .name(name)
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .role(Role.USER)
                .build();

        memberRepository.save(memberEntity);

        return true;
    }

    @Override
    public MemberEntity getMember(Long memberId) {
        return null;
    }

    @Override
    public Long deleteMember(Long memberId) {
        return 0L;
    }

    @Override
    public SignInResponse signIn(SignInRequest request) {
        return null;
    }
}
