package com.sms.matcher.service;

import com.sms.matcher.model.SmsTemplate;
import com.sms.matcher.model.TemplateMatchResult;
import com.sms.matcher.repository.SmsTemplateRepository;
import com.sms.matcher.repository.TemplateMatchResultRepository;
import com.sms.matcher.trie.MatchResult;
import com.sms.matcher.trie.TemplateTrie;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@DependsOn("smsTemplateSchemaMigration")
@RequiredArgsConstructor
public class TemplateService {

    private final SmsTemplateRepository templateRepository;
    private final TemplateMatchResultRepository matchResultRepository;

    // The Trie — built once at startup, held in memory
    private TemplateTrie trie;

    // ─────────────────────────────────────────────────────────────
    // STARTUP: Load all templates from DB and build the Trie
    // ─────────────────────────────────────────────────────────────

    /**
     * Called automatically when Spring Boot starts.
     * Loads ALL templates from DB and inserts them into the Trie.
     * This happens ONCE. After this the Trie is ready for matching.
     */
    @PostConstruct
    public void buildTrie() {
        log.info("Building Trie from templates in DB...");

        trie = new TemplateTrie();

        List<SmsTemplate> templates = templateRepository.findAll();

        for (SmsTemplate template : templates) {
            trie.insert(template);
            log.info("Inserted template ID={} into Trie", template.getId());
        }

        log.info("Trie built successfully with {} templates.", templates.size());
    }

    // ─────────────────────────────────────────────────────────────
    // MATCH: Process an incoming message
    // ─────────────────────────────────────────────────────────────

    /**
     * Takes an incoming message, runs it through the Trie,
     * and saves the result (templateId + values) to DB.
     *
     * Example:
     *   input  = "Dear Customer,your OTP is 4532,valid for 2 mins."
     *   output = TemplateMatchResult { templateId=1, extractedValues="4532,2" }
     */
    public TemplateMatchResult processMessage(String message) {
        log.info("Processing message: {}", message);

        // Run message through Trie
        MatchResult matchResult = trie.match(message);

        if (!matchResult.isMatched()) {
            log.warn("No template matched for message: {}", message);
            throw new RuntimeException("No matching template found for the given message.");
        }

        log.info("Matched Template ID={}, Values={}",
                matchResult.getTemplateId(),
                matchResult.getExtractedValues());

        // Join extracted values as comma-separated string for storage
        // Example: ["4532", "2"] → "4532,2"
        String valuesAsString = String.join(",", matchResult.getExtractedValues());

        // Save result to DB
        TemplateMatchResult result = new TemplateMatchResult();
        result.setId(nextMatchResultId());
        result.setTemplateId(matchResult.getTemplateId());
        result.setIncomingMessage(message);
        result.setExtractedValues(valuesAsString);

        return matchResultRepository.save(result);
    }

    // ─────────────────────────────────────────────────────────────
    // TEMPLATE MANAGEMENT
    // ─────────────────────────────────────────────────────────────

    /**
     * Adds a new template to DB and also inserts it into the Trie immediately.
     * No restart needed.
     */
    public SmsTemplate addTemplate(String templateText) {
        SmsTemplate template = new SmsTemplate();
        template.setId(nextTemplateId());
        template.setTemplateText(templateText);
        SmsTemplate saved = templateRepository.save(template);

        // Insert into live Trie immediately
        trie.insert(saved);
        log.info("New template added and inserted into Trie: ID={}", saved.getId());

        return saved;
    }

    /**
     * Returns all templates stored in DB.
     */
    public List<SmsTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    /**
     * Returns all match results stored in DB.
     */
    public List<TemplateMatchResult> getAllResults() {
        return matchResultRepository.findAll();
    }

    /**
     * Rebuilds the Trie from scratch from DB.
     * Call this if templates are updated or deleted.
     */
    public void rebuildTrie() {
        log.info("Rebuilding Trie...");
        buildTrie();
        log.info("Trie rebuilt.");
    }

    private long nextTemplateId() {
        return templateRepository.findTopByOrderByIdDesc()
                .map(SmsTemplate::getId)
                .orElse(0L) + 1L;
    }

    private long nextMatchResultId() {
        return matchResultRepository.findTopByOrderByIdDesc()
                .map(TemplateMatchResult::getId)
                .orElse(0L) + 1L;
    }
}
