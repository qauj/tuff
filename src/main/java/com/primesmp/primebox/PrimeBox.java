package com.primesmp.primebox;

import net.kyori.adventure.text.minimessage.MiniMessage;
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

    @Override
    public void onEnable() {
        saveDefaultConfig();
        arenaManager = new ArenaManager(this);
        matchManager = new MatchManager(this);

        getServer().getPluginManager().registerEvents(new ArenaListener(this), this);

        getCommand("primebox").setExecutor(new PrimeBoxCommand(this));
        getCommand("pos1").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player player)) return true;
                pos1Map.put(player.getUniqueId(), player.getLocation());
                sendMsg(player, "&fPos1 set to &b" + formatLoc(player.getLocation()));
                return true;
            }
        });
        getCommand("pos2").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (!(sender instanceof Player player)) return true;
                pos2Map.put(player.getUniqueId(), player.getLocation());
                sendMsg(player, "&fPos2 set to &b" + formatLoc(player.getLocation()));
                return true;
            }
        });

        getLogger().info("PrimeBox enabled!");
    }

    @Override
    public void onDisable() {
        arenaManager.save();
        getLogger().info("PrimeBox disabled.");
    }

    public void sendMsg(Player player, String message) {
        String prefix = "<#0083FF><b>PRIMESMP</b> <reset>";
        player.sendMessage(MM.deserialize(prefix + message));
    }

    public ArenaManager getArenaManager() { return arenaManager; }
    public MatchManager getMatchManager() { return matchManager; }

    public Location getPos1(Player player) { return pos1Map.get(player.getUniqueId()); }
    public Location getPos2(Player player) { return pos2Map.get(player.getUniqueId()); }

    private String formatLoc(Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
}
