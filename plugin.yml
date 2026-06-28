package com.primesmp.primebox;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MatchManager {

    private final PrimeBox plugin;
    private final Map<UUID, Arena> playerArena = new HashMap<>();
    private final Map<UUID, org.bukkit.inventory.ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, org.bukkit.inventory.ItemStack[]> savedArmor = new HashMap<>();

    public MatchManager(PrimeBox plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player, Arena arena) {
        playerArena.put(player.getUniqueId(), arena);
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents().clone());
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());
        arena.addPlayer(player);

        if (arena.getPlayers().size() == 2) {
            startMatch(arena);
        }
    }

    public void startMatch(Arena arena) {
        arena.setLocked(true);
        Material occupied = Material.valueOf(plugin.getConfig().getString("glass.occupied", "RED_STAINED_GLASS"));
        arena.setGlassColor(occupied);

        for (Player p : arena.getPlayers()) {
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            plugin.sendMsg(p, plugin.getConfig().getString("messages.match-start", "Match started! Fight!"));
        }

        Player p1 = arena.getPlayers().get(0);
        Player p2 = arena.getPlayers().get(1);

        if (arena.getSpawn1() != null) p1.teleport(arena.getSpawn1());
        if (arena.getSpawn2() != null) p2.teleport(arena.getSpawn2());

        p1.setHealth(20);
        p2.setHealth(20);
        p1.setFoodLevel(20);
        p2.setFoodLevel(20);
    }

    public void handleDeath(Player loser) {
        Arena arena = playerArena.get(loser.getUniqueId());
        if (arena == null) return;

        Player winner = arena.getOpponent(loser);

        plugin.sendMsg(loser, plugin.getConfig().getString("messages.match-end-lose", "You lost!"));
        if (winner != null) {
            plugin.sendMsg(winner, plugin.getConfig().getString("messages.match-end-win", "You won!"));
        }

        removePlayer(loser, arena, true);
        if (winner != null) removePlayer(winner, arena, false);

        resetArena(arena);
    }

    public void removePlayer(Player player, Arena arena, boolean teleportOut) {
        arena.removePlayer(player);
        playerArena.remove(player.getUniqueId());

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        org.bukkit.inventory.ItemStack[] inv = savedInventories.remove(player.getUniqueId());
        org.bukkit.inventory.ItemStack[] armor = savedArmor.remove(player.getUniqueId());
        if (inv != null) player.getInventory().setContents(inv);
        if (armor != null) player.getInventory().setArmorContents(armor);

        if (teleportOut) {
            org.bukkit.Location spawn = player.getWorld().getSpawnLocation();
            player.teleport(spawn);
        }

        player.setHealth(20);
        player.setFoodLevel(20);
    }

    public void resetArena(Arena arena) {
        arena.setLocked(false);
        arena.getPlayers().clear();
        Material available = Material.valueOf(plugin.getConfig().getString("glass.available", "LIME_STAINED_GLASS"));
        arena.setGlassColor(available);
    }

    public boolean isInMatch(Player player) {
        return playerArena.containsKey(player.getUniqueId());
    }

    public Arena getArena(Player player) {
        return playerArena.get(player.getUniqueId());
    }
}
