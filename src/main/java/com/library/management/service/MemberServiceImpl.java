package com.library.management.service;

import com.library.management.dto.MemberDto;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.model.Member;
import com.library.management.model.MemberStatus;
import com.library.management.model.Role;
import com.library.management.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberDto createMember(MemberDto memberDto) {
        if (memberRepository.existsByEmail(memberDto.getEmail())) {
            throw new BadRequestException("Member with email '" + memberDto.getEmail() + "' already exists.");
        }

        String code = memberDto.getMemberCode();
        if (code == null || code.trim().isEmpty()) {
            code = "LIB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else if (memberRepository.existsByMemberCode(code)) {
            throw new BadRequestException("Member with code '" + code + "' already exists.");
        }

        Member member = Member.builder()
                .memberCode(code)
                .name(memberDto.getName())
                .email(memberDto.getEmail())
                .phone(memberDto.getPhone())
                .role(memberDto.getRole() != null ? memberDto.getRole() : Role.STUDENT)
                .status(memberDto.getStatus() != null ? memberDto.getStatus() : MemberStatus.ACTIVE)
                .build();

        Member savedMember = memberRepository.save(member);
        return mapToDto(savedMember);
    }

    @Override
    public MemberDto updateMember(Long id, MemberDto memberDto) {
        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        if (!existingMember.getEmail().equalsIgnoreCase(memberDto.getEmail())
                && memberRepository.existsByEmail(memberDto.getEmail())) {
            throw new BadRequestException("Member with email '" + memberDto.getEmail() + "' already exists.");
        }

        existingMember.setName(memberDto.getName());
        existingMember.setEmail(memberDto.getEmail());
        existingMember.setPhone(memberDto.getPhone());
        if (memberDto.getRole() != null) {
            existingMember.setRole(memberDto.getRole());
        }
        if (memberDto.getStatus() != null) {
            existingMember.setStatus(memberDto.getStatus());
        }

        Member updatedMember = memberRepository.save(existingMember);
        return mapToDto(updatedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDto getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return mapToDto(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDto getMemberByCode(String memberCode) {
        Member member = memberRepository.findByMemberCode(memberCode)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with code: " + memberCode));
        return mapToDto(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberDto> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberDto> searchMembers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMembers();
        }
        return memberRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrMemberCodeContainingIgnoreCase(
                        query, query, query)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        memberRepository.delete(member);
    }

    private MemberDto mapToDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .memberCode(member.getMemberCode())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .role(member.getRole())
                .status(member.getStatus())
                .registeredAt(member.getRegisteredAt())
                .build();
    }
}
