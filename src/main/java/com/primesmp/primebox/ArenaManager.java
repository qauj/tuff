package com.primesmp.primebox;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private final PrimeBox plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();
    private File dataFile;
    private YamlConfiguration dataConfig;

    public ArenaManager(PrimeBox plugin) {
        this.plugin = plugin;
        load();
    }

    public Arena create(String name, Location pos1, Location pos2) {
        Arena arena = new Arena(name, pos1, pos2);
        arenas.put(name.toLowerCase(), arena);
        save();
        return arena;
    }

    public boolean remove(String name) {
        Arena arena = get(name);
        if (arena == null) return false;
        arenas.remove(name.toLowerCase());
        save();
        return true;
    }

    public Arena get(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Collection<Arena> getAll() {
        return arenas.values();
    }

    public Arena getArenaContaining(Location loc) {
        for (Arena arena : arenas.values()) {
            if (arena.contains(loc)) return arena;
        }
        return null;
    }

    public Arena getArenaByGlass(Location loc) {
        for (Arena arena : arenas.values()) {
            if (arena.isGlassBlock(loc)) return arena;
        }
        return null;
    }

    public Arena getArenaByPlayer(org.bukkit.entity.Player player) {
        for (Arena arena : arenas.values()) {
            if (arena.getPlayers().contains(player)) return arena;
        }
        return null;
    }

    public void save() {
        dataConfig.set("arenas", null);
        for (Map.Entry<String, Arena> entry : arenas.entrySet()) {
            dataConfig.set("arenas." + entry.getKey(), entry.getValue().serialize());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save arenas: " + e.getMessage());
        }
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("arenas");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection a = section.getConfigurationSection(key);
            if (a == null) continue;

            Location pos1 = deserializeLoc(a.getConfigurationSection("pos1"));
            Location pos2 = deserializeLoc(a.getConfigurationSection("pos2"));
            if (pos1 == null || pos2 == null) continue;

            Arena arena = new Arena(key, pos1, pos2);

            Location sp1 = deserializeLoc(a.getConfigurationSection("spawn1"));
            Location sp2 = deserializeLoc(a.getConfigurationSection("spawn2"));
            if (sp1 != null) arena.setSpawn1(sp1);
            if (sp2 != null) arena.setSpawn2(sp2);

            if (a.isList("glass")) {
                for (Map<?, ?> glassMap : a.getMapList("glass")) {
                    Location glassLoc = deserializeLocMap(glassMap);
                    if (glassLoc != null) arena.getGlassBlocks().add(glassLoc);
                }
            }

            arenas.put(key.toLowerCase(), arena);
        }
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas.");
    }

    private Location deserializeLoc(ConfigurationSection s) {
        if (s == null) return null;
        World world = plugin.getServer().getWorld(s.getString("world", "world"));
        if (world == null) return null;
        return new Location(world,
            s.getDouble("x"), s.getDouble("y"), s.getDouble("z"),
            (float) s.getDouble("yaw"), (float) s.getDouble("pitch"));
    }

    @SuppressWarnings("unchecked")
    private Location deserializeLocMap(Map<?, ?> map) {
        if (map == null) return null;
        World world = plugin.getServer().getWorld((String) map.get("world"));
        if (world == null) return null;
        return new Location(world,
            ((Number) map.get("x")).doubleValue(),
            ((Number) map.get("y")).doubleValue(),
            ((Number) map.get("z")).doubleValue());
    }
}
