package fr.liveinground.admin_craft.commands.tools;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import fr.liveinground.admin_craft.Config;
import fr.liveinground.admin_craft.storage.nbt.PlayerDataLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class InvseeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invsee")
                .requires(commandSource -> commandSource.hasPermission(Config.invsee_level) && commandSource.isPlayer())
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> {
                            ServerPlayer operator = ctx.getSource().getPlayer();
                            assert operator != null;

                            Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                            if (profiles.isEmpty()) {
                                ctx.getSource().sendFailure(Component.literal("No player was found.").withStyle(ChatFormatting.RED));
                                return 1;
                            }
                            GameProfile profile = null;
                            for (GameProfile p: profiles) {
                                if (p != null) {
                                    profile = p;
                                    break;
                                }
                            }

                            if (profile == null) {
                                ctx.getSource().sendFailure(Component.literal("No profile was found.").withStyle(ChatFormatting.RED));
                                return 1;
                            }

                            ServerPlayer onlinePlayer = ctx.getSource().getServer().getPlayerList().getPlayer(profile.getId());

                            if (onlinePlayer == null) {
                                try {
                                    SimpleContainer inv = PlayerDataLoader.loadInventoryFromUUID(ctx.getSource().getLevel(), profile.getId());

                                    if (inv == null) {
                                        ctx.getSource().sendFailure(Component.literal("No data was found.").withStyle(ChatFormatting.RED));
                                        return 1;
                                    }

                                    operator.openMenu(new SimpleMenuProvider((id, ownInv, player) -> new ChestMenu(MenuType.GENERIC_9x6, id, ownInv, inv, 6) {
                                        @Override
                                        public boolean stillValid(@NotNull Player _ignored) {
                                            return true;
                                        }
                                    }, Component.literal(profile.getName() + "'s inventory")));

                                } catch (IOException e) {
                                    ctx.getSource().sendFailure(Component.literal("An error occurred when attempting to load this offline player data's: " + e.getMessage()));
                                    return 1;
                                }
                            } else {
                                Inventory playerInv = onlinePlayer.getInventory();
                                operator.openMenu(new SimpleMenuProvider((id, ownInv, player) -> new ChestMenu(MenuType.GENERIC_9x4, id, ownInv, playerInv, 4) {
                                    @Override
                                    public boolean stillValid(@NotNull Player _ignored) {
                                        return true;
                                    }
                                }, Component.literal(onlinePlayer.getDisplayName().getString() + "'s inventory")));
                            }

                            return 1;
                        })
                )
        );
    }

    private static void saveInventoryToNBT(SimpleContainer inv, CompoundTag tag) {
        ListTag list = new ListTag();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                stack.save(itemTag);
                list.add(itemTag);
            }
        }
        tag.put("Inventory", list);
    }

    private static void saveOfflinePlayerData(ServerLevel level, UUID uuid, CompoundTag tag) throws IOException {
        File dir = level.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile();
        File file = new File(dir, uuid.toString() + ".dat");

        NbtIo.writeCompressed(tag, file);
    }
}