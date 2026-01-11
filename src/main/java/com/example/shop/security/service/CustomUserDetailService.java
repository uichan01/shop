package com.example.shop.security.service;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.security.dto.CustomUserDetails;
import com.example.shop.security.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberEntity member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다: " + username));

        MemberDto memberDto = MemberDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .role(member.getRole().toString())
                .build();

        return new CustomUserDetails(memberDto);
    }
}
