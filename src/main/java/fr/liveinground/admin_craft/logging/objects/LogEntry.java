package fr.liveinground.admin_craft.logging.objects;

import net.minecraft.world.level.block.state.BlockState;

import java.util.Date;

public class LogEntry {
    public final int id;
    public String worldName;
    public int x;
    public int y;
    public int z;
    public InteractionType type;
    public BlockState block;
    public String source;
    public String cause;
    public Date timestamp;
    public int rollbackID;

    public LogEntry(int id, String worldName, int x, int y, int z, InteractionType type, BlockState block, String source, String cause, Date timestamp, int rollbackID) {
        this.id = id;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.block = block;
        this.source = source;
        this.cause = cause;
        this.timestamp = timestamp;
        this.rollbackID = rollbackID;
    }
}
