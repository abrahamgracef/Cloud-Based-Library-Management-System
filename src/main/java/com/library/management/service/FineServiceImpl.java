package com.library.management.service;

import com.library.management.dto.FineDto;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.model.Fine;
import com.library.management.model.FineStatus;
import com.library.management.repository.FineRepository;
import com.library.management.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public FineServiceImpl(FineRepository fineRepository, MemberRepository memberRepository) {
        this.fineRepository = fineRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FineDto> getFinesByMemberId(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }
        return fineRepository.findByMemberId(memberId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FineDto> getUnpaidFinesByMemberId(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }
        return fineRepository.findByMemberIdAndStatus(memberId, FineStatus.UNPAID).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FineDto> getAllFines() {
        return fineRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public FineDto payFine(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found with id: " + fineId));

        if (fine.getStatus() == FineStatus.PAID) {
            throw new BadRequestException("Fine with id " + fineId + " has already been paid.");
        }

        fine.setStatus(FineStatus.PAID);
        fine.setPaidAt(LocalDateTime.now());

        Fine updatedFine = fineRepository.save(fine);
        return mapToDto(updatedFine);
    }

    @Override
    @Transactional(readOnly = true)
    public FineDto getFineById(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found with id: " + fineId));
        return mapToDto(fine);
    }

    private FineDto mapToDto(Fine fine) {
        return FineDto.builder()
                .id(fine.getId())
                .memberId(fine.getMember().getId())
                .memberName(fine.getMember().getName())
                .memberCode(fine.getMember().getMemberCode())
                .borrowRecordId(fine.getBorrowRecord() != null ? fine.getBorrowRecord().getId() : null)
                .bookTitle(fine.getBorrowRecord() != null && fine.getBorrowRecord().getBook() != null
                        ? fine.getBorrowRecord().getBook().getTitle() : null)
                .amount(fine.getAmount())
                .status(fine.getStatus())
                .paidAt(fine.getPaidAt())
                .reason(fine.getReason())
                .build();
    }
}
