package com.codingguru.inventorystacks.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ConsoleUtil;
import com.cryptomorin.xseries.XMaterial;

/**
 * Manages per-world stack size overrides.
 *
 * <p>Config format (inside config.yml):
 * <pre>
 * worlds:
 *   world_nether:
 *     DIAMOND: 32
 *   minigames:
 *     POTION: 1
 * </pre>
 *
 * <p>This feature is entirely optional. If the {@code worlds} section is absent or
 * disabled the plugin behaves exactly as before.
 */
public class WorldStackManager {

    private static final WorldStackManager INSTANCE = new WorldStackManager();

    /**
     * worldName -> (XMaterial -> stackSize)
     */
    private final Map<String, Map<XMaterial, Integer>> worldStacks = new HashMap<>();

    private boolean enabled = false;

    private WorldStackManager() {
    }

    public static WorldStackManager getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Setup / reload
    // -------------------------------------------------------------------------

    public void load() {
        worldStacks.clear();
        enabled = false;

        ConfigurationSection worldsSection = InventoryStacks.getInstance().getConfig()
                .getConfigurationSection("worlds");

        if (worldsSection == null) {
            return; // feature not configured – that's fine
        }

        Set<String> worldNames = worldsSection.getKeys(false);
        if (worldNames.isEmpty()) {
            return;
        }

        int absoluteMax = ItemHandler.getInstance().getServerVersion().getAbsoluteMaxStackSize();

        for (String worldName : worldNames) {
            ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
            if (worldSection == null) continue;

            Map<XMaterial, Integer> stackMap = new HashMap<>();

            for (String key : worldSection.getKeys(false)) {
                if (!worldSection.isInt(key)) {
                    ConsoleUtil.warning("[WorldStacks] Invalid stack size for '" + key
                            + "' in world '" + worldName + "' – skipping.");
                    continue;
                }

                int size = worldSection.getInt(key);

                if (size < 1) {
                    ConsoleUtil.warning("[WorldStacks] Stack size for '" + key
                            + "' in world '" + worldName + "' must be >= 1 – defaulting to 1.");
                    size = 1;
                } else if (size > absoluteMax) {
                    ConsoleUtil.warning("[WorldStacks] Stack size for '" + key
                            + "' in world '" + worldName + "' exceeds max (" + absoluteMax
                            + ") – clamping.");
                    size = absoluteMax;
                }

                XMaterial xMat = XMaterial.matchXMaterial(key).orElse(null);
                if (xMat == null) {
                    ConsoleUtil.warning("[WorldStacks] Unknown material '" + key
                            + "' in world '" + worldName + "' – skipping.");
                    continue;
                }

                stackMap.put(xMat, size);
                ConsoleUtil.info("[WorldStacks] " + worldName + " | " + xMat.name() + " -> " + size);
            }

            if (!stackMap.isEmpty()) {
                worldStacks.put(worldName.toLowerCase(), stackMap);
            }
        }

        enabled = !worldStacks.isEmpty();
    }

    // -------------------------------------------------------------------------
    // Runtime helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the per-world feature has any configuration.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the world-specific stack size for the given material in the given
     * world, or {@code -1} if no override is defined.
     *
     * @param xMaterial the material to look up
     * @param worldName the Bukkit world name (case-insensitive)
     * @return the configured stack size, or {@code -1} if not overridden
     */
    public int getWorldStackSize(XMaterial xMaterial, String worldName) {
        if (!enabled || xMaterial == null || worldName == null) return -1;

        Map<XMaterial, Integer> stackMap = worldStacks.get(worldName.toLowerCase());
        if (stackMap == null) return -1;

        return stackMap.getOrDefault(xMaterial, -1);
    }

    /**
     * Returns every world that has at least one override defined.
     */
    public Set<String> getConfiguredWorlds() {
        return worldStacks.keySet();
    }

    /**
     * Returns the full override map for a world, or an empty map if none.
     */
    public Map<XMaterial, Integer> getStacksForWorld(String worldName) {
        return worldStacks.getOrDefault(worldName.toLowerCase(), new HashMap<>());
    }
}
