package com.gym_management.membership_service.service.impl;

import com.gym_management.membership_service.dto.CreateMembershipDto;
import com.gym_management.membership_service.dto.MembershipDto;
import com.gym_management.membership_service.mapper.MembershipMapper;
import com.gym_management.membership_service.model.Membership;
import com.gym_management.membership_service.model.MembershipAction;
import com.gym_management.membership_service.model.MembershipEvent;
import com.gym_management.membership_service.model.MembershipType;
import com.gym_management.membership_service.repository.MembershipRepository;
import com.gym_management.membership_service.service.MembershipService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MembershipServiceImpl implements MembershipService {

    private static final String MEMBERSHIP_NOT_FOUND = "Membership not found with id: ";
    private static final String MEMBERSHIP_TOPIC = "membership-topic";
    private static final String KAFKA_RESPONSE = "Kafka response: {}";
    private static final String EVENT = "Event: {}";
    private final MembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;
    private final StreamBridge streamBridge;

    public MembershipServiceImpl(MembershipRepository membershipRepository, MembershipMapper membershipMapper, StreamBridge streamBridge) {
        this.membershipRepository = membershipRepository;
        this.membershipMapper = membershipMapper;
        this.streamBridge = streamBridge;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND + uuid));
    }

    @Override
    @Transactional
    public MembershipDto save(CreateMembershipDto createMembershipDto) {
        Membership membershipToSave = membershipMapper.dtoToMembership(createMembershipDto);
        membershipToSave.setType(MembershipType.BASIC);

        LocalDate startDate = LocalDate.now();
        membershipToSave.setStartDate(startDate);
        membershipToSave.setEndDate(startDate.plusMonths(1));

        Membership savedMembership = membershipRepository.save(membershipToSave);

        // Build event
        MembershipEvent event = new MembershipEvent(
                savedMembership.getUuid(),
                MembershipAction.CREATE,
                savedMembership.getType(),
                "Welcome! Your membership has been created as BASIC."
        );

        // Send to Kafka
        log.info(EVENT, event);
        boolean kafkaResponse = streamBridge.send(MEMBERSHIP_TOPIC, event);
        log.info(KAFKA_RESPONSE, kafkaResponse);


        return membershipMapper.membershipToDto(savedMembership);
    }

    @Override
    @Transactional
    public MembershipDto upgradeMembership(UUID uuid) {
        Membership membership = membershipRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND + uuid));
        if (membership.getType() == MembershipType.GOLD) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Membership with with id: " + uuid + " is already GOLD");
        }
        membership.setType(MembershipType.GOLD);

        Membership upgraded = membershipRepository.save(membership);

        // Build event for membership upgrade
        MembershipEvent event = new MembershipEvent(
                upgraded.getUuid(),
                MembershipAction.UPGRADE,
                upgraded.getType(),
                "Congratulations! Your membership has been upgraded to GOLD."
        );

        log.info(EVENT, event);

        boolean kafkaResponse = streamBridge.send(MEMBERSHIP_TOPIC, event);
        log.info(KAFKA_RESPONSE, kafkaResponse);

        return membershipMapper.membershipToDto(upgraded);
    }

    @Override
    @Transactional
    public MembershipDto downgradeMembership(UUID uuid) {
        Membership membership = membershipRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MEMBERSHIP_NOT_FOUND + uuid));
        if (membership.getType() == MembershipType.BASIC) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Membership with with id: " + uuid + " is already BASIC");
        }
        membership.setType(MembershipType.BASIC);

        Membership downgraded = membershipRepository.save(membership);
        MembershipEvent event = new MembershipEvent(
                downgraded.getUuid(),
                MembershipAction.DOWNGRADE,
                downgraded.getType(),
                " Your membership has been downgraded to BASIC."
        );

        log.info(EVENT, event);
        boolean kafkaResponse = streamBridge.send(MEMBERSHIP_TOPIC, event);
        log.info(KAFKA_RESPONSE, kafkaResponse);

        return membershipMapper.membershipToDto(downgraded);
    }
}
