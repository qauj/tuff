package com.primesmp.primebox;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.*;

public class ArenaListener implements Listener {

    private final PrimeBox plugin;
    // Cooldown map to prevent spam entry messages
    private final Map<UUID, Long> entryCooldown = new HashMap<>();
    // Track players already queued to enter
    private final Set<UUID> enteringQueue = new HashSet<>();

    public ArenaListener(PrimeBox plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Only fire if player actually moved to a new block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockY() == event.getTo().getBlockY()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // If already in a match, prevent leaving
        if (plugin.getMatchManager().isInMatch(player)) {
            Arena arena = plugin.getMatchManager().getArena(player);
            if (arena != null && !arena.contains(event.getTo())) {
                event.setCancelled(true);
            }
            return;
        }

        // If already queued to enter, don't queue again
        if (enteringQueue.contains(uuid)) return;

        // Check if walking near glass of any arena
        Location to = event.getTo();
        Arena arena = null;
        for (Arena a : plugin.getArenaManager().getAll()) {
            if (a.isNearGlass(to)) {
                arena = a;
                break;
            }
        }
        if (arena == null) return;
        if (!arena.isAvailable()) {
            // Only send occupied message once per 3 seconds
            long now = System.currentTimeMillis();
            if (!entryCooldown.containsKey(uuid) || now - entryCooldown.get(uuid) > 3000) {
                entryCooldown.put(uuid, now);
                plugin.sendMsg(player, "<red>This arena is currently occupied!");
            }
            return;
        }

        // Send entry message with cooldown
        long now = System.currentTimeMillis();
        if (entryCooldown.containsKey(uuid) && now - entryCooldown.get(uuid) < 2000) return;
        entryCooldown.put(uuid, now);

        final Arena finalArena = arena;
        int delay = plugin.getConfig().getInt("teleport-delay", 3);
        enteringQueue.add(uuid);
        plugin.sendMsg(player, "<white>Entering arena <aqua>" + finalArena.getName() + "<white> in <aqua>" + delay + "<white> seconds...");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            enteringQueue.remove(uuid);
            if (!player.isOnline()) return;
            if (plugin.getMatchManager().isInMatch(player)) return;
            if (!finalArena.isAvailable()) {
                plugin.sendMsg(player, "<red>Arena became occupied while you were waiting!");
                return;
            }
            Location spawnLoc = finalArena.getPlayers().isEmpty() ? finalArena.getSpawn1() : finalArena.getSpawn2();
            if (spawnLoc == null) {
                plugin.sendMsg(player, "<red>Arena spawns not set! Contact an admin.");
                return;
            }
            player.teleport(spawnLoc);
            plugin.getMatchManager().addPlayer(player, finalArena);
        }, delay * 20L);
    }

    // Block commands inside arena
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getMatchManager().isInMatch(player)) return;
        if (player.isOp()) return;

        List<String> blocked = plugin.getConfig().getStringList("blocked-commands");
        String cmd = event.getMessage().toLowerCase().substring(1).split(" ")[0];

        for (String b : blocked) {
            if (cmd.equalsIgnoreCase(b)) {
                event.setCancelled(true);
                plugin.sendMsg(player, "<red>You cannot use that command during a match!");
                return;
            }
        }
    }

    // Handle death in arena
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.getMatchManager().isInMatch(player)) return;

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    // Handle respawn — teleport loser out after respawn
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getMatchManager().isInMatch(player)) return;

        // Set respawn to world spawn
        event.setRespawnLocation(player.getWorld().getSpawnLocation());

        // Handle match end after respawn
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getMatchManager().handleDeath(player);
        }, 1L);
    }

    // Clean up on quit
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        enteringQueue.remove(player.getUniqueId());
        entryCooldown.remove(player.getUniqueId());

        if (!plugin.getMatchManager().isInMatch(player)) return;

        Arena arena = plugin.getMatchManager().getArena(player);
        if (arena == null) return;

        Player opponent = arena.getOpponent(player);
        if (opponent != null) {
            plugin.sendMsg(opponent, "<green>You won! <white>(Opponent disconnected)");
            plugin.getMatchManager().removePlayer(opponent, arena, false);
        }
        plugin.getMatchManager().removePlayer(player, arena, false);
        plugin.getMatchManager().resetArena(arena);
    }
}
