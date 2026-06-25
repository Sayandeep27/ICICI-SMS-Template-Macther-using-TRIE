package com.sms.matcher.repository;

import com.sms.matcher.model.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {
    Optional<SmsTemplate> findTopByOrderByIdDesc();
}
