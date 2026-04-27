package com.codingguru.inventorystacks.listeners.itemmeta;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.managers.GameModeManager;
import com.codingguru.inventorystacks.scheduler.Schedule;

public class UpdateItemMetaListener implements Listener {

	private final java.util.Set<Integer> scheduled = java.util.Collections
			.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		// Respect gamemode restriction: if the viewing player's gamemode is not
		// allowed, strip any custom stack size instead of applying it.
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if (!GameModeManager.getInstance().isAllowed(p.getGameMode())) {
				handleCleanup(e.getCurrentItem());
				return;
			}
		}
		callNow(e.getCurrentItem());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPickup(EntityPickupItemEvent e) {
		// Respect gamemode restriction for players picking up items.
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (!GameModeManager.getInstance().isAllowed(p.getGameMode())) {
				handleCleanup(e.getItem().getItemStack());
				return;
			}
		}
		callNow(e.getItem().getItemStack());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryPickupItem(InventoryPickupItemEvent e) {
		callNow(e.getItem().getItemStack());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryMove(InventoryMoveItemEvent e) {
		callNow(e.getItem());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockDispense(BlockDispenseEvent e) {
		callLater(e.getItem());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent e) {
		callLater(e.getEntity().getItemStack());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPrepareCrafter(CrafterCraftEvent e) {
		callLater(e.getResult());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onFurnaceSmelt(FurnaceSmeltEvent e) {
		callLater(e.getResult());
	}

	private void callNow(ItemStack stack) {
		if (!shouldHandle(stack))
			return;

		ItemHandler.getInstance().applyItem(false, stack);
	}

	private void callLater(ItemStack stack) {
		if (!shouldHandle(stack))
			return;

		final int key = System.identityHashCode(stack);

		if (!scheduled.add(key))
			return;

		Schedule stackApplyTask = new Schedule() {
			@Override
			public void run() {
				try {
					ItemHandler.getInstance().applyItem(false, stack);
				} finally {
					scheduled.remove(key);
				}
			}
		};

		stackApplyTask.runTaskLater(1L);
	}

	private boolean shouldHandle(ItemStack stack) {
		if (stack == null)
			return false;

		if (stack.getType().isAir())
			return false;

		if (!ItemHandler.getInstance().hasUpdatedStack(stack)) {
			handleCleanup(stack);
			return false;
		}

		return true;
	}

	private void handleCleanup(ItemStack stack) {
		if (stack == null || stack.getType().isAir()) return;

		FileConfiguration config = InventoryStacks.getInstance().getConfig();

		if (!config.getBoolean("auto-stack-cleanup"))
			return;

		ItemMeta meta = stack.getItemMeta();

		if (meta == null)
			return;

		if (!meta.hasMaxStackSize())
			return;

		// Clamp the actual amount to the vanilla max BEFORE stripping the custom
		// max, so we never leave a slot holding more than its new declared max.
		// (The excess items are simply discarded here because this code path
		// fires in a read-only inventory-click context where we cannot safely
		// redistribute items without risking further event loops.)
		int vanillaMax = stack.getType().getMaxStackSize();
		if (stack.getAmount() > vanillaMax) {
			stack.setAmount(vanillaMax);
		}

		meta.setMaxStackSize(null);
		stack.setItemMeta(meta);
	}
}