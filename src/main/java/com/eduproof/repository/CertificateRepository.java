package com.eduproof.repository;

import com.eduproof.model.Certificate;
import com.eduproof.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByUniqueCertificateCodeIgnoreCase(String code);
    List<Certificate> findByStudentOrderByIssueDateDesc(Student student);
    List<Certificate> findAllByOrderByCertificateIdDesc();
    long countByStatus(String status);

    @Query("SELECT c FROM Certificate c WHERE LOWER(c.uniqueCertificateCode) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.student.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.student.rollNumber) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Certificate> search(@Param("q") String query);
}
