package com.xenderz.xzcore.gui;

/**
 * Touch target utilities for mobile-first GUI design.
 * Provides slot calculations optimized for touch interaction,
 * ensuring adequate spacing for finger taps.
 */
public final class TouchTargets {
    
    /**
     * Minimum recommended slot spacing for touch targets.
     * This ensures adequate space between interactive elements.
     */
    public static final int MIN_TOUCH_SPACING = 1;
    
    /**
     * Standard touch-friendly button sizes (in inventory slots).
     */
    public static final int SIZE_SMALL = 1;   // Single slot
    public static final int SIZE_MEDIUM = 2;  // 2x2 area
    public static final int SIZE_LARGE = 3;   // 3x3 area
    
    /**
     * Get a 2x2 touch-friendly grid of slots.
     * Returns positions optimized for thumb reach on mobile devices.
     *
     * @param rows Total rows in the GUI (must be at least 3)
     * @return Array of 4 slot indices forming a 2x2 grid
     */
    public static int[] touchGrid(int rows) {
        if (rows < 3) {
            throw new IllegalArgumentException("GUI must have at least 3 rows for touch grid");
        }
        
        // Center in upper area (rows 1-2), comfortable for thumb
        return new int[] {10, 12, 19, 21};
    }
    
    /**
     * Get a 2x2 touch-friendly grid in the upper area.
     * Best for primary actions that need high visibility.
     *
     * @return Array of 4 slot indices
     */
    public static int[] upperGrid() {
        return new int[] {10, 12, 19, 21};
    }
    
    /**
     * Get a 2x2 touch-friendly grid in the center area.
     * Good for secondary actions or content display.
     *
     * @return Array of 4 slot indices
     */
    public static int[] centerGrid() {
        return new int[] {20, 22, 29, 31};
    }
    
    /**
     * Get a vertical stack of touch-friendly slots.
     * Useful for lists or sequential actions.
     *
     * @param startSlot Starting slot index
     * @param count Number of items
     * @param spacing Slots between items (minimum 1 for touch)
     * @return Array of slot indices
     */
    public static int[] verticalStack(int startSlot, int count, int spacing) {
        int actualSpacing = Math.max(spacing, MIN_TOUCH_SPACING);
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) {
            slots[i] = startSlot + (i * (actualSpacing + 1) * 9);
        }
        return slots;
    }
    
    /**
     * Get a horizontal row of touch-friendly slots.
     * Useful for toolbars or category selectors.
     *
     * @param row Row index (0-based)
     * @param startCol Starting column (0-based)
     * @param count Number of items
     * @param spacing Columns between items
     * @return Array of slot indices
     */
    public static int[] horizontalRow(int row, int startCol, int count, int spacing) {
        int actualSpacing = Math.max(spacing, MIN_TOUCH_SPACING);
        int[] slots = new int[count];
        int base = row * 9 + startCol;
        for (int i = 0; i < count; i++) {
            slots[i] = base + (i * (actualSpacing + 1));
        }
        return slots;
    }
    
    /**
     * Calculate slot index from row and column.
     *
     * @param row Row index (0-based)
     * @param col Column index (0-based, 0-8)
     * @return Slot index (0-53 for 6-row inventory)
     */
    public static int slot(int row, int col) {
        return row * 9 + col;
    }
    
    /**
     * Check if a slot is in a thumb-friendly zone.
     * Zones are optimized for right-thumb use on mobile.
     *
     * @param slot Slot index
     * @return true if slot is in thumb-friendly area
     */
    public static boolean isThumbFriendly(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        
        // Right side, middle-to-lower rows are best for right thumb
        return col >= 4 && row >= 2 && row <= 4;
    }
    
    /**
     * Get the recommended slot spacing for a given GUI density.
     *
     * @param density Desired element density (LOW, MEDIUM, HIGH)
     * @return Recommended spacing between elements
     */
    public static int spacing(Density density) {
        return switch (density) {
            case LOW -> 2;      // Maximum comfort, fewer elements
            case MEDIUM -> 1;   // Balanced
            case HIGH -> 0;     // Desktop-optimized, minimal spacing
        };
    }
    
    // Swipe gesture zones (column ranges for navigation)
    public static final int[] SWIPE_LEFT = {0, 9, 18, 27, 36, 45};
    public static final int[] SWIPE_RIGHT = {8, 17, 26, 35, 44, 53};
    
    /**
     * Density levels for GUI element arrangement.
     */
    public enum Density {
        LOW,      // Spacious, touch-optimized
        MEDIUM,   // Balanced
        HIGH      // Compact, desktop-optimized
    }
    
    private TouchTargets() {
        // Utility class
    }
}
