package me.alex.mythicenchants.command;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import me.alex.mythicenchants.api.MythicEnchantsAPI;
import me.alex.mythicenchants.gui.EnchantMenu;
import me.alex.mythicenchants.util.Text;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MythicEnchantsCommand implements TabExecutor {
    @FunctionalInterface public interface Reload { void run() throws Exception; }
    private final JavaPlugin plugin;
    private final MythicEnchantsAPI api;
    private final Reload reload;
    public MythicEnchantsCommand(JavaPlugin plugin, MythicEnchantsAPI api, Reload reload) { this.plugin = plugin; this.api = api; this.reload = reload; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) { sender.sendMessage(Text.colour("&cOnly OPs can use this command.")); return true; }
        if (args.length == 0) {
            if (sender instanceof Player player) EnchantMenu.open(player);
            else sender.sendMessage(Text.colour("&cOnly players can open the MythicEnchants menu."));
            return true;
        }
        try {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "list" -> {
                    if (args.length != 1) { usage(sender, label); break; }
                    sender.sendMessage(Text.colour("&dMythicEnchants &7(" + api.definitions().size() + " definitions)"));
                    api.definitions().forEach(d -> sender.sendMessage(Text.colour("&e" + d.id() + " &7- " + d.tier().displayName() + ", max level " + d.maxLevel())));
                }
                case "reload" -> {
                    if (args.length != 1) { usage(sender, label); break; }
                    reload.run(); sender.sendMessage(Text.colour("&aMythicEnchants configuration reloaded."));
                }
                case "give" -> {
                    if (args.length != 6) { usage(sender, label); break; }
                    var player = plugin.getServer().getPlayerExact(args[1]);
                    if (player == null) throw new IllegalArgumentException("Player must be online.");
                    NamespacedKey id = args[2].contains(":") ? NamespacedKey.fromString(args[2]) : null;
                    if (id == null) throw new IllegalArgumentException("Use a namespaced enchant ID.");
                    var book = api.createBook(id, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                    var contents = player.getInventory().getStorageContents();
                    int destination = -1;
                    for (int slot = 0; slot < contents.length; slot++) {
                        var item = contents[slot];
                        if (item == null || item.getType().isAir()) { destination = slot; break; }
                    }
                    if (destination < 0) throw new IllegalArgumentException("Player inventory is full; no book was given.");
                    player.getInventory().setItem(destination, book);
                    sender.sendMessage(Text.colour("&aGave an enchant book to " + player.getName() + "."));
                }
                default -> usage(sender, label);
            }
        } catch (NumberFormatException e) { sender.sendMessage(Text.colour("&cLevel and chances must be integers.")); }
        catch (Exception e) { sender.sendMessage(Text.colour("&c" + e.getMessage())); }
        return true;
    }
    private static void usage(CommandSender sender, String label) {
        sender.sendMessage(Text.colour("&e/" + label + " give <player> <enchant-id> <level> <success> <destroy>"));
        sender.sendMessage(Text.colour("&e/" + label + " <list|reload>"));
    }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) return List.of();
        List<String> choices = List.of();
        if (args.length == 1) choices = List.of("give", "list", "reload");
        else if (args[0].equalsIgnoreCase("give")) {
            choices = switch (args.length) {
                case 2 -> plugin.getServer().getOnlinePlayers().stream().map(p -> p.getName()).sorted().toList();
                case 3 -> api.definitions().stream().map(d -> d.id().toString()).toList();
                case 4 -> {
                    var id = NamespacedKey.fromString(args[2]);
                    int max = id == null ? 1 : api.definition(id).map(d -> d.maxLevel()).orElse(1);
                    yield java.util.stream.IntStream.rangeClosed(1, Math.min(max, 100)).mapToObj(Integer::toString).toList();
                }
                case 5, 6 -> Arrays.asList("1", "25", "50", "75", "100");
                default -> List.of();
            };
        }
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return choices.stream().filter(choice -> choice.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
