package com.sms.matcher;

import com.sms.matcher.model.TemplateMatchResult;
import com.sms.matcher.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runs one sample match automatically at startup.
 * Change sourceMessage whenever you want to test a different SMS.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class StartupMatcherRunner implements CommandLineRunner {

    private final TemplateService templateService;

    @Override
    public void run(String... args) {
        String sourceMessage = "Dear Customer,your OTP is 4532,valid for 2 mins.";

        try {
            TemplateMatchResult result = templateService.processMessage(sourceMessage);
            log.info("Startup match result: templateId={}, values={}, message={}",
                    result.getTemplateId(),
                    result.getExtractedValues(),
                    result.getIncomingMessage());
        } catch (Exception ex) {
            log.error("Startup match failed for message: {}", sourceMessage, ex);
        }
    }
}
