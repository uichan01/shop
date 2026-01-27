package com.example.shop.member.service;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.dto.request.LoginRequest;
import com.example.shop.member.dto.request.SignUpRequest;
import com.example.shop.member.dto.response.MemberInfoResponse;
import com.example.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

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
        Role signUpUserRole = request.getRole();

        Boolean isExist = memberRepository.existsByEmail(email);

        if (isExist) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        if (signUpUserRole == Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("관리자 권한으로 가입할 수 없습니다.");
        }

        MemberEntity memberEntity = MemberEntity.builder()
                .name(name)
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .role(signUpUserRole)
                .build();

        memberRepository.save(memberEntity);

        return true;
    }

    @Override
    public MemberInfoResponse getMember(String email) {

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        MemberInfoResponse memberInfo = MemberInfoResponse.builder()
                .email(email)
                .name(member.getName())
                .role(member.getRole())
                .created_at(member.getCreatedAt())
                .build();

        return memberInfo;
    }

    @Override
    @Transactional
    public void deleteMember(String email) {
        long count = memberRepository.deleteByEmail(email);
        if(count == 0) {
            throw new NoSuchElementException("회원이 존재하지 않습니다.");
        }
    }
}
