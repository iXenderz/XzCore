package com.xenderz.xzcore.gui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Standardized sound effects for GUI interactions.
 * Provides consistent audio feedback across all XzPlugin GUIs.
 */
public enum XzCoreSounds {
    
    // Navigation sounds
    OPEN(Sound.BLOCK_CHEST_OPEN, 0.8f, 1.0f),
    CLOSE(Sound.BLOCK_CHEST_CLOSE, 0.8f, 1.0f),
    BACK(Sound.UI_BUTTON_CLICK, 0.6f, 0.8f),
    
    // Action sounds
    CLICK(Sound.UI_BUTTON_CLICK, 0.8f, 1.0f),
    SELECT(Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.2f),
    CONFIRM(Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.5f),
    CANCEL(Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f),
    
    // Success/Error sounds
    SUCCESS(Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.2f),
    ERROR(Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f),
    WARNING(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.8f, 0.8f),
    
    // Value/Transaction sounds
    COINS(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.5f),
    VALUE_UP(Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.5f),
    VALUE_DOWN(Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 0.8f),
    
    // Combat/PvP sounds
    COMBAT_ENABLE(Sound.ITEM_ARMOR_EQUIP_IRON, 0.8f, 1.0f),
    COMBAT_DISABLE(Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.8f, 0.8f),
    TARGET_LOCK(Sound.ENTITY_ARROW_HIT_PLAYER, 0.6f, 1.0f),
    
    // Menu navigation
    PAGE_NEXT(Sound.UI_BUTTON_CLICK, 0.6f, 1.2f),
    PAGE_PREV(Sound.UI_BUTTON_CLICK, 0.6f, 0.9f),
    TAB_SWITCH(Sound.UI_BUTTON_CLICK, 0.5f, 1.5f),
    
    // Special interactions
    HOVER(Sound.UI_BUTTON_CLICK, 0.3f, 2.0f),
    DRAG(Sound.UI_BUTTON_CLICK, 0.4f, 1.8f),
    DROP(Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
    
    private final Sound sound;
    private final float volume;
    private final float pitch;
    
    XzCoreSounds(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    /**
     * Play this sound for a player.
     *
     * @param player The player to play the sound for
     */
    public void play(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
    
    /**
     * Play this sound for a player with a custom pitch variation.
     *
     * @param player The player to play the sound for
     * @param pitchVariation Amount to vary the pitch (0.0 to 1.0)
     */
    public void play(Player player, float pitchVariation) {
        float variedPitch = pitch + ((float) Math.random() * pitchVariation * 2 - pitchVariation);
        player.playSound(player.getLocation(), sound, volume, Math.max(0.5f, variedPitch));
    }
    
    /**
     * Play this sound at a specific location for all nearby players.
     *
     * @param player The source player (for location)
     * @param radius Radius to play the sound
     */
    public void playNearby(Player player, double radius) {
        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
    }
    
    public Sound getSound() {
        return sound;
    }
    
    public float getVolume() {
        return volume;
    }
    
    public float getPitch() {
        return pitch;
    }
}
