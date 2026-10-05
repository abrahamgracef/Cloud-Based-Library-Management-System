package com.library.management.repository;

import com.library.management.model.Member;
import com.library.management.model.MemberStatus;
import com.library.management.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberCode(String memberCode);

    Optional<Member> findByEmail(String email);

    boolean existsByMemberCode(String memberCode);

    boolean existsByEmail(String email);

    List<Member> findByRole(Role role);

    List<Member> findByStatus(MemberStatus status);

    long countByStatus(MemberStatus status);

    List<Member> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrMemberCodeContainingIgnoreCase(
            String name, String email, String memberCode
    );
}
