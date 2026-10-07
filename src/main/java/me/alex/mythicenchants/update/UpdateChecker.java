package me.alex.mythicenchants.update;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.alex.mythicenchants.config.Settings;
import me.alex.mythicenchants.util.Text;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class UpdateChecker implements Listener {
    private final JavaPlugin plugin;
    private final PluginVersion installed;
    private final Map<UUID, String> notified = new HashMap<>();
    private BukkitTask task;
    private Settings settings;
    private PluginVersion available;
    private PluginVersion checkedVersion;
    private volatile long generation;
    private String lastError;
    public UpdateChecker(JavaPlugin plugin) { this.plugin = plugin; this.installed = PluginVersion.parse(plugin.getPluginMeta().getVersion()); }
    public void start(Settings settings) {
        stop();
        this.settings = settings;
        available = null;
        checkedVersion = null;
        if (!settings.updatesEnabled()) return;
        long token = generation;
        task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> check(settings, token), 0L,
            settings.updateIntervalHours() * 60L * 60L * 20L);
    }
    public void stop() {
        generation++;
        if (task != null) { task.cancel(); task = null; }
    }
    private void check(Settings settings, long token) {
        PluginVersion remote = null;
        String failure = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) settings.versionUrl().toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "MythicEnchants/" + installed);
            if (connection.getResponseCode() != 200) throw new java.io.IOException("HTTP " + connection.getResponseCode());
            try (var input = connection.getInputStream()) {
                byte[] body = input.readNBytes(129);
                if (body.length > 128) throw new java.io.IOException("Version file exceeds 128 bytes");
                remote = PluginVersion.parse(new String(body, StandardCharsets.UTF_8));
            }
        } catch (Exception e) { failure = e.getClass().getSimpleName() + ": " + e.getMessage(); }
        finally { if (connection != null) connection.disconnect(); }
        if (generation != token) return;
        PluginVersion checked = remote;
        String error = failure;
        try {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (generation != token) return;
                if (error != null) {
                    if (!error.equals(lastError)) plugin.getLogger().warning("Update check failed: " + error);
                    lastError = error;
                    return;
                }
                lastError = null;
                checkedVersion = checked;
                available = checked.compareTo(installed) > 0 ? checked : null;
                plugin.getServer().getOnlinePlayers().forEach(this::notifyPlayer);
            });
        } catch (org.bukkit.plugin.IllegalPluginAccessException ignored) { /* Plugin disabled while fetching. */ }
    }
    private void notifyPlayer(Player player) {
        if (checkedVersion == null || !player.isOp()) return;
        String notice = available == null ? "current:" + installed : "update:" + available;
        if (notice.equals(notified.get(player.getUniqueId()))) return;
        if (available == null) {
            player.sendMessage(Text.colour("&a&l(!) &a&l&nMythicEnchants&r &aup to date."));
            notified.put(player.getUniqueId(), notice);
            return;
        }
        player.sendMessage(Text.colour("&b&l(!) &dMythicEnchants &eupdate &6v" + available));
        player.sendMessage(Text.colour("&ePlease go onto ")
            .append(Text.colour("&e" + settings.repositoryUrl()).clickEvent(ClickEvent.openUrl(settings.repositoryUrl().toString())))
            .append(Text.colour("&e and update this version of the plugin for the latest features!")));
        notified.put(player.getUniqueId(), notice);
    }
    @EventHandler public void onJoin(PlayerJoinEvent event) { notifyPlayer(event.getPlayer()); }
    @EventHandler public void onQuit(PlayerQuitEvent event) { notified.remove(event.getPlayer().getUniqueId()); }
}
