package com.xenderz.xzcore.gui.tokens;

import net.kyori.adventure.text.format.TextColor;

/**
 * Semantic color tokens for consistent UI theming.
 * Provides color constants organized by purpose (actions, states, content).
 */
public final class SemanticTokens {
    
    // Action colors - for interactive elements
    public static final TextColor ACTION_PRIMARY = TextColor.color(0x00A8FF);
    public static final TextColor ACTION_SECONDARY = TextColor.color(0x6C5CE7);
    public static final TextColor ACTION_ACCENT = TextColor.color(0xFDCB6E);
    
    // State colors - for status indicators
    public static final TextColor STATE_SUCCESS = TextColor.color(0x00B894);
    public static final TextColor STATE_WARNING = TextColor.color(0xE17055);
    public static final TextColor STATE_ERROR = TextColor.color(0xD63031);
    public static final TextColor STATE_INFO = TextColor.color(0x74B9FF);
    
    // Content colors - for text hierarchy
    public static final TextColor CONTENT_PRIMARY = TextColor.color(0xFFFFFF);
    public static final TextColor CONTENT_SECONDARY = TextColor.color(0xB2BEC3);
    public static final TextColor CONTENT_MUTED = TextColor.color(0x636E72);
    
    // Background/UI colors
    public static final TextColor BACKGROUND_PRIMARY = TextColor.color(0x2D3436);
    public static final TextColor BACKGROUND_SECONDARY = TextColor.color(0x636E72);
    
    // Economy/value colors
    public static final TextColor VALUE_HIGH = TextColor.color(0x00CEC9);
    public static final TextColor VALUE_MEDIUM = TextColor.color(0xFDCB6E);
    public static final TextColor VALUE_LOW = TextColor.color(0xE17055);
    
    // PvP-specific colors
    public static final TextColor COMBAT_HOSTILE = TextColor.color(0xFF7675);
    public static final TextColor COMBAT_FRIENDLY = TextColor.color(0x55EFC4);
    public static final TextColor COMBAT_NEUTRAL = TextColor.color(0xA29BFE);
    
    // Rank colors
    public static final TextColor RANK_COMMON = TextColor.color(0xB2BEC3);
    public static final TextColor RANK_UNCOMMON = TextColor.color(0x00CEC9);
    public static final TextColor RANK_RARE = TextColor.color(0x6C5CE7);
    public static final TextColor RANK_EPIC = TextColor.color(0xFDCB6E);
    public static final TextColor RANK_LEGENDARY = TextColor.color(0xE17055);
    public static final TextColor RANK_MYTHIC = TextColor.color(0xFF7675);
    
    private SemanticTokens() {
        // Utility class
    }
}
