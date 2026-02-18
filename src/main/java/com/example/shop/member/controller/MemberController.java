package com.example.shop.member.controller;

import com.example.shop._common.response.ApiResponse;
import com.example.shop.member.dto.request.SignUpRequest;
import com.example.shop.member.dto.request.UpdateRequest;
import com.example.shop.member.dto.response.MemberInfoResponse;
import com.example.shop.member.service.MemberService;
import com.example.shop.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    //일반유저 회원가입
    @PostMapping("/sign-up")
    public ApiResponse<Void> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {

        memberService.addMember(signUpRequest);
        return ApiResponse.success();
    }

    //정보조회
    @GetMapping("/info")
    public ApiResponse<MemberInfoResponse> getMemberInfo(@AuthenticationPrincipal CustomUserDetails currentUser) {

        MemberInfoResponse memberInfo = memberService.getMember(currentUser.getUsername());
        return ApiResponse.success(memberInfo);
    }

    //정보수정
    @PutMapping("/info")
    public ApiResponse<Void> updateMemberInfo(@AuthenticationPrincipal CustomUserDetails currentUser,
                                              @Valid @RequestBody UpdateRequest request) {

        memberService.updateMember(currentUser.getUsername(), request);
        return ApiResponse.success();
    }

    //탈퇴
    @DeleteMapping("/my_account")
    public ApiResponse<Void> deleteMember(@AuthenticationPrincipal CustomUserDetails currentUser) {

        memberService.deleteMember(currentUser.getUsername());
        return ApiResponse.success();
    }
}
