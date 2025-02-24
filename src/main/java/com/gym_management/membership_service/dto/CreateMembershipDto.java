package com.gym_management.membership_service.dto;

import com.gym_management.membership_service.model.MembershipType;

public record CreateMembershipDto(
        MembershipType type
) {
}
