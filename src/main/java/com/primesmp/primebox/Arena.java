package com.primesmp.primebox;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class Arena {

    private final String name;
    private Location pos1;
    private Location pos2;
    private Location spawn1;
    private Location spawn2;
    private final List<Location> glassBlocks = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private boolean locked = false;

    public Arena(String name, Location pos1, Location pos2) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public String getName() { return name; }
    public Location getPos1() { return pos1; }
    public Location getPos2() { return pos2; }
    public Location getSpawn1() { return spawn1; }
    public Location getSpawn2() { return spawn2; }
    public List<Location> getGlassBlocks() { return glassBlocks; }
    public List<Player> getPlayers() { return players; }
    public boolean isLocked() { return locked; }

    public void setSpawn1(Location loc) { this.spawn1 = loc.clone(); }
    public void setSpawn2(Location loc) { this.spawn2 = loc.clone(); }
    public void setLocked(boolean locked) { this.locked = locked; }

    public boolean isAvailable() {
        return !locked && players.size() < 2;
    }

    // Check if location is inside the arena bounding box
    public boolean contains(Location loc) {
        if (pos1 == null || pos2 == null) return false;
        if (!loc.getWorld().equals(pos1.getWorld())) return false;
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        return loc.getX() >= minX && loc.getX() <= maxX
            && loc.getY() >= minY && loc.getY() <= maxY
            && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    // Detect glass blocks within a specific selection (glassPos1/glassPos2)
    public void detectGlassBlocks(Location gp1, Location gp2) {
        glassBlocks.clear();
        if (gp1 == null || gp2 == null) return;
        World world = gp1.getWorld();
        int minX = (int) Math.min(gp1.getX(), gp2.getX());
        int maxX = (int) Math.max(gp1.getX(), gp2.getX());
        int minY = (int) Math.min(gp1.getY(), gp2.getY());
        int maxY = (int) Math.max(gp1.getY(), gp2.getY());
        int minZ = (int) Math.min(gp1.getZ(), gp2.getZ());
        int maxZ = (int) Math.max(gp1.getZ(), gp2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType().name().contains("GLASS")) {
                        glassBlocks.add(block.getLocation());
                    }
                }
            }
        }
    }

    // Auto detect glass on the outer walls of the arena bounding box
    public void autoDetectGlass() {
        detectGlassBlocks(pos1, pos2);
    }

    public void setGlassColor(Material material) {
        for (Location loc : glassBlocks) {
            Block block = loc.getBlock();
            if (block.getType().name().contains("GLASS") || block.getType().name().contains("AIR")) {
                block.setType(material);
            }
        }
    }

    public boolean isGlassBlock(Location loc) {
        for (Location glass : glassBlocks) {
            if (glass.getBlockX() == loc.getBlockX()
                && glass.getBlockY() == loc.getBlockY()
                && glass.getBlockZ() == loc.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    // Check if a location is adjacent to any glass block (for entry detection)
    public boolean isNearGlass(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        for (Location glass : glassBlocks) {
            int gx = glass.getBlockX();
            int gy = glass.getBlockY();
            int gz = glass.getBlockZ();
            if (Math.abs(x - gx) <= 1 && Math.abs(y - gy) <= 1 && Math.abs(z - gz) <= 1) {
                return true;
            }
        }
        return false;
    }

    public void addPlayer(Player player) {
        if (!players.contains(player)) players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public Player getOpponent(Player player) {
        for (Player p : players) {
            if (!p.equals(player)) return p;
        }
        return null;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("pos1", serializeLoc(pos1));
        map.put("pos2", serializeLoc(pos2));
        if (spawn1 != null) map.put("spawn1", serializeLoc(spawn1));
        if (spawn2 != null) map.put("spawn2", serializeLoc(spawn2));
        List<Map<String, Object>> glass = new ArrayList<>();
        for (Location loc : glassBlocks) glass.add(serializeLoc(loc));
        map.put("glass", glass);
        return map;
    }

    private Map<String, Object> serializeLoc(Location loc) {
        if (loc == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("world", loc.getWorld().getName());
        m.put("x", loc.getX());
        m.put("y", loc.getY());
        m.put("z", loc.getZ());
        m.put("yaw", (double) loc.getYaw());
        m.put("pitch", (double) loc.getPitch());
        return m;
    }
}
