package com.eduproof.repository;

import com.eduproof.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmailIgnoreCase(String email);
    Optional<Student> findByRollNumberIgnoreCase(String rollNumber);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByRollNumberIgnoreCase(String rollNumber);
    List<Student> findByNameContainingIgnoreCaseOrRollNumberContainingIgnoreCase(String name, String rollNumber);
}
