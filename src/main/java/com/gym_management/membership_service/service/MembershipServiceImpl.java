package com.gym_management.membership_service.service;

import com.gym_management.membership_service.dto.CreateMembershipDto;
import com.gym_management.membership_service.dto.MembershipDto;
import com.gym_management.membership_service.mapper.MembershipMapper;
import com.gym_management.membership_service.model.Membership;
import com.gym_management.membership_service.model.MembershipType;
import com.gym_management.membership_service.repository.MembershipRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;

    public MembershipServiceImpl(MembershipRepository membershipRepository, MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.membershipMapper = membershipMapper;
    }

    @Override
    public List<MembershipDto> getAll() {
        return membershipRepository.findAll().stream()
                .map(membershipMapper::membershipToDto)
                .toList();
    }

    @Override
    public MembershipDto getById(UUID uuid) {
        return membershipRepository.findById(uuid)
                .map(membershipMapper::membershipToDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found with id: " + uuid));
    }

    @Override
    @Transactional
    public MembershipDto save(CreateMembershipDto createMembershipDto) {
        Membership membership = membershipMapper.dtoToMembership(createMembershipDto);
        membership.setType(MembershipType.BASIC);

        LocalDate startDate = LocalDate.now();
        membership.setStartDate(startDate);
        membership.setEndDate(startDate.plusMonths(1));

        return membershipMapper.membershipToDto(membershipRepository.save(membership));
    }

    @Override
    public MembershipDto upgradeMembership(UUID uuid) {
        Membership membership = membershipRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found with id: " + uuid));
        if (membership.getType() == MembershipType.GOLD) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Membership with with id: " + uuid + " is already GOLD");
        }
        membership.setType(MembershipType.GOLD);
        return membershipMapper.membershipToDto(membershipRepository.save(membership));
    }

    @Override
    public MembershipDto downgradeMembership(UUID uuid) {
        Membership membership = membershipRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found with id: " + uuid));
        if (membership.getType() == MembershipType.BASIC) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Membership with with id: " + uuid + " is already BASIC");
        }
        membership.setType(MembershipType.BASIC);
        return membershipMapper.membershipToDto(membershipRepository.save(membership));
    }
}
