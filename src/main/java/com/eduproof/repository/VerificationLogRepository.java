package com.eduproof.repository;

import com.eduproof.model.VerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.eduproof.model.Certificate;

import java.util.List;

@Repository
public interface VerificationLogRepository extends JpaRepository<VerificationLog, Long> {
    List<VerificationLog> findAllByOrderByVerifiedAtDesc();
    void deleteByCertificate(Certificate certificate);
}
