package com.company.calendar.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Response object containing user details")
public class GetUserResponse {
    @Schema(description = "User ID", example = "1")
    private String id;

    @Schema(description = "Email address", example = "akash.singh@gmail.com")
    private String email;

    @Schema(description = "Full name", example = "Akashdeep Singh")
    private String name;
}
