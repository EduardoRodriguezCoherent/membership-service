package com.gym_management.membership_service.mapper;

import com.gym_management.membership_service.dto.CreateMembershipDto;
import com.gym_management.membership_service.dto.MembershipDto;
import com.gym_management.membership_service.model.Membership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MembershipMapper {

    MembershipDto membershipToDto(Membership membership);

    @Mapping(target = "uuid", ignore = true)
    Membership dtoToMembership(CreateMembershipDto createMembershipDto);
}
