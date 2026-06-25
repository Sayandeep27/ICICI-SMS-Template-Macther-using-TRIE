package com.sms.matcher.trie;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MatchResult {

    // The matched template ID (-1 means no match)
    private long templateId;

    // The extracted variable values in order
    // Example for "OTP is 4532, valid for 2 mins" → ["4532", "2"]
    private List<String> extractedValues;

    public boolean isMatched() {
        return templateId != -1;
    }

    public static MatchResult noMatch() {
        return new MatchResult(-1, List.of());
    }
}
