package com.xenderz.xzcore.commands;

import com.xenderz.xzcore.XzCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command handler for /xzcore
 * 
 * Usage: /xzcore [reload|status|save]
 */
public class XzCoreCommand implements CommandExecutor, TabCompleter {
    
    private final XzCore plugin;
    
    public XzCoreCommand(@NotNull XzCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("xzcore.admin")) {
            sender.sendMessage(Component.text("You don't have permission!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sendMainHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "status" -> handleStatus(sender);
            case "save" -> handleSave(sender);
            default -> sendMainHelp(sender);
        }
        
        return true;
    }
    
    private void handleReload(@NotNull CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(Component.text("✓ XzCore configuration reloaded", NamedTextColor.GREEN));
    }
    
    private void handleStatus(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("═══ XzCore Status ═══", NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Version: ", NamedTextColor.GRAY).append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Services: ", NamedTextColor.GRAY).append(Component.text(plugin.getServiceContainer().getActiveServices(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text(""));
    }
    
    private void handleSave(@NotNull CommandSender sender) {
        plugin.getServiceContainer().getPlayerDataManager().saveAll();
        sender.sendMessage(Component.text("✓ All data saved", NamedTextColor.GREEN));
    }
    
    private void sendMainHelp(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("═══ XzCore Commands ═══", NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("/xzcore reload", NamedTextColor.YELLOW).append(Component.text(" - Reload configuration", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/xzcore status", NamedTextColor.YELLOW).append(Component.text(" - Show plugin status", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/xzcore save", NamedTextColor.YELLOW).append(Component.text(" - Save all data", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text(""));
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("xzcore.admin")) {
            return List.of();
        }
        
        if (args.length == 1) {
            return List.of("reload", "status", "save").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return List.of();
    }
}
