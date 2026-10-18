# Scuffed Undead

A Forge 1.20.1 mod that powers a zombie-apocalypse modpack. It adds a **class system** (pick a role on first join, get a starter kit and a passive ability), a set of **lockpicks** built to plug into Sona's locked chests, and a few survival economy tweaks.

- **Minecraft:** 1.20.1
- **Loader:** Forge 47.x
- **Mod ID:** `scuffedundead`
- **Package:** `net.yaseru.scuffedundead`
- **Author:** YaserUXD

---

## Features at a glance

- **Class selection screen** the first time a player joins. The choice is saved to the player and survives death, relogs and End portals.
- **11 classes**, each with flavor text, a starter kit and one or more passives.
- **Starter kits** with vanilla gear, modded items (guns, flashlights, tools) and NBT items. Armor in a kit is equipped automatically.
- **Lockpicks** (iron, steel, diamond, netherite) that lose durability when they open a locked container. Made for Sona's *Unlock Items* config.
- **Scrap economy:** villager and wandering trader emerald trades use `recycler:scrap` instead.
- **Log stripping crafting recipe:** put an axe above a log in a crafting grid.
- **Missing-mod safe:** items from mods that aren't installed are skipped with a warning in the log, not a crash.

---

## Classes

The class is chosen once. Passives only apply to the matching class.

