package com.primesmp.primebox;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrimeBoxCommand implements CommandExecutor {

    private final PrimeBox plugin;

    public PrimeBoxCommand(PrimeBox plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("primebox.admin")) {
            plugin.sendMsg(player, "<red>No permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (args.length < 2) { plugin.sendMsg(player, "<red>Usage: /primebox create <name>"); return true; }
                if (plugin.getPos1(player) == null || plugin.getPos2(player) == null) {
                    plugin.sendMsg(player, "<red>Set pos1 and pos2 first with /pos1 and /pos2!");
                    return true;
                }
                String name = args[1];
                if (plugin.getArenaManager().get(name) != null) {
                    plugin.sendMsg(player, "<red>Arena " + name + " already exists!");
                    return true;
                }
                plugin.getArenaManager().create(name, plugin.getPos1(player), plugin.getPos2(player));
                plugin.sendMsg(player, "<green>Arena <white>" + name + "<green> created! Now run:");
                plugin.sendMsg(player, "<aqua>/primebox setglass " + name + " <white>— to set glass walls");
                plugin.sendMsg(player, "<aqua>/primebox setspawn " + name + " 1 <white>and <aqua>2 <white>— to set spawns");
            }

            case "remove" -> {
                if (args.length < 2) { plugin.sendMsg(player, "<red>Usage: /primebox remove <name>"); return true; }
                if (!plugin.getArenaManager().remove(args[1])) {
                    plugin.sendMsg(player, "<red>Arena not found: " + args[1]);
                    return true;
                }
                plugin.sendMsg(player, "<green>Arena <white>" + args[1] + "<green> removed.");
            }

            case "setglass" -> {
                if (args.length < 2) { plugin.sendMsg(player, "<red>Usage: /primebox setglass <name>"); return true; }
                Arena arena = plugin.getArenaManager().get(args[1]);
                if (arena == null) { plugin.sendMsg(player, "<red>Arena not found: " + args[1]); return true; }

                if (plugin.getGlassPos1(player) != null && plugin.getGlassPos2(player) != null) {
                    // Use custom glass selection
                    arena.detectGlassBlocks(plugin.getGlassPos1(player), plugin.getGlassPos2(player));
                    plugin.sendMsg(player, "<white>Detected <aqua>" + arena.getGlassBlocks().size() + "<white> glass blocks using custom selection.");
                } else {
                    // Auto detect from bounding box
                    arena.autoDetectGlass();
                    plugin.sendMsg(player, "<white>Auto-detected <aqua>" + arena.getGlassBlocks().size() + "<white> glass blocks from arena bounds.");
                    plugin.sendMsg(player, "<gray>Tip: Use /glasspos1 and /glasspos2 to select glass manually.");
                }

                if (arena.getGlassBlocks().isEmpty()) {
                    plugin.sendMsg(player, "<red>No glass blocks found! Place glass walls first, then run this command.");
                    return true;
                }

                Material available = Material.valueOf(plugin.getConfig().getString("glass.available", "LIME_STAINED_GLASS"));
                arena.setGlassColor(available);
                plugin.getArenaManager().save();
                plugin.sendMsg(player, "<green>Glass walls set and colored lime for arena <white>" + args[1]);
            }

            case "setspawn" -> {
                if (args.length < 3) { plugin.sendMsg(player, "<red>Usage: /primebox setspawn <name> <1|2>"); return true; }
                Arena arena = plugin.getArenaManager().get(args[1]);
                if (arena == null) { plugin.sendMsg(player, "<red>Arena not found: " + args[1]); return true; }
                int num;
                try { num = Integer.parseInt(args[2]); } catch (NumberFormatException e) { plugin.sendMsg(player, "<red>Spawn must be 1 or 2."); return true; }
                if (num == 1) arena.setSpawn1(player.getLocation());
                else if (num == 2) arena.setSpawn2(player.getLocation());
                else { plugin.sendMsg(player, "<red>Spawn must be 1 or 2."); return true; }
                plugin.getArenaManager().save();
                plugin.sendMsg(player, "<green>Spawn <white>" + num + "<green> set for arena <white>" + args[1]);
            }

            case "tp" -> {
                if (args.length < 2) { plugin.sendMsg(player, "<red>Usage: /primebox tp <name>"); return true; }
                Arena arena = plugin.getArenaManager().get(args[1]);
                if (arena == null) { plugin.sendMsg(player, "<red>Arena not found: " + args[1]); return true; }
                if (arena.getSpawn1() != null) player.teleport(arena.getSpawn1());
                else if (arena.getPos1() != null) player.teleport(arena.getPos1());
                else { plugin.sendMsg(player, "<red>Arena has no spawn set."); return true; }
                plugin.sendMsg(player, "<green>Teleported to arena <white>" + args[1]);
            }

            case "list" -> {
                if (plugin.getArenaManager().getAll().isEmpty()) {
                    plugin.sendMsg(player, "<white>No arenas created yet.");
                    return true;
                }
                plugin.sendMsg(player, "<white>Arenas:");
                for (Arena arena : plugin.getArenaManager().getAll()) {
                    String status = arena.isAvailable() ? "<green>Available" : "<red>Occupied";
                    String spawns = (arena.getSpawn1() != null ? "<green>✓" : "<red>✗") + " <white>S1  " +
                                    (arena.getSpawn2() != null ? "<green>✓" : "<red>✗") + " <white>S2  " +
                                    (arena.getGlassBlocks().isEmpty() ? "<red>✗ Glass" : "<green>✓ Glass (" + arena.getGlassBlocks().size() + ")");
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                        "  <gray>- <white>" + arena.getName() + " " + status + "  " + spawns));
                }
            }

            case "reset" -> {
                if (args.length < 2) { plugin.sendMsg(player, "<red>Usage: /primebox reset <name>"); return true; }
                Arena arena = plugin.getArenaManager().get(args[1]);
                if (arena == null) { plugin.sendMsg(player, "<red>Arena not found: " + args[1]); return true; }
                plugin.getMatchManager().resetArena(arena);
                plugin.sendMsg(player, "<green>Arena <white>" + args[1] + "<green> force reset.");
            }

            case "reload" -> {
                plugin.reloadConfig();
                plugin.sendMsg(player, "<green>Config reloaded.");
            }

            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(""));
        plugin.sendMsg(player, "<white><b>PrimeBox Commands</b>");
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
            "  <aqua>/pos1 <gray>& <aqua>/pos2 <white>— Set arena bounding box corners\n" +
            "  <aqua>/glasspos1 <gray>& <aqua>/glasspos2 <white>— Select glass wall area\n" +
            "  <aqua>/primebox create <name> <white>— Create arena from pos1/pos2\n" +
            "  <aqua>/primebox setglass <name> <white>— Set glass walls (uses glasspos or auto)\n" +
            "  <aqua>/primebox setspawn <name> <1|2> <white>— Set spawn points inside arena\n" +
            "  <aqua>/primebox tp <name> <white>— Teleport to arena\n" +
            "  <aqua>/primebox list <white>— List all arenas with status\n" +
            "  <aqua>/primebox reset <name> <white>— Force reset an arena\n" +
            "  <aqua>/primebox reload <white>— Reload config"
        ));
    }
}
