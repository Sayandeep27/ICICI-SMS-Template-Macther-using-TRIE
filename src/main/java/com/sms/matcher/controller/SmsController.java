package com.sms.matcher.controller;

import com.sms.matcher.model.SmsTemplate;
import com.sms.matcher.model.TemplateMatchResult;
import com.sms.matcher.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SmsController {

    private final TemplateService templateService;

    // ─────────────────────────────────────────────────
    // POST /api/template
    // Add a new template to DB and Trie
    //
    // Request body:
    // {
    //   "templateText": "Dear Customer,your OTP is {#var},valid for {#var} mins."
    // }
    // ─────────────────────────────────────────────────
    @PostMapping("/template")
    public ResponseEntity<SmsTemplate> addTemplate(@RequestBody Map<String, String> body) {
        String templateText = body.get("templateText");
        SmsTemplate saved = templateService.addTemplate(templateText);
        return ResponseEntity.ok(saved);
    }

    // ─────────────────────────────────────────────────
    // GET /api/templates
    // Get all templates from DB
    // ─────────────────────────────────────────────────
    @GetMapping("/templates")
    public ResponseEntity<List<SmsTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    // ─────────────────────────────────────────────────
    // POST /api/match
    // Send an incoming message → get matched template + extracted values
    //
    // Request body:
    // {
    //   "message": "Dear Customer,your OTP is 4532,valid for 2 mins."
    // }
    // ─────────────────────────────────────────────────
    @PostMapping("/match")
    public ResponseEntity<TemplateMatchResult> matchMessage(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        TemplateMatchResult result = templateService.processMessage(message);
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────
    // GET /api/results
    // Get all match results stored in DB
    // ─────────────────────────────────────────────────
    @GetMapping("/results")
    public ResponseEntity<List<TemplateMatchResult>> getAllResults() {
        return ResponseEntity.ok(templateService.getAllResults());
    }

    // ─────────────────────────────────────────────────
    // POST /api/rebuild-trie
    // Rebuild the Trie from DB (call after bulk template updates)
    // ─────────────────────────────────────────────────
    @PostMapping("/rebuild-trie")
    public ResponseEntity<String> rebuildTrie() {
        templateService.rebuildTrie();
        return ResponseEntity.ok("Trie rebuilt successfully.");
    }
}
