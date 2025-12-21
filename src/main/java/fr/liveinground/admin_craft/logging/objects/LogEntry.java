package fr.liveinground.admin_craft.logging.objects;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Date;

public class LogEntry {
    public final int id;
    public String worldName;
    public int x;
    public int y;
    public int z;
    public InteractionType type;
    public Block material;
    public String playerUUID;
    public Date timestamp;

    public LogEntry(int id, String worldName, int x, int y, int z, InteractionType type, Block material, String playerUUID, Date timestamp) {
        this.id = id;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.material = material;
        this.playerUUID = playerUUID;
        this.timestamp = timestamp;
    }
}
