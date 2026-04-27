# InventoryStacks 📦

> **Change the default 64 stack size for any Minecraft item — per-item, per-world, or per-gamemode.**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.8--1.21.x-brightgreen)](https://papermc.io)
[![API](https://img.shields.io/badge/API-Paper%20%7C%20Spigot%20%7C%20Folia-blue)](https://papermc.io)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](LICENSE)

---

## 🔍 What is InventoryStacks?

**InventoryStacks** is a lightweight, highly configurable Minecraft server plugin that lets you override the maximum stack size of any item in the game. Whether you want potions to stack to 16, diamonds to stack to 256, or every item to stack to 1 for a hardcore survival experience — InventoryStacks handles it all.

Compatible with **Paper**, **Spigot**, and **Folia** across Minecraft versions **1.8 through 1.21.x**.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔢 **Custom Stack Sizes** | Set any item's max stack size (1–99 on legacy, 1–99 on 1.20.5+) |
| 🌍 **Per-World Overrides** | Different stack limits per world (e.g. survival vs minigames) |
| 🎮 **Gamemode Restriction** | Only apply custom stacks in allowed gamemodes (e.g. Survival only) |
| 📋 **`/stacks list`** | View all active stack overrides in-game at a glance |
| 🔄 **Live Reload** | Reload config without restarting the server via `/stacks reload` |
| 🪣 **Stack Command** | Players can merge item stacks via `/stack` |
| 🧩 **Regex Item Matching** | Target groups of items using regex patterns in config |
| ⚡ **Folia Support** | Fully compatible with Folia's regionalised multithreading |
| 🛡️ **Exploit-Safe** | Overflow items redistributed or discarded safely — no dupe exploits |
| 📦 **MiniMessage Support** | Modern rich-text formatting for all plugin messages |

---

## 📋 Requirements

- **Java** 17+
- **Minecraft** 1.8 – 1.21.x
- **Server software**: Paper, Spigot, or Folia
- **Optional dependency**: [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) (for legacy NMS support)

---

## 🚀 Installation

1. Download the latest `InventoryStacks.jar` from the [Releases](../../releases) page.
2. Drop it into your server's `plugins/` folder.
3. Restart or reload your server.
4. Edit `plugins/InventoryStacks/config.yml` to configure your stack sizes.
5. Run `/stacks reload` to apply changes without restarting.

---

## ⚙️ Configuration

### Basic Stack Sizes (`config.yml`)

```yaml
# Set custom stack sizes per item.
# Supports exact names and regex patterns.
items:
  POTION: 16
  SPLASH_POTION: 16
  DIAMOND: 256
  .*_SWORD: 1        # All swords stack to 1 (regex)

# Set a universal stack size for ALL items at once.
max-stack-for-all-items:
  enabled: false
  amount: 64
  whitelist:         # Items excluded from the universal rule
    - DIAMOND_PICKAXE
```

### Per-World Stack Overrides *(1.20.5+ only)*

```yaml
# Different stack sizes per world.
# Optional — remove this section to disable entirely.
worlds:
  world_nether:
    DIAMOND: 32
    POTION: 1
  minigames:
    ARROW: 128
    POTION: 4
```

### Gamemode Restriction *(1.20.5+ only)*

```yaml
# Only apply custom stacks when a player is in one of these gamemodes.
# Switching gamemode strips/restores stack sizes automatically.
apply-in-gamemodes:
  enabled: false
  gamemodes:
    - SURVIVAL
    - ADVENTURE
```

### Stack Command

```yaml
stack-command:
  enabled: true
  default-stack-type: 'HAND'   # HAND or ALL
```

---

## 🕹️ Commands

| Command | Description | Permission |
|---|---|---|
| `/stack` | Stack items in hand (or all inventory) | `STACKS.COMMAND` |
| `/stack hand` | Stack only the held item type | `STACKS.COMMAND` |
| `/stack all` | Stack all items in your inventory | `STACKS.COMMAND` |
| `/stacks reload` | Reload all configuration files | `STACKS.RELOAD` |
| `/stacks list` | View all active stack size overrides | `STACKS.LIST` |

**Permission wildcard:** `STACKS.*` grants access to all commands.

---

## 🔐 Permissions

| Permission | Description |
|---|---|
| `STACKS.*` | Grants all InventoryStacks permissions |
| `STACKS.COMMAND` | Allows use of `/stack` |
| `STACKS.RELOAD` | Allows `/stacks reload` |
| `STACKS.LIST` | Allows `/stacks list` |

---

## 🖥️ Server Compatibility

| Server | Supported | Notes |
|---|---|---|
| Paper 1.20.5+ | ✅ | Full feature set (ItemMeta API) |
| Paper 1.8–1.20.4 | ✅ | Legacy NMS reflection mode |
| Spigot 1.13+ | ✅ | Full support |
| Folia | ✅ | Fully regionalised-scheduler aware |
| BungeeCord | ❌ | Not applicable (backend plugin) |

---

## 🔧 Advanced Options

```yaml
# Force legacy NMS reflection even on 1.20.5+ servers
use-legacy-reflection: false

# Automatically clean up custom stack meta from unconfigured items
auto-stack-cleanup: true

# Use MiniMessage format for all plugin messages
use-mini-message: false

# Tick delay for bucket/potion/damageable item updates (increase for Folia)
item-change-delay: 2
```

---

## 🛡️ Security

InventoryStacks is built with exploit prevention in mind:

- **No item duplication** — when stack sizes shrink, overflow is safely redistributed to free inventory slots or dropped at the player's feet.
- **Creative/Spectator protection** — players in creative or spectator mode have overflow discarded rather than dropped into the world, preventing item generation exploits.
- **Amount clamping** — all stack size changes clamp actual item counts before updating metadata to prevent client/server desync.

---

## 📜 Changelog

### v2.6.2-BETA (Current)
- ✅ Per-world stack size overrides
- ✅ Gamemode-restricted stacking
- ✅ `/stacks list` command
- ✅ `InventoryOverflowUtil` — safe overflow handling with exploit prevention
- ✅ Legacy build compatibility fixes

---

## 🤝 Contributing

Pull requests are welcome! Please open an issue first to discuss any major changes.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'feat: add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made for Minecraft server administrators who want full control over item stacking.
  <br/>
  <strong>InventoryStacks</strong> — Stack smarter, not harder.
</p>
