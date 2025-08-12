package com.company.calendar.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Response object containing user details")
public final class GetUserResponse {
    @Schema(description = "User ID", example = "1")
    private final String id;

    @Schema(description = "Email address", example = "akash.singh@gmail.com")
    private final String email;

    @Schema(description = "Full name", example = "Akashdeep Singh")
    private final String name;
}
