package com.library.management.service;

import com.library.management.dto.MemberDto;

import java.util.List;

public interface MemberService {
    MemberDto createMember(MemberDto memberDto);
    MemberDto updateMember(Long id, MemberDto memberDto);
    MemberDto getMemberById(Long id);
    MemberDto getMemberByCode(String memberCode);
    List<MemberDto> getAllMembers();
    List<MemberDto> searchMembers(String query);
    void deleteMember(Long id);
}
