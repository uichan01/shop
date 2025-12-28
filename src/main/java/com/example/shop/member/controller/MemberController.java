package com.example.shop.member.controller;

import com.example.shop.common.response.ApiResponse;
import com.example.shop.member.dto.request.SignUpRequest;
import com.example.shop.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    //회원가입
    @PostMapping("/sign-up")
    public ApiResponse<Void> signUp(@Valid SignUpRequest signUpRequest) {

        memberService.addMember(signUpRequest);
        return ApiResponse.success();
    }
    //로그인
    //정보조회
    //정보수정
    //탈퇴
}
