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
            plugin.sendMsg(player, "&cNo permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (args.length < 2) { plugin.sendMsg(player, "Usage: /primebox create <name>"); return true; }
                if (plugin.getPos1(player) == null || plugin.getPos2(player) == null) {
                    plugin.sendMsg(player, plugin.getConfig().getString("messages.no-pos", "Set pos1 and pos2 first!"));
                    return true;
                }
                String name = args[1];
                if (plugin.getArenaManager().get(name) != null) {
                    plugin.sendMsg(player, "Arena " + name + " already exists!");
                    return true;
                }
                Arena arena = plugin.getArenaManager().create(name, plugin.getPos1(player), plugin.getPos2(player));
                plugin.sendMsg(player, plugin.getConfig().getString("messages.arena-created", "Arena <arena> created!").replace("<arena>", name));
            }

            case "remove" -> {
                if (args.length < 2) { plugin.sendMsg(player, "Usage: /primebox remove <name>"); return true; }
                String name = args[1];
                if (!plugin.getArenaManager().remove(name)) {
                    plugin.sendMsg(player, plugin.getConfig().getString("messages.arena-not-found", "Arena not found.").replace("<arena>", name));
                    return true;
                }
                plugin.sendMsg(player, plugin.getConfig().getString("messages.arena-removed", "Arena removed.").replace("<arena>", name));
            }

            case "setglass" -> {
                if (args.length < 2) { plugin.sendMsg(player, "Usage: /primebox setglass <name>"); return true; }
                Arena arena = plugin.getArenaManager().get(args[1]);
                if (arena == null) { plugin.sendMsg(player, "Arena not found."); return true; }
                arena.detectGlassBlocks();
                Material available = Material.valueOf(plugin.getConfig().getString("glass.available", "LIME_STAINED_GLASS"));
                arena.setGlassColor(available);
                plugin.getArenaManager().save();
                plugin.sendMsg(player, plugin.getConfig().getString("messages.glass-set", "Glass set for <arena>.").replace("<arena>", args[1]));
            }

            case "setspawn" -> {
                if (args.length < 3) { plugin.sendMsg(player, "Usage: /primebox setspawn <name> <1|2>"); return true; }
                Arena arena = plugin.getArenaManager().get(args[1]);
                if (arena == null) { plugin.sendMsg(player, "Arena not found."); return true; }
                int num = Integer.parseInt(args[2]);
                if (num == 1) arena.setSpawn1(player.getLocation());
                else if (num == 2) arena.setSpawn2(player.getLocation());
                else { plugin.sendMsg(player, "Spawn number must be 1 or 2."); return true; }
                plugin.getArenaManager().save();
                plugin.sendMsg(player, plugin.getConfig().getString("messages.spawn-set", "Spawn set.")
                    .replace("<num>", String.valueOf(num)).replace("<arena>", args[1]));
            }

            case "tp" -> {
                if (args.length < 2) { plugin.sendMsg(player, "Usage: /primebox tp <name>"); return true; }
                Arena arena = plugin.getArenaManager().get(args[1]);
                if (arena == null) { plugin.sendMsg(player, "Arena not found."); return true; }
                if (arena.getSpawn1() != null) player.teleport(arena.getSpawn1());
                else if (arena.getPos1() != null) player.teleport(arena.getPos1());
                plugin.sendMsg(player, "Teleported to arena " + args[1] + ".");
            }

            case "list" -> {
                plugin.sendMsg(player, "&fArenas:");
                for (Arena arena : plugin.getArenaManager().getAll()) {
                    String status = arena.isAvailable() ? "&aAvailable" : "&cOccupied";
                    player.sendMessage("  &7- &f" + arena.getName() + " " + status);
                }
            }

            case "reload" -> {
                plugin.reloadConfig();
                plugin.sendMsg(player, "Config reloaded.");
            }

            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        plugin.sendMsg(player, "&fCommands:");
        player.sendMessage("  &b/primebox create <name>");
        player.sendMessage("  &b/primebox remove <name>");
        player.sendMessage("  &b/primebox setglass <name>");
        player.sendMessage("  &b/primebox setspawn <name> <1|2>");
        player.sendMessage("  &b/primebox tp <name>");
        player.sendMessage("  &b/primebox list");
        player.sendMessage("  &b/primebox reload");
        player.sendMessage("  &b/pos1  /pos2");
        player.sendMessage("");
    }
}
