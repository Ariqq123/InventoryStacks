package com.codingguru.inventorystacks.util;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Utility for safely applying a new max-stack size to an {@link ItemStack}
 * that may currently hold more items than the new max allows.
 *
 * <p>When the current amount exceeds {@code newMax}:
 * <ul>
 *   <li>The slot is clamped to the new max.</li>
 *   <li>For normal gamemodes the overflow is added to the inventory; anything
 *       that doesn't fit is dropped at the player's feet.</li>
 *   <li>For CREATIVE / SPECTATOR the excess is simply discarded – dropping
 *       physical items from creative inventories would allow item generation
 *       exploits.</li>
 * </ul>
 */
public final class InventoryOverflowUtil {

    private InventoryOverflowUtil() {
    }

    /**
     * Applies {@code newMax} as the ItemMeta max-stack-size on {@code stack}.
     *
     * @param player the player whose inventory is being modified
     * @param stack  a live reference inside the player's inventory
     * @param newMax the new max stack size; use {@code -1} to strip (vanilla max)
     * @return {@code true} if the ItemMeta was changed
     */
    public static boolean applyWithOverflow(Player player, ItemStack stack, int newMax) {
        if (stack == null || stack.getType().isAir()) return false;

        // Resolve the effective cap.
        int effectiveCap = (newMax == -1) ? stack.getType().getMaxStackSize() : newMax;
        if (effectiveCap <= 0) effectiveCap = 1;

        // Fetch meta early so we can do the no-op check.
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;

        boolean metaAlreadySet = (newMax == -1)
                ? !meta.hasMaxStackSize()
                : (meta.hasMaxStackSize() && meta.getMaxStackSize() == newMax);

        int currentAmount = stack.getAmount();
        if (metaAlreadySet && currentAmount <= effectiveCap) return false;

        // ---- Handle overflow BEFORE mutating meta ----
        if (currentAmount > effectiveCap) {
            // Clamp the slot first.
            stack.setAmount(effectiveCap);

            int overflow = currentAmount - effectiveCap;

            // SECURITY: Never drop items into the world from creative/spectator
            // players – that would generate real items from nothing.
            boolean shouldDrop = player.getGameMode() != GameMode.CREATIVE
                    && player.getGameMode() != GameMode.SPECTATOR;

            if (shouldDrop) {
                // Build a clean overflow template with the correct max already set.
                ItemStack overflowTemplate = stack.clone(); // amount = effectiveCap, we'll override
                ItemMeta overflowMeta = overflowTemplate.getItemMeta();
                if (overflowMeta != null) {
                    if (newMax == -1) overflowMeta.setMaxStackSize(null);
                    else overflowMeta.setMaxStackSize(newMax);
                    overflowTemplate.setItemMeta(overflowMeta);
                }

                while (overflow > 0) {
                    int portion = Math.min(overflow, effectiveCap);
                    overflow -= portion;

                    ItemStack drop = overflowTemplate.clone();
                    drop.setAmount(portion);

                    // Give to inventory first; drop at feet if full.
                    player.getInventory().addItem(drop).values()
                            .forEach(leftover ->
                                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
                }
            }
            // else: creative/spectator – excess is simply discarded (no physical drop).
        }

        // ---- Apply the new max to the slot ----
        // Re-fetch meta from the (now-clamped) stack so we're working with the
        // latest state rather than the stale copy obtained before setAmount().
        ItemMeta freshMeta = stack.getItemMeta();
        if (freshMeta == null) return false;

        try {
            if (newMax == -1) {
                freshMeta.setMaxStackSize(null);
            } else {
                freshMeta.setMaxStackSize(newMax);
            }
            stack.setItemMeta(freshMeta);
            return true;
        } catch (Exception ex) {
            ConsoleUtil.warning("[InventoryStacks] Failed to apply stack size "
                    + newMax + " to " + stack.getType() + ": " + ex.getMessage());
            return false;
        }
    }
}
