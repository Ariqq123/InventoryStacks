package com.codingguru.inventorystacks.managers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.GameMode;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.util.ConsoleUtil;

/**
 * Manages the optional gamemode-restriction feature.
 *
 * <p>When enabled, custom stack sizes are only applied when a player is in one
 * of the configured gamemodes. Switching to a non-allowed gamemode will strip
 * the custom stack size from all items in the player's inventory; switching
 * back will restore it.
 *
 * <p>Config example:
 * <pre>
 * apply-in-gamemodes:
 *   enabled: true
 *   gamemodes:
 *     - SURVIVAL
 *     - ADVENTURE
 * </pre>
 *
 * <p>This feature is fully optional. If disabled or absent there is zero
 * overhead – every existing code path behaves exactly as before.
 */
public class GameModeManager {

    private static final GameModeManager INSTANCE = new GameModeManager();

    private boolean enabled = false;
    private final Set<GameMode> allowedGameModes = EnumSet.noneOf(GameMode.class);

    private GameModeManager() {
    }

    public static GameModeManager getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Setup / reload
    // -------------------------------------------------------------------------

    public void load() {
        allowedGameModes.clear();
        enabled = false;

        if (!InventoryStacks.getInstance().getConfig().getBoolean("apply-in-gamemodes.enabled", false)) {
            return;
        }

        List<String> gameModeNames = InventoryStacks.getInstance().getConfig()
                .getStringList("apply-in-gamemodes.gamemodes");

        for (String name : gameModeNames) {
            try {
                GameMode gm = GameMode.valueOf(name.toUpperCase());
                allowedGameModes.add(gm);
                ConsoleUtil.info("[GameModeRestriction] Allowing stacks in gamemode: " + gm.name());
            } catch (IllegalArgumentException e) {
                ConsoleUtil.warning("[GameModeRestriction] Unknown gamemode '" + name + "' – skipping.");
            }
        }

        if (allowedGameModes.isEmpty()) {
            ConsoleUtil.warning("[GameModeRestriction] Enabled but no valid gamemodes listed – feature disabled.");
            return;
        }

        enabled = true;
    }

    // -------------------------------------------------------------------------
    // Runtime helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the gamemode restriction feature is active.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns {@code true} if custom stack sizes should be applied for the
     * given gamemode. Always returns {@code true} when the feature is disabled.
     *
     * @param gameMode the player's current gamemode
     * @return whether stack size overrides are permitted in this gamemode
     */
    public boolean isAllowed(GameMode gameMode) {
        if (!enabled) return true;
        return allowedGameModes.contains(gameMode);
    }
}
