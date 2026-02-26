package com.xenderz.xzcore.gui.tokens;

/**
 * Slot position constants for common GUI components.
 * Standardized positions for consistent navigation across all GUIs.
 */
public final class ComponentTokens {
    
    // Navigation slots (bottom row)
    public static final int NAV_BACK = 45;
    public static final int NAV_CLOSE = 49;
    public static final int NAV_NEXT = 53;
    public static final int NAV_PREV = 45;
    
    // Page navigation (for paginated GUIs)
    public static final int PAGE_FIRST = 45;
    public static final int PAGE_LAST = 53;
    public static final int PAGE_INFO = 49;
    
    // Quick action slots (hotbar area)
    public static final int ACTION_1 = 36;
    public static final int ACTION_2 = 37;
    public static final int ACTION_3 = 38;
    public static final int ACTION_4 = 39;
    public static final int ACTION_5 = 40;
    public static final int ACTION_6 = 41;
    public static final int ACTION_7 = 42;
    public static final int ACTION_8 = 43;
    public static final int ACTION_9 = 44;
    
    // Header slots (top row)
    public static final int HEADER_LEFT = 0;
    public static final int HEADER_CENTER = 4;
    public static final int HEADER_RIGHT = 8;
    
    // Info/Status slots
    public static final int STATUS_TOP = 13;
    public static final int STATUS_CENTER = 22;
    public static final int STATUS_BOTTOM = 31;
    
    // Touch-friendly grid positions (for mobile-first layouts)
    // 2x2 grid in upper area (rows 1-2)
    public static final int[] GRID_2X2_UPPER = {10, 12, 19, 21};
    
    // 2x2 grid in center (rows 2-3)
    public static final int[] GRID_2X2_CENTER = {20, 22, 29, 31};
    
    // 3x3 grid center (rows 2-4, cols 2-6)
    public static final int[] GRID_3X3_CENTER = {
        11, 12, 13,
        20, 21, 22,
        29, 30, 31
    };
    
    /**
     * Calculate slot index from row and column.
     * Convenience method that delegates to TouchTargets.
     *
     * @param row Row index (0-based)
     * @param col Column index (0-based, 0-8)
     * @return Slot index (0-53 for 6-row inventory)
     */
    public static int slot(int row, int col) {
        return row * 9 + col;
    }
    
    private ComponentTokens() {
        // Utility class
    }
}
