package com.gym_management.membership_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembershipEvent {
    private UUID uuid;
    private MembershipAction action;
    private MembershipType type;
    private String message;
}
