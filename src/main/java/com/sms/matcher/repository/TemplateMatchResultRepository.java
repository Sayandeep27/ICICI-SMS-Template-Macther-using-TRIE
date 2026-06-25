package com.sms.matcher.repository;

import com.sms.matcher.model.TemplateMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateMatchResultRepository extends JpaRepository<TemplateMatchResult, Long> {
    List<TemplateMatchResult> findByTemplateId(Long templateId);
    Optional<TemplateMatchResult> findTopByOrderByIdDesc();
}
