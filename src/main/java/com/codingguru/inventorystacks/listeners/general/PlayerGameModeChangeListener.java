package com.codingguru.inventorystacks.listeners.general;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.managers.GameModeManager;
import com.codingguru.inventorystacks.managers.WorldStackManager;
import com.codingguru.inventorystacks.util.InventoryOverflowUtil;
import com.cryptomorin.xseries.XMaterial;

/**
 * Listens for gamemode changes and re-applies or strips custom stack sizes
 * from the player's inventory based on whether the new gamemode is in the
 * allowed list.
 *
 * <p>When stripping (switching to a non-allowed gamemode), any slots whose
 * current amount exceeds the vanilla max are clamped and the overflow is
 * redistributed to free slots or dropped at the player's feet.
 *
 * <p>This listener is only registered when {@link GameModeManager#isEnabled()}
 * returns {@code true} AND the modern ItemMeta API is in use.
 */
public class PlayerGameModeChangeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (!GameModeManager.getInstance().isEnabled()) return;
        if (!ItemHandler.getInstance().isUsingModernAPI()) return;

        Player player = event.getPlayer();
        GameMode newGameMode = event.getNewGameMode();
        boolean allowed = GameModeManager.getInstance().isAllowed(newGameMode);

        ItemStack[] contents = player.getInventory().getContents();
        boolean changed = false;

        for (ItemStack stack : contents) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            XMaterial xMat = XMaterial.matchXMaterial(stack);
            if (xMat == null) continue;

            if (allowed) {
                // Switching into an allowed gamemode – restore the correct stack size.
                int targetSize = resolveTargetSize(xMat, player.getWorld().getName());
                if (targetSize == -1) continue;
                // When restoring a larger max there is no overflow risk; use the util
                // for consistency (it handles the "already set" no-op check too).
                if (InventoryOverflowUtil.applyWithOverflow(player, stack, targetSize)) {
                    changed = true;
                }
            } else {
                // Switching into a non-allowed gamemode – strip custom stack size.
                // Pass -1 so the util resets to vanilla and handles overflow.
                if (InventoryOverflowUtil.applyWithOverflow(player, stack, -1)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            player.updateInventory();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private int resolveTargetSize(XMaterial xMat, String worldName) {
        // World-specific override takes priority over global override.
        int worldSize = WorldStackManager.getInstance().getWorldStackSize(xMat, worldName);
        if (worldSize != -1) return worldSize;

        return ItemHandler.getInstance().getGlobalStackSize(xMat);
    }
}
