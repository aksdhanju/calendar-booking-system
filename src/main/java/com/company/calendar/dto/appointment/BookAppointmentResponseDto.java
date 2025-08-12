package com.company.calendar.dto.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class BookAppointmentResponseDto {
    @Schema(description = "Indicates if the appointment was booked successfully")
    private final boolean success;

    @Schema(description = "Response message providing details about the booking result")
    private final String message;

    @Schema(description = "Unique identifier of the booked appointment")
    private final String appointmentId;

    @Schema(description = "Error code in case the booking request failed")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String errorCode;
}



