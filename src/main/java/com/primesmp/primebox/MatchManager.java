package com.primesmp.primebox;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MatchManager {

    private final PrimeBox plugin;
    private final Map<UUID, Arena> playerArena = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();

    public MatchManager(PrimeBox plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player, Arena arena) {
        // Save inventory
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents().clone());
        savedArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());
        playerArena.put(player.getUniqueId(), arena);
        arena.addPlayer(player);

        plugin.sendMsg(player, "<white>Waiting for opponent...");

        if (arena.getPlayers().size() == 2) {
            startMatch(arena);
        }
    }

    public void startMatch(Arena arena) {
        arena.setLocked(true);
        Material occupied = Material.valueOf(plugin.getConfig().getString("glass.occupied", "RED_STAINED_GLASS"));
        arena.setGlassColor(occupied);

        Player p1 = arena.getPlayers().get(0);
        Player p2 = arena.getPlayers().get(1);

        // Clear and teleport
        clearPlayer(p1);
        clearPlayer(p2);

        if (arena.getSpawn1() != null) p1.teleport(arena.getSpawn1());
        if (arena.getSpawn2() != null) p2.teleport(arena.getSpawn2());

        p1.setHealth(p1.getMaxHealth());
        p2.setHealth(p2.getMaxHealth());
        p1.setFoodLevel(20);
        p2.setFoodLevel(20);
        p1.setSaturation(20);
        p2.setSaturation(20);

        plugin.sendMsg(p1, "<green><b>FIGHT!</b> <white>Good luck!");
        plugin.sendMsg(p2, "<green><b>FIGHT!</b> <white>Good luck!");

        plugin.getServer().broadcastMessage("");
    }

    private void clearPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        player.setExp(0);
        player.setLevel(0);
    }

    public void handleDeath(Player loser) {
        Arena arena = playerArena.get(loser.getUniqueId());
        if (arena == null) return;

        Player winner = arena.getOpponent(loser);

        plugin.sendMsg(loser, "<red>You lost the match! Better luck next time.");
        if (winner != null) {
            plugin.sendMsg(winner, "<gold><b>You won the match!</b> <white>Well played!");
        }

        // Restore and remove loser
        restoreAndRemove(loser, arena, true);

        // Restore and remove winner (stays in position, no teleport)
        if (winner != null) {
            restoreAndRemove(winner, arena, false);
        }

        resetArena(arena);
    }

    private void restoreAndRemove(Player player, Arena arena, boolean teleportOut) {
        arena.removePlayer(player);
        playerArena.remove(player.getUniqueId());

        // Restore inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        ItemStack[] inv = savedInventories.remove(player.getUniqueId());
        ItemStack[] armor = savedArmor.remove(player.getUniqueId());
        if (inv != null) player.getInventory().setContents(inv);
        if (armor != null) player.getInventory().setArmorContents(armor);

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        if (teleportOut) {
            player.teleport(player.getWorld().getSpawnLocation());
        }
    }

    public void removePlayer(Player player, Arena arena, boolean teleportOut) {
        restoreAndRemove(player, arena, teleportOut);
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
