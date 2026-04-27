package com.codingguru.inventorystacks.commands;

import java.util.Map;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.managers.GameModeManager;
import com.codingguru.inventorystacks.managers.WorldStackManager;
import com.codingguru.inventorystacks.util.MessagesUtil;
import com.cryptomorin.xseries.XMaterial;

public class ReloadCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            MessagesUtil.sendMessage(sender,
                    MessagesUtil.INCORRECT_USAGE.toString().replaceAll("%command%", "/" + label + " reload|list"));
            return false;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
            case "rl":
                return handleReload(sender);
            case "list":
                return handleList(sender);
            default:
                MessagesUtil.sendMessage(sender,
                        MessagesUtil.INCORRECT_USAGE.toString().replaceAll("%command%", "/" + label + " reload|list"));
                return false;
        }
    }

    // -------------------------------------------------------------------------
    // Subcommand handlers
    // -------------------------------------------------------------------------

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("STACKS.*") && !sender.hasPermission("STACKS.RELOAD")) {
            MessagesUtil.sendMessage(sender, MessagesUtil.NO_PERMISSION.toString());
            return false;
        }

        InventoryStacks.getInstance().reloadConfig();
        ItemHandler.getInstance().reloadInventoryStacks();
        MessagesUtil.sendMessage(sender, MessagesUtil.RELOAD.toString());
        return false;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("STACKS.*") && !sender.hasPermission("STACKS.LIST")) {
            MessagesUtil.sendMessage(sender, MessagesUtil.NO_PERMISSION.toString());
            return false;
        }

        // --- Global stack sizes ---
        Map<XMaterial, Integer> globalStacks = ItemHandler.getInstance().getCachedMaterialSizes();

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "=== InventoryStacks – Active Stack Sizes ===");

        if (globalStacks.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "  (no global overrides configured)");
        } else {
            // Sort alphabetically for readability.
            new TreeMap<>(globalStacks).forEach((mat, size) ->
                    sender.sendMessage(ChatColor.YELLOW + "  " + mat.name()
                            + ChatColor.GRAY + " → " + ChatColor.GREEN + size));
        }

        // --- World-specific overrides ---
        if (WorldStackManager.getInstance().isEnabled()) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "--- Per-World Overrides ---");
            for (String world : new TreeMap<>(WorldStackManager.getInstance().getConfiguredWorlds()
                    .stream().collect(java.util.stream.Collectors.toMap(w -> w, w -> w))).keySet()) {
                sender.sendMessage(ChatColor.AQUA + "  [" + world + "]");
                WorldStackManager.getInstance().getStacksForWorld(world).forEach((mat, size) ->
                        sender.sendMessage(ChatColor.YELLOW + "    " + mat.name()
                                + ChatColor.GRAY + " → " + ChatColor.GREEN + size));
            }
        }

        // --- Gamemode restriction ---
        if (GameModeManager.getInstance().isEnabled()) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "--- Gamemode Restriction ---");
            sender.sendMessage(ChatColor.YELLOW + "  Stacks apply in: "
                    + ChatColor.GREEN + InventoryStacks.getInstance().getConfig()
                            .getStringList("apply-in-gamemodes.gamemodes").toString());
        }

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "==========================================");
        return false;
    }
}
