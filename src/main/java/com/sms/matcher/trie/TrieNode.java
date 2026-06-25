package com.sms.matcher.trie;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {

    // Each child is keyed by a character
    // Example: node for 'D' has child 'e', which has child 'a', etc.
    public Map<Character, TrieNode> children = new HashMap<>();

    // This is the special WILDCARD node
    // It exists when {#var} appears in a template
    // Only one wildcard child per node (a {#var} leads to one wildcard path)
    public TrieNode wildcardChild = null;

    // If this node is the END of a template, store the template ID here
    // Otherwise it is -1 (not a leaf)
    public long templateId = -1;

    // Is this node a leaf (end of a full template)?
    public boolean isLeaf() {
        return templateId != -1;
    }
}
