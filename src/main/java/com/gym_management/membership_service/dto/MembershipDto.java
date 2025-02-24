package com.gym_management.membership_service.dto;

import com.gym_management.membership_service.model.MembershipType;

import java.time.LocalDate;
import java.util.UUID;

public record MembershipDto(
        UUID uuid,
        MembershipType type,
        LocalDate startDate,
        LocalDate endDate
) {
}
