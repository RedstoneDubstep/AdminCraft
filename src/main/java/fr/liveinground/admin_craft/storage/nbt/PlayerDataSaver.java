package fr.liveinground.admin_craft.storage.nbt;

import fr.liveinground.admin_craft.AdminCraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerDataSaver {
    public static void applyToOnlinePlayer(SimpleContainer container, ServerPlayer player) {
        Inventory inv = player.getInventory();
        inv.clearContent();

        for (int i = 0; i < container.getContainerSize(); i++) {
            inv.setItem(i, container.getItem(i));
        }

        player.inventoryMenu.broadcastChanges();
    }

    public static void saveToNBT(UUID targetUuid, ServerLevel level, SimpleContainer container) {
        try {
            PlayerDataSaver.saveInventory(level, targetUuid, container);
        } catch (IOException e) {
            AdminCraft.LOGGER.error("Failed to save offline inventory", e);
        }
    }

    public static void saveInventory(ServerLevel level,
                                     UUID uuid,
                                     SimpleContainer container) throws IOException {

        File playerDataDir = level.getServer()
                .getWorldPath(LevelResource.PLAYER_DATA_DIR)
                .toFile();

        File file = new File(playerDataDir, uuid.toString() + ".dat");
        if (!file.exists()) return;

        CompoundTag root = NbtIo.readCompressed(file);
        ListTag list = new ListTag();

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag item = new CompoundTag();
                item.putByte("Slot", (byte) i);
                stack.save(item);
                list.add(item);
            }
        }

        root.put("Inventory", list);
        NbtIo.writeCompressed(root, file);
    }
}
