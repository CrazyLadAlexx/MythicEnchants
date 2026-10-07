package me.alex.mythicenchants.listener;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class CommandAliasListener implements Listener {
    private final PluginCommand command;

    public CommandAliasListener(PluginCommand command) { this.command = command; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] parts = event.getMessage().trim().split("\\s+");
        if (!parts[0].equalsIgnoreCase("/me")) return;
        event.setCancelled(true);
        command.execute(event.getPlayer(), "me", java.util.Arrays.copyOfRange(parts, 1, parts.length));
    }
}
