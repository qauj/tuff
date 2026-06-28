package com.primesmp.primebox;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrimeBox extends JavaPlugin {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private ArenaManager arenaManager;
    private MatchManager matchManager;
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();
    private final Map<UUID, Location> glassPos1Map = new HashMap<>();
    private final Map<UUID, Location> glassPos2Map = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        arenaManager = new ArenaManager(this);
        matchManager = new MatchManager(this);

        getServer().getPluginManager().registerEvents(new ArenaListener(this), this);

        getCommand("primebox").setExecutor(new PrimeBoxCommand(this));

        getCommand("pos1").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            if (!player.hasPermission("primebox.admin")) { sendMsg(player, "<red>No permission."); return true; }
            pos1Map.put(player.getUniqueId(), player.getLocation().clone());
            sendMsg(player, "<white>Pos1 set to <aqua>" + formatLoc(player.getLocation()));
            return true;
        });

        getCommand("pos2").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            if (!player.hasPermission("primebox.admin")) { sendMsg(player, "<red>No permission."); return true; }
            pos2Map.put(player.getUniqueId(), player.getLocation().clone());
            sendMsg(player, "<white>Pos2 set to <aqua>" + formatLoc(player.getLocation()));
            return true;
        });

        getCommand("glasspos1").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            if (!player.hasPermission("primebox.admin")) { sendMsg(player, "<red>No permission."); return true; }
            glassPos1Map.put(player.getUniqueId(), player.getLocation().clone());
            sendMsg(player, "<white>Glass Pos1 set to <aqua>" + formatLoc(player.getLocation()));
            return true;
        });

        getCommand("glasspos2").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return true;
            if (!player.hasPermission("primebox.admin")) { sendMsg(player, "<red>No permission."); return true; }
            glassPos2Map.put(player.getUniqueId(), player.getLocation().clone());
            sendMsg(player, "<white>Glass Pos2 set to <aqua>" + formatLoc(player.getLocation()));
            return true;
        });

        getLogger().info("PrimeBox enabled!");
    }

    @Override
    public void onDisable() {
        arenaManager.save();
        getLogger().info("PrimeBox disabled.");
    }

    public void sendMsg(Player player, String message) {
        Component prefix = MM.deserialize("<#0083FF><b>PRIMESMP</b> <reset>");
        Component msg = MM.deserialize(message);
        player.sendMessage(prefix.append(msg));
    }

    public ArenaManager getArenaManager() { return arenaManager; }
    public MatchManager getMatchManager() { return matchManager; }
    public Location getPos1(Player p) { return pos1Map.get(p.getUniqueId()); }
    public Location getPos2(Player p) { return pos2Map.get(p.getUniqueId()); }
    public Location getGlassPos1(Player p) { return glassPos1Map.get(p.getUniqueId()); }
    public Location getGlassPos2(Player p) { return glassPos2Map.get(p.getUniqueId()); }

    private String formatLoc(Location loc) {
        return String.format("%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());
    }
}
