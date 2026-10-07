# MythicEnchants

By **Alex**. A range of enchants in multiple tiers with a server api.

A Paper **26.3** enchant framework running on **Java 25**. Version 1 includes books, configurable definitions and colours, inventory application, a developer API, admin commands, and GitHub update notices. Enchant definitions do not provide gameplay effects. The bundled Example enchant explicitly has no gameplay effect; extension plugins can read applied levels and implement their own effects.

## Install and build

Copy `build/libs/MythicEnchants-1.02.jar` into your Paper server's `plugins` directory and restart. The plugin creates `plugins/MythicEnchants/config.yml` and `enchants.yml` on first startup.

Build with the included Gradle 9.4.0 wrapper and Java 25 installed:

```powershell
.\gradlew.bat clean build
```

On Linux/macOS: `./gradlew clean build`. The API dependency is pinned to Paper `26.3.build.157-beta`; verification also runs on Paper 26.3 build 159. Paper and Adventure are supplied by the server and are not bundled into the JAR.

## Commands

Only OPs can use these commands, even if a non-OP has been granted `mythicenchants.admin`. Console can still use the admin subcommands.

`/mythicenchants` or `/me` without arguments opens an empty 4-row, 36-slot menu titled `&8MythicEnchantments`. Opening it plays `ENTITY_BAT_TAKEOFF` at volume `0.6` and pitch `1.0`. Items cannot be placed in the menu using clicks, shift-clicks, or drags.

```text
/mythicenchants give <player> <enchant-id> <level> <success> <destroy>
/mythicenchants list
/mythicenchants reload
```

Example:

```text
/mythicenchants give Alex mythicenchants:example 3 75 25
```

Player commands using `/me` are routed to MythicEnchants even when Minecraft or another plugin also registers that name. Book delivery requires an online player and enough storage space; a full inventory rejects delivery without dropping a book. Commands have tab completion.

## Books and applying enchants

Books use `BOOK` items with a maximum stack size of one, a tier-coloured name, a Roman numeral level, green success chance, red destroy chance, yellow description lines, a blank separator, and the two requested grey hint lines. Book names, book lore, and applied enchant lore have italics explicitly disabled, including embedded `&o` formatting.

Move books normally between empty or occupied inventory and chest slots. Pick up a book on your cursor and **left-click** a compatible single piece of gear in your own inventory, including equipped armour, to apply it. Each valid attempt consumes **one** book:

- Success applies the enchant or upgrades an existing lower level. No destruction roll occurs on success.
- Failure triggers a separate destroy roll. If it passes, the target gear is destroyed; otherwise the gear survives unchanged.
- Invalid books, unsupported gear, unknown definitions, levels above the maximum, equal/lower upgrades, stacked gear, and cancelled attempts consume nothing.

Both chances must be integers from **1 through 100**, independently; they do not need to add up to 100. Shift-clicks, right-clicks, inventory drag events, and container slots do not apply books. Moving the cursor or changing the target before the scheduled transaction prevents that application.

The plugin uses persistent metadata rather than visible lore to identify books and levels. Enchants survive ordinary item serialization and server restarts. Unrelated lore and metadata are preserved. Definition removal leaves applied metadata intact; an unknown applied enchant uses its ID as the fallback name when its gear is later updated. Existing books keep their original display until recreated; new books and newly updated gear use current tier colours.

## Configuration

`config.yml` provides these enum defaults, editable without rebuilding:

| Tier | Colour |
| --- | --- |
| Simple | `&f` |
| Uncommon | `&a` |
| Elite | `&9` |
| Ultimate | `&e` |
| Legendary | `&6` |
| Godly | `&c` |
| Mythic | `&d` |

Use a single legacy colour code `&0` through `&f` for each tier. Example definition in `enchants.yml`:

```yaml
enchants:
  'mythicenchants:example':
    enabled: true
    name: Example
    tier: SIMPLE
    max-level: 3
    description:
      - 'Example enchant for testing the framework.'
      - 'Has no gameplay effect.'
    materials:
      - WOODEN_SWORD
      - STONE_SWORD
      - COPPER_SWORD
      - IRON_SWORD
      - GOLDEN_SWORD
      - DIAMOND_SWORD
      - NETHERITE_SWORD
```

IDs must include a namespace. Levels range from 1 to 3999; materials must be valid non-stackable item types. Each description entry becomes a yellow lore line. Definitions may have different description lengths. Set `enabled: false` to omit a definition.

Reload validates both files before replacing active settings. Invalid configuration or a conflict with an API registration keeps the previous settings and definitions. API registrations remain available after reload. Existing applied levels are not downgraded when a definition's maximum changes.

## Developer API

Use this JAR as a **compile-only** dependency in your extension, and add `depend: [MythicEnchants]` to its `plugin.yml`. Do not shade MythicEnchants or Paper into your extension.

