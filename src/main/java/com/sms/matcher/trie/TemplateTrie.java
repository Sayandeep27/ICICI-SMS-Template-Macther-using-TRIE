package com.sms.matcher.trie;

import com.sms.matcher.model.SmsTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * THE CORE ENGINE.
 *
 * This class does two things:
 *
 * 1. INSERT — At startup, takes all templates from DB and builds a Trie tree.
 *    Each character of the template becomes a node.
 *    When {#var} is found, a WILDCARD node is placed instead.
 *
 * 2. MATCH — When a message arrives, walks the message through the Trie
 *    character by character. At WILDCARD nodes, it skips message characters
 *    and captures them as variable values. At the end, returns templateId
 *    and the list of captured values.
 */
public class TemplateTrie {

    private static final String VAR_PLACEHOLDER = "{#var}";

    // The root of the Trie — empty starting node
    private final TrieNode root = new TrieNode();

    // ─────────────────────────────────────────────────────────────
    // INSERT: Build the Trie from a template
    // ─────────────────────────────────────────────────────────────

    /**
     * Inserts one template into the Trie.
     *
     * Example template: "Dear Customer,your OTP is {#var},valid for {#var} mins."
     *
     * Walk:
     *   D → e → a → r → ... → s → [WILDCARD] → , → v → ... → [WILDCARD] → m → i → n → s → . → LEAF(id=1)
     */
    public void insert(SmsTemplate template) {
        TrieNode current = root;
        String text = template.getTemplateText();
        int i = 0;

        while (i < text.length()) {

            // Check if current position starts with {#var}
            if (text.startsWith(VAR_PLACEHOLDER, i)) {

                // Place a WILDCARD node here if not already present
                if (current.wildcardChild == null) {
                    current.wildcardChild = new TrieNode();
                }
                current = current.wildcardChild;

                // Skip past the {#var} in the template text
                i += VAR_PLACEHOLDER.length();

            } else {

                // Normal character — add as a regular child node
                char ch = text.charAt(i);
                current.children.putIfAbsent(ch, new TrieNode());
                current = current.children.get(ch);
                i++;
            }
        }

        // Mark the final node as a LEAF with this template's ID
        current.templateId = template.getId();
    }

    // ─────────────────────────────────────────────────────────────
    // MATCH: Walk the message through the Trie
    // ─────────────────────────────────────────────────────────────

    /**
     * Matches an incoming message against the Trie.
     *
     * Returns MatchResult with templateId and extracted variable values.
     * Returns MatchResult.noMatch() if nothing matches.
     *
     * Example:
     *   message = "Dear Customer,your OTP is 4532,valid for 2 mins."
     *   → templateId = 1, extractedValues = ["4532", "2"]
     */
    public MatchResult match(String message) {
        List<String> extractedValues = new ArrayList<>();
        MatchResult result = matchFromNode(root, message, 0, extractedValues);
        return result;
    }

    /**
     * Recursive matching function.
     *
     * node    = current position in the Trie
     * message = the full incoming message
     * msgIdx  = current position in the message we are reading
     * values  = list we keep appending captured variable values to
     */
    private MatchResult matchFromNode(TrieNode node, String message, int msgIdx, List<String> values) {

        // ── BASE CASE ──
        // We reached a LEAF node AND we have consumed the full message → MATCH!
        if (node.isLeaf() && msgIdx == message.length()) {
            return new MatchResult(node.templateId, new ArrayList<>(values));
        }

        // ── BASE CASE ──
        // Message fully consumed but node is not a leaf → no match on this path
        if (msgIdx >= message.length()) {
            return MatchResult.noMatch();
        }

        // ── NORMAL CHARACTER ──
        // Try to follow the exact character in Trie
        char currentChar = message.charAt(msgIdx);
        TrieNode nextNode = node.children.get(currentChar);

        if (nextNode != null) {
            // Character matched — go deeper
            MatchResult result = matchFromNode(nextNode, message, msgIdx + 1, values);
            if (result.isMatched()) {
                return result; // Propagate the successful match up
            }
        }

        // ── WILDCARD CHARACTER ──
        // If a wildcard child exists, try consuming message characters
        // until we find the next static character that the Trie expects
        if (node.wildcardChild != null) {
            return matchWithWildcard(node.wildcardChild, message, msgIdx, values);
        }

        // Nothing worked on this path
        return MatchResult.noMatch();
    }

    /**
     * Handles WILDCARD node matching.
     *
     * At a wildcard node, we do NOT know how many characters to skip.
     * So we try consuming 1 character, then 2, then 3... and after each
     * attempt we try to continue matching the REST of the Trie.
     *
     * The first attempt that eventually leads to a full match wins.
     *
     * Example:
     *   message remaining = "4532,valid for 2 mins."
     *   After wildcard, Trie expects ","
     *
     *   Try consume "4"     → remaining = "532,..." → Trie next char "," ? No, "5" → keep going
     *   Try consume "45"    → remaining = "32,..."  → Trie next char "," ? No, "3" → keep going
     *   Try consume "453"   → remaining = "2,..."   → Trie next char "," ? No, "2" → keep going
     *   Try consume "4532"  → remaining = ",..."    → Trie next char "," ? YES ✅
     *   Captured value = "4532"
     */
    private MatchResult matchWithWildcard(TrieNode wildcardNode, String message, int msgIdx, List<String> values) {

        // Keep expanding the captured value one character at a time
        for (int end = msgIdx + 1; end <= message.length(); end++) {

            // The value captured so far
            String capturedValue = message.substring(msgIdx, end);

            // Try to continue matching from this wildcard node
            // with the rest of the message starting at position `end`
            List<String> valuesCopy = new ArrayList<>(values);
            valuesCopy.add(capturedValue);

            MatchResult result = matchFromNode(wildcardNode, message, end, valuesCopy);

            if (result.isMatched()) {
                return result;
            }
        }

        return MatchResult.noMatch();
    }
}