| Class | Passive(s) |
|---|---|
| **Police** | **Suspect Tagging:** hostile mobs you hit glow for 5s and are slowed (Slowness II) for 2s. Each mob has a short cooldown so fast guns can't lock it down. |
| **Farmer** | **Bountiful Harvest:** 20% chance of a second batch of drops from fully grown crops. |
| **Cook** | **Chef's Touch:** 20% chance per item to double food you craft or cook. Works for crafting, furnaces and smokers, the Farmer's Delight cooking pot, and the Farm & Charm pot, stove and roaster. |
| **Medic** | **Stockpile:** 20% chance to double healing items you craft (from a configurable item list). **Distress Ping:** action bar alert when a teammate within 48 blocks drops to 25% health or less. |
| **Hunter** | **Butcher:** a knife or cleaver in your main hand gets +2 Looting while held (also seen by butchering mods that read the tool's enchantments). |
| **Lumberjack** | **Replanter:** cutting the base log of a tree plants a sapling of the same wood, where the tree stood or where you are standing. Works with tree-felling mods. |
| **Veteran** | **Frugal Shooter:** 20% chance each shot from a TaCZ gun doesn't use ammo. |
| **Otaku** | **Bucket List:** melee kills in a row stack Speed (up to Speed III). **Opening Strike:** first hit after 3s without attacking deals +50% damage. **Katana Mastery:** +25% damage with listed Japanese blades. |
| **LARPer** | **Iron Will:** 1% less damage from attacks per armor point (max 25%). **Medieval Might:** Resistance I while holding a `tkvm_weapons` weapon in your main hand. |
| **Fireman** | **Fireproof:** 60% less fire and lava damage, 30% less explosion damage. |
| **Miner** | **Prospector's Luck:** 20% chance of extra drops from ores (not with Silk Touch). **Tunnel Rat:** Haste I while underground. |

Passive numbers (chances, multipliers, durations) are constants at the top of each class's event file in `classes/classevents/`.

### Admin commands

Op-only (permission level 2), meant for testing:

| Command | What it does |
|---|---|
| `/scuffedclass get` | Shows your current class |
| `/scuffedclass set <CLASS>` | Switches class without giving a kit (tab-completes names) |
| `/scuffedclass kit` | Gives the kit of your current class |
| `/scuffedclass reset` | Clears your class; the selection screen shows again after you relog |

---

## Lockpicks and Sona

Lockpicks are items you add to Sona's config so they can open Sona's locked chests and barrels, in place of (or alongside) the crowbar.

| Item | ID | Durability | Suggested unlock chance |
|---|---|---|---|
| Iron Lockpick | `scuffedundead:iron_lockpick` | 10 | 30 |
| Steel Lockpick | `scuffedundead:steel_lockpick` | 20 | 45 |
| Diamond Lockpick | `scuffedundead:diamond_lockpick` | 40 | 65 |
| Netherite Lockpick | `scuffedundead:netherite_lockpick` | 100 | 90 |

There is also `scuffedundead:iron_lockpick_blade`, a crafting component.

### Adding them to Sona

Edit `config/sona-common.toml`. Every entry is wrapped in double quotes, in the form `"item_id, chance"`:

```toml
"Unlock Items" = [
    "scuffedundead:iron_lockpick, 30",
    "scuffedundead:steel_lockpick, 45",
    "scuffedundead:diamond_lockpick, 65",
    "scuffedundead:netherite_lockpick, 90"
]
```

Check the comment above the option in your Sona version to confirm that the chance is a percentage. Use the exact item IDs (hold F3 + H in game to see them in tooltips). A missing quote or a typo in an ID can stop the config from loading and crash the game on startup.

### How wear works

When a player right-clicks a locked container with a lockpick, Scuffed Undead notes whether it was locked, then watches it for 2 seconds. If the lock opens, the lockpick loses 1 durability. A failed attempt costs nothing. The lock state is read from the container's own data (`ForgeData.LockFlag` and the `Lock` field), so it works on vanilla chests, barrels and Lootr containers alike.

If a lockpick loses 2 durability per success, Sona is also consuming the item itself; turn that off in Sona's config.

### Recipes

- **Iron Lockpick:** shaped, 2x2.

  | | | |
  |---|---|---|
  | Leather Strip (`tkvm_weapons:leather_strip`) | Iron Lockpick Blade | |
  | Stick | Leather Strip | |

- Other tiers: <!-- TODO: document the steel / diamond / netherite recipes here -->

---

## Other changes

- **Villager trades:** every emerald in a villager or wandering trader trade becomes `recycler:scrap`. Prices are multiplied by 3 and payouts stay 1:1, so scrap has to come mostly from scrapping, not trading. Trades are capped at one stack. Villagers that already have trades keep their old ones.
- **Log stripping recipe:** an axe above a log in a crafting grid gives the stripped log and damages the axe by 1 (recipes in `data/scuffedundead/recipes/log_stripping/`).
- **Creative tab:** a Scuffed Undead tab lists the mod's items.

---

## Integrated mods

None of these are hard requirements; features that depend on a missing mod simply don't activate.

| Mod | Used for |
|---|---|
| TaCZ (Timeless and Classics Zero) | Guns and ammo boxes in kits; the Veteran passive |
| Farmer's Delight, Farm & Charm | Cook passive on their cooking stations |
| Sona | Locked chests that accept the lockpicks |
| Vulcan's Flashlights (`vfl`) | Flashlight items in kits |
| `tkvm_weapons` | LARPer passive and kits |
| Marbled's Melees, Nihon Blacksmithing | Otaku katana bonus |
| Enhanced Falling Trees | Verified to work with the Lumberjack passive |
| Scrapper mod providing `recycler:scrap` | Trade currency |

---

## For developers

### Layout

```
net.yaseru.scuffedundead
├── ScuffedUndead.java            main class, registers everything
├── classes/
│   ├── PlayerClass.java          the class list, kits and descriptions
│   ├── ClassData.java            saves each player's class
│   ├── ClassEvents.java          login prompt and data copy on respawn
│   ├── ClassCommands.java        /scuffedclass
│   └── classevents/              one file per class passive
├── client/                       class selection screen
├── network/                      select / open-screen packets
├── item/                         LockpickItem and LockpickEvents
├── recipe/                       log stripping recipe and serializer
├── registry/                     ModItems, ModCreativeTab, ModNetwork, ModRecipeTypes
└── trades/                       villager trade changes
```

### Adding a class

Add one entry to `PlayerClass`. The selection screen, networking and saving pick it up automatically.

```java
MECHANIC("Mechanic",
        "Fixes anything with duct tape.\n§6Passive name: §7What it does.",
        "%s is now a Mechanic.",
        () -> List.of(
                item("minecraft:iron_pickaxe", 1, "Big Wrench"),
                gun("tacz:glock_17", "SEMI", 17),
                ammoBox("tacz:9mm", 144))),
```

- Every entry ends with a comma, except the last, which ends with `;`.
- **Never rename an existing entry.** The enum name is what's saved on each player.
- The order of entries is the order of the buttons; the server and client must run the same build.
- `\n` starts a new line in the description, and `§6` / `§7` are color codes.

### Kit helpers

| Helper | Example |
|---|---|
| `item(id, count)` | `item("farmersdelight:iron_knife", 1)` |
| `item(id, count, name)` | `item("modid:cleaver", 1, "Chef's Cleaver")` |
| `itemNbt(id, count, snbt)` | `itemNbt("vfl:flashlight", 1, "{Flashlight:{Charge:8206,Active:0b}}")` |
| `named(Items.X, count, name)` | `named(Items.IRON_AXE, 1, "Old Faithful")` |
| `gun(gunId, fireMode, ammo)` | `gun("tacz:1873", "SEMI", 8)` |
| `ammoBox(ammoId, amount)` | `ammoBox("tacz:44_40", 144)` |

Item IDs must be the full `modid:item`. To get the NBT of something you're holding, run `/data get entity @s SelectedItem`, and copy the part inside `tag: {...}`. Keys that contain a colon need quotes inside the string.

### Adding a passive

Create `classes/classevents/MyClassEvents.java`, annotate it with `@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)`, and listen for a Forge event. Every passive starts the same way:

```java
if (ClassData.get(player).orElse(null) != PlayerClass.MYCLASS) return;
```

### Adding an item

1. Register it in `ModItems` with a `RegistryObject`.
2. Add `assets/scuffedundead/models/item/<name>.json`, `textures/item/<name>.png` and a lang entry `item.scuffedundead.<name>`.
3. List it in `ModCreativeTab`.

### Building

```
./gradlew build
```

The jar is written to `build/libs/`. Use `./gradlew clean build` if the output looks stale.

---

## Known limitations

- Passive values are code constants; moving them to JSON or config files is planned (the Medic item list can already become an item tag).
- The Farmer bonus only covers crops built on the vanilla crop block. Crops from mods with their own block types may not count.
- Villagers keep trades they already generated; only new trades use scrap.
- The Veteran passive works by reading ammo tags on the held gun; the in-hand ammo counter may flicker briefly when a shot is refunded.

---

## License

<!-- TODO: add a license -->
