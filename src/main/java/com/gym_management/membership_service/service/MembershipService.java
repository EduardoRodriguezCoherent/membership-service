package com.gym_management.membership_service.service;

import com.gym_management.membership_service.dto.CreateMembershipDto;
import com.gym_management.membership_service.dto.MembershipDto;

import java.util.List;
import java.util.UUID;

public interface MembershipService {

    List<MembershipDto> getAll();

    MembershipDto getById(UUID uuid);

    MembershipDto save(CreateMembershipDto createMembershipDto);

    MembershipDto upgradeMembership(UUID uuid);

    MembershipDto downgradeMembership(UUID uuid);
}
