package com.sms.matcher;

import com.sms.matcher.model.SmsTemplate;
import com.sms.matcher.repository.SmsTemplateRepository;
import com.sms.matcher.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the DB with your 3 sample templates on first startup.
 * If templates already exist, does nothing.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final SmsTemplateRepository templateRepository;
    private final TemplateService templateService;

    @Override
    public void run(String... args) {

        // Only seed if DB is empty
        if (templateRepository.count() > 0) {
            log.info("Templates already exist in DB. Skipping seed.");
            return;
        }

        log.info("Seeding initial templates into DB...");

        List<SmsTemplate> templates = List.of(
            new SmsTemplate(1L,
                "Dear Customer,your OTP is {#var},valid for {#var} mins."),
            new SmsTemplate(2L,
                "Amount Rs {#var} debite to your account"),
            new SmsTemplate(3L,
                "Amount Rs {#var} debited to your account.Your available balance is Rs {#var}")
        );

        templateRepository.saveAll(templates);
        templateService.rebuildTrie();

        log.info("Seeded {} templates.", templates.size());
    }
}
