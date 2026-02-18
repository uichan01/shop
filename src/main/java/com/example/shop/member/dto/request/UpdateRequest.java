package com.example.shop.member.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class UpdateRequest {
    @NotNull
    @Pattern(regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{7,}$",
            message = "비밀번호는 특수기호가 적어도 1개 포함된 7자 이상의 비밀번호여야 합니다.")
    private String password;

    @NotNull
    private String name;
}
