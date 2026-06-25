package com.sms.matcher;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Rebuilds the sms_templates table without a UNIQUE constraint on template_text.
 * This lets duplicate template bodies exist with different IDs, which the seed data needs.
 */
@Slf4j
@Component("smsTemplateSchemaMigration")
@RequiredArgsConstructor
public class SmsTemplateSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        Boolean tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM sqlite_master WHERE type='table' AND name='sms_templates')",
                Boolean.class);

        if (Boolean.FALSE.equals(tableExists)) {
            log.info("sms_templates table does not exist yet. Skipping schema migration.");
            return;
        }

        Boolean migrated = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM sqlite_master WHERE type='table' AND name='sms_templates_migrated')",
                Boolean.class);

        if (Boolean.TRUE.equals(migrated)) {
            log.info("sms_templates schema already migrated.");
            return;
        }

        log.info("Migrating sms_templates table to remove unique constraint on template_text...");

        jdbcTemplate.execute("PRAGMA foreign_keys=OFF");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sms_templates_new (
                    id INTEGER PRIMARY KEY,
                    template_text TEXT NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                INSERT INTO sms_templates_new (id, template_text)
                SELECT id, template_text FROM sms_templates ORDER BY id
                """);
        jdbcTemplate.execute("DROP TABLE sms_templates");
        jdbcTemplate.execute("ALTER TABLE sms_templates_new RENAME TO sms_templates");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sms_templates_migrated (
                    marker INTEGER PRIMARY KEY
                )
                """);
        jdbcTemplate.execute("INSERT INTO sms_templates_migrated (marker) VALUES (1)");
        jdbcTemplate.execute("PRAGMA foreign_keys=ON");

        log.info("sms_templates migration completed.");
    }
}
