package com.example.shop.member.controller;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

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
        mockMvc.perform(post("/member/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "테스트유저")
                        .param("email", "test@example.com")
                        .param("password", "password@")
                        .param("role", "ROLE_USER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("셀러 회원가입 테스트")
    void testSellerSignUp() throws Exception {
        mockMvc.perform(post("/member/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "테스트셀러")
                        .param("email", "seller@example.com")
                        .param("password", "password@")
                        .param("role", "ROLE_SELLER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("중복이메일 회원가입 테스트")
    void testDuplicationSignUp() throws Exception {
        mockMvc.perform(post("/member/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "테스트유저")
                        .param("email", "test@example.com")
                        .param("password", "password@")
                        .param("role", "ROLE_USER"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/member/sign-up")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "테스트유저")
                        .param("email", "test@example.com")
                        .param("password", "password@")
                        .param("role", "ROLE_USER"))
                .andExpect(status().isBadRequest());
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