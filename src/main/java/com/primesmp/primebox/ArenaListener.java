package com.primesmp.primebox;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.List;

public class ArenaListener implements Listener {

    private final PrimeBox plugin;

    public ArenaListener(PrimeBox plugin) {
        this.plugin = plugin;
    }

    // Walk into glass wall → enter arena
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getMatchManager().isInMatch(player)) return;

        Location to = event.getTo();
        if (to == null) return;

        // Check block the player is walking into
        Location front = to.clone().add(to.getDirection().normalize().multiply(0.5));
        Arena arena = plugin.getArenaManager().getArenaByGlass(front.getBlock().getLocation());
        if (arena == null) {
            // Also check the block at player feet
            arena = plugin.getArenaManager().getArenaByGlass(to.getBlock().getLocation());
        }
        if (arena == null) return;
        if (!arena.isAvailable()) {
            plugin.sendMsg(player, plugin.getConfig().getString("messages.arena-occupied", "This arena is occupied!"));
            return;
        }

        final Arena finalArena = arena;
        int delay = plugin.getConfig().getInt("teleport-delay", 3);
        String enterMsg = plugin.getConfig().getString("messages.arena-entering", "Entering arena <arena> in <delay> seconds...")
            .replace("<arena>", finalArena.getName())
            .replace("<delay>", String.valueOf(delay));
        plugin.sendMsg(player, enterMsg);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!finalArena.isAvailable()) {
                plugin.sendMsg(player, plugin.getConfig().getString("messages.arena-occupied", "This arena is occupied!"));
                return;
            }
            Location spawnLoc = finalArena.getPlayers().isEmpty() ? finalArena.getSpawn1() : finalArena.getSpawn2();
            if (spawnLoc != null) player.teleport(spawnLoc);
            plugin.getMatchManager().addPlayer(player, finalArena);
        }, delay * 20L);
    }

    // Block commands inside arena
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getMatchManager().isInMatch(player)) return;

        List<String> blocked = plugin.getConfig().getStringList("blocked-commands");
        String cmd = event.getMessage().toLowerCase().substring(1).split(" ")[0];

        for (String blocked1 : blocked) {
            if (cmd.equalsIgnoreCase(blocked1)) {
                event.setCancelled(true);
                plugin.sendMsg(player, plugin.getConfig().getString("messages.command-blocked", "You cannot use that command during a match!"));
                return;
            }
        }
    }

    // Block leaving arena
    @EventHandler
    public void onMoveOut(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getMatchManager().isInMatch(player)) return;

        Arena arena = plugin.getMatchManager().getArena(player);
        if (arena == null) return;

        Location to = event.getTo();
        if (to == null) return;

        if (!arena.contains(to)) {
            event.setCancelled(true);
        }
    }

    // Handle death
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.getMatchManager().isInMatch(player)) return;

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getMatchManager().handleDeath(player);
        }, 1L);
    }

    // Handle respawn after death in arena
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getMatchManager().getArena(player);
        if (arena == null) return;
        Location spawn = player.getWorld().getSpawnLocation();
        event.setRespawnLocation(spawn);
    }

    // Clean up on quit
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getMatchManager().isInMatch(player)) return;

        Arena arena = plugin.getMatchManager().getArena(player);
        if (arena == null) return;

        Player opponent = arena.getOpponent(player);
        if (opponent != null) {
            plugin.sendMsg(opponent, plugin.getConfig().getString("messages.match-end-win", "You won!") + " (Opponent disconnected)");
            plugin.getMatchManager().removePlayer(opponent, arena, false);
        }

        plugin.getMatchManager().removePlayer(player, arena, false);
        plugin.getMatchManager().resetArena(arena);
    }
}
