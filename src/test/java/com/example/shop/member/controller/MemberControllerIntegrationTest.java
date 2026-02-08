package com.example.shop.member.controller;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.dto.request.SignUpRequest;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.security.dto.CustomUserDetails;
import com.example.shop.security.dto.MemberDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;


    @BeforeEach
    void setup() {
        memberRepository.deleteAll();
    }

    private MemberEntity saveTestMember() {
        return memberRepository.save(
                MemberEntity.builder()
                        .name("테스트유저")
                        .email("test@example.com")
                        .password(passwordEncoder.encode("password@"))
                        .role(Role.ROLE_USER)
                        .build()
        );
    }

    private void setSecurityContext(MemberEntity member) {
        CustomUserDetails userDetails = new CustomUserDetails(
                new MemberDto(
                        member.getEmail(),
                        member.getPassword(),
                        member.getRole().name()
                )
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("일반유저 회원가입 테스트")
    void testSignUp() throws Exception {
        SignUpRequest req = new SignUpRequest(
                "user@example.com",
                "password@",
                "테스트유저",
                Role.ROLE_USER
        );

        mockMvc.perform(post("/member/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @DisplayName("셀러 회원가입 테스트")
    @Test
    void testSellerSignUp() throws Exception {
        SignUpRequest req = new SignUpRequest(
                "seller@example.com",
                "password@",
                "테스트셀러",
                Role.ROLE_SELLER
        );

        mockMvc.perform(post("/member/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("중복 이메일 회원가입 테스트")
    void testDuplicationSignUp() throws Exception {

        SignUpRequest req = new SignUpRequest(
                "test@example.com",
                "password@",
                "중복테스트유저",
                Role.ROLE_USER
        );

        mockMvc.perform(post("/member/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/member/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 정보 조회 테스트")
    void testGetMemberInfoSecurityContext() throws Exception {

        MemberEntity member = saveTestMember();
        setSecurityContext(member);

        mockMvc.perform(get("/member/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("테스트유저"));
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void testDeleteMember() throws Exception {

        MemberEntity member = saveTestMember();
        setSecurityContext(member);

        mockMvc.perform(delete("/member/my_account")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}