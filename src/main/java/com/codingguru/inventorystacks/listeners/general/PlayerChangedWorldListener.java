package com.codingguru.inventorystacks.listeners.general;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.managers.GameModeManager;
import com.codingguru.inventorystacks.managers.WorldStackManager;
import com.codingguru.inventorystacks.util.InventoryOverflowUtil;
import com.cryptomorin.xseries.XMaterial;

/**
 * Listens for world changes and re-applies the correct stack sizes to every
 * item in the player's inventory based on the destination world's overrides.
 *
 * <p>If the new world's configured max for an item is smaller than the player's
 * current stack amount, the slot is clamped and overflow is redistributed to
 * free inventory slots or dropped at the player's feet.
 *
 * <p>This listener is only registered when {@link WorldStackManager#isEnabled()}
 * returns {@code true}, so there is zero overhead when the feature is unused.
 *
 * <p><b>Modern API (1.20.5+) only.</b>
 */
public class PlayerChangedWorldListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (!WorldStackManager.getInstance().isEnabled()) return;

        // Only the modern ItemMeta API can cleanly do per-player/per-world overrides.
        if (!ItemHandler.getInstance().isUsingModernAPI()) return;

        // Respect gamemode restriction.
        Player player = event.getPlayer();
        if (!GameModeManager.getInstance().isAllowed(player.getGameMode())) return;

        String newWorldName = player.getWorld().getName();

        ItemStack[] contents = player.getInventory().getContents();
        boolean changed = false;

        for (ItemStack stack : contents) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            XMaterial xMat = XMaterial.matchXMaterial(stack);
            if (xMat == null) continue;

            int targetSize = resolveTargetSize(xMat, newWorldName);
            if (targetSize == -1) continue;

            // applyWithOverflow handles the case where the current amount exceeds
            // the new max: it clamps the slot and gives/drops the overflow.
            if (InventoryOverflowUtil.applyWithOverflow(player, stack, targetSize)) {
                changed = true;
            }
        }

        if (changed) {
            player.updateInventory();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Determines the correct stack size for this material in the given world.
     *
     * <ol>
     *   <li>World-specific override (from {@code worlds:} section)</li>
     *   <li>Global plugin override</li>
     *   <li>No change needed – returns {@code -1}</li>
     * </ol>
     */
    private int resolveTargetSize(XMaterial xMat, String worldName) {
        // Check for a world-specific override first.
        int worldSize = WorldStackManager.getInstance().getWorldStackSize(xMat, worldName);
        if (worldSize != -1) return worldSize;

        // If the global plugin also has a stack for this material, re-apply it
        // in case the previous world had a different (larger) override.
        if (ItemHandler.getInstance().hasEditedStackSize(xMat)) {
            return ItemHandler.getInstance().getGlobalStackSize(xMat);
        }

        return -1;
    }
}
