package com.company.calendar.dto.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BookAppointmentResponseDto {
    @Schema(description = "Indicates if the appointment was booked successfully")
    private boolean success;

    @Schema(description = "Response message providing details about the booking result")
    private String message;

    @Schema(description = "Unique identifier of the booked appointment")
    private String appointmentId;

    @Schema(description = "Error code in case the booking request failed")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorCode;
}



