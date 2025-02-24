package com.gym_management.membership_service.controller;

import com.gym_management.membership_service.dto.CreateMembershipDto;
import com.gym_management.membership_service.dto.MembershipDto;
import com.gym_management.membership_service.model.MembershipType;
import com.gym_management.membership_service.service.MembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public ResponseEntity<List<MembershipDto>> getAllMemberships() {
        List<MembershipDto> memberships = membershipService.getAll();
        return ResponseEntity.ok(memberships);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<MembershipDto> getMembershipById(@PathVariable UUID uuid) {
        MembershipDto membershipDto = membershipService.getById(uuid);
        return ResponseEntity.ok(membershipDto);
    }

    @PostMapping
    public ResponseEntity<UUID> createMembership(@RequestBody String membershipType) {
        CreateMembershipDto createMembershipDto = new CreateMembershipDto(MembershipType.valueOf(membershipType.toUpperCase()));
        MembershipDto createdMembership = membershipService.save(createMembershipDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMembership.uuid());
    }

    @PutMapping("/{uuid}/upgrade")
    public ResponseEntity<MembershipDto> upgradeMembership(@PathVariable UUID uuid) {
        MembershipDto upgradedMembership = membershipService.upgradeMembership(uuid);
        return ResponseEntity.status(HttpStatus.OK).body(upgradedMembership);
    }

    @PutMapping("/{uuid}/downgrade")
    public ResponseEntity<MembershipDto> downgradeMembership(@PathVariable UUID uuid) {
        MembershipDto downgradedMembership = membershipService.downgradeMembership(uuid);
        return ResponseEntity.status(HttpStatus.OK).body(downgradedMembership);
    }
}