Public API: `me.alex.mythicenchants.api.MythicEnchantsAPI`. All API calls must run on the server thread.

```java
import java.util.List;
import java.util.Set;
import me.alex.mythicenchants.api.EnchantDefinition;
import me.alex.mythicenchants.api.MythicEnchantsAPI;
import me.alex.mythicenchants.enchant.EnchantTier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

MythicEnchantsAPI api = getServer().getServicesManager().load(MythicEnchantsAPI.class);
if (api == null) throw new IllegalStateException("MythicEnchants API unavailable");
NamespacedKey id = new NamespacedKey(this, "lifesteal");
api.register(this, new EnchantDefinition(
    id, "Lifesteal", EnchantTier.LEGENDARY, 3,
    List.of("Your extension implements this effect."),
    Set.of(Material.DIAMOND_SWORD, Material.NETHERITE_SWORD)
));
var book = api.createBook(id, 2, 75, 25);

int level = api.appliedEnchants(player.getInventory().getItemInMainHand())
    .getOrDefault(id, 0);
```

`register(owner, definition)` rejects duplicate IDs and disabled owners. `unregister(owner, id)` removes only that owner's API definition. Disabling an extension removes its registrations automatically. Config definitions cannot be unregistered through this method.

`definitions()` and `definition(id)` expose immutable definitions. `inspectBook(item)` returns stored `BookData`, or an empty optional for an unmarked/malformed book. A syntactically valid book can still reference an unregistered definition; application rejects it. `appliedEnchants(item)` returns an immutable map and throws `IllegalArgumentException` for malformed stored levels.

`attemptApplication(player, books, gear)` validates and rolls without mutating its inputs or the player's inventory. It returns an `ApplicationResult` with `status`, `message`, `remainingBooks`, and `resultingGear`. The caller must commit **both** output snapshots to its own inventory transaction when `consumedBook()` is true. A null snapshot means an empty slot. Statuses are `INVALID`, `CANCELLED`, `APPLIED`, `FAILED`, and `DESTROYED`.

```java
var result = api.attemptApplication(player, cursorBooks, targetGear);
if (result.consumedBook()) {
    player.setItemOnCursor(result.remainingBooks());
    player.getInventory().setItem(targetSlot, result.resultingGear());
}
```

Valid attempts fire cancellable `EnchantPreApplyEvent` before randomness or consumption. Cancel it to protect gear in regions or special inventories. Successful, failed, and destroyed attempts then fire `EnchantApplyResultEvent`. Both events expose item snapshots. The result event reports the calculated outcome **before** the calling inventory code commits it; use it as an outcome notification, not proof of an inventory commit. The built-in listener respects existing inventory-event cancellation and rechecks the cursor and slot before committing.

## GitHub update notices

`version.txt` at the repository root is the build's single version source, currently `1.02`. The updater checks this public URL asynchronously at startup and every six hours:

```text
https://raw.githubusercontent.com/CrazyLadAlexx/MythicEnchants/main/version.txt
```

Publish a new JAR, increase `version.txt`, and push it to `main`. Versions accept `major.minor` or `major.minor.patch` with an optional leading `v`. Components compare numerically, with a missing patch treated as zero: `1.01` is greater than `1.0.0`, and `1.02` is greater than `1.01`. Notifications preserve the published spelling. Code pushes without a version increase do not produce update notices.

Online OPs receive a new version notice, and joining OPs receive the cached notice once per session per available version:

```text
&b&l(!) &dMythicEnchants &eupdate &6v<updated_version>
&ePlease go onto <github_link> and update this version of the plugin for the latest features!
```

When a successful check confirms the installed version is current (or newer than the version on GitHub), OPs receive:

```text
&a&l(!) &a&l&nMythicEnchants&r &aup to date.
```

This status is also sent once per player session. Failed checks never report the plugin as up to date. Disabling update checks disables both kinds of notices.

Startup prints `&aMythicEnchants loaded successfully!` to the console. Startup failures print `&cMythicEnchants produced <issue>!`, with the issue filled in, and log the exception for diagnosis.

The repository link is clickable. Non-OPs receive no notices. `updates.enabled`, `updates.interval-hours` (1–168), `updates.version-url`, and `updates.repository-url` are configurable; URLs must use HTTPS. Fetches have connection/read timeouts and a 128-byte response limit. Missing or invalid files and connection errors log a warning without player chat spam; repeated identical errors are suppressed until the error changes or a check succeeds.

## Project layout

Java packages under `me.alex.mythicenchants`:

- `api`, `api.event`: external contracts and events.
- `enchant`, `book`, `application`: definitions, books, and application rules.
- `gui`, `listener`, `command`: the OP menu, player inventory interaction, and administration.
- `config`, `update`, `util`: validated configuration, GitHub checks, text, and numeral helpers.

Runtime resources live in `src/main/resources`. Test sources, smoke-server files, and test dependencies have been removed.
