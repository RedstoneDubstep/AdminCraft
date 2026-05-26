package fr.liveinground.admin_craft.moderation;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.Config;
import fr.liveinground.admin_craft.ServerHolder;
import fr.liveinground.admin_craft.discord.DiscordBot;
import fr.liveinground.admin_craft.lang.LangManager;
import fr.liveinground.admin_craft.lang.TrKeys;
import fr.liveinground.admin_craft.storage.SanctionDatabase;
import fr.liveinground.admin_craft.storage.types.PlayerMuteData;
import fr.liveinground.admin_craft.storage.types.sanction.AppealStatus;
import fr.liveinground.admin_craft.storage.types.sanction.DatabaseSanctionData;
import fr.liveinground.admin_craft.storage.types.sanction.Sanction;
import fr.liveinground.admin_craft.storage.types.sanction.SanctionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CustomSanctionSystem {
    public static String banPlayer(MinecraftServer server, String source, NameAndId player, String reason, @Nullable Date expiresOn, boolean appealable, @Nullable Date appealDelay) {
        PlayerList playerList = server.getPlayerList();
        UserBanList banList = playerList.getBans();
        if (banList.isBanned(player)) {
            return null;
        }
        UserBanListEntry banEntry = new UserBanListEntry(player, null, source, expiresOn, reason);
        banList.add(banEntry);

        ServerPlayer serverPlayer = AdminCraft.getOnlinePlayer(server, player);
        if (serverPlayer != null) {
            serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            MutableComponent banMessage = Component.literal("");
            banMessage.append(Component.literal("You are now banned on this server.\nReason » ").withStyle(ChatFormatting.RED));
            banMessage.append(Component.literal(reason + "\n").withStyle(ChatFormatting.YELLOW));
            if (expiresOn == null) {
                banMessage.append(Component.literal("This is a permanent ban.\n")).withStyle(ChatFormatting.RED);
            } else {
                banMessage.append(Component.literal("This sanction will end on ").withStyle(ChatFormatting.RED));
                banMessage.append(Component.literal(expiresOn + "\n").withStyle(ChatFormatting.YELLOW));
            }

            //serverPlayer.connection.disconnect(banMessage);

        }
        return SanctionDatabase.registerSanction(player.id().toString(), player.name(), new SanctionData(Sanction.BAN, reason, new Date(), expiresOn), appealable, appealDelay);
        //AdminCraft.playerDataManager.addSanction(String.valueOf(player.id()), Sanction.BAN, reason, expiresOn);
    }
    /*
    public static String banPlayer(MinecraftServer server, String source, Collection<NameAndId> player, String reason, @Nullable Date expiresOn, boolean appealable, @Nullable Date appealDelay) {
        PlayerList playerList = server.getPlayerList();
        UserBanList banList = playerList.getBans();
        for (NameAndId profile: player) {
            if (!banList.isBanned(profile)) {
                UserBanListEntry banEntry = new UserBanListEntry(profile, null, source, expiresOn, reason);
                banList.add(banEntry);
                ServerPlayer serverPlayer = server.getPlayerList().getPlayer(profile.id());
                if (serverPlayer != null) serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }
            return SanctionDatabase.registerSanction(profile.id().toString(), profile.name(), new SanctionData(Sanction.BAN, reason, new Date(), expiresOn), appealable, appealDelay);
            //AdminCraft.playerDataManager.addSanction(profile.id().toString(), Sanction.BAN, reason, expiresOn);
        }
        return null;
    }*/

    public static void kickPlayer(ServerPlayer player, String reason) {
        player.connection.disconnect(Component.literal(reason).withStyle(ChatFormatting.RED));
        SanctionDatabase.registerSanction(player.getStringUUID(), player.getPlainTextName(), new SanctionData(Sanction.KICK, reason, new Date(), null), false, null);
        //AdminCraft.playerDataManager.addSanction(player.getStringUUID(), Sanction.KICK, reason, null);
    }

    public static String mutePlayer(MinecraftServer server, NameAndId player, String reason, @Nullable Date expiresOn, boolean appealable, @Nullable Date appealDelay) {
        if (!AdminCraft.mutedPlayersUUID.contains(String.valueOf(player))) {
            //AdminCraft.playerDataManager.addMuteEntry(new PlayerMuteData(player.name(), player.id().toString(), reason, expiresOn));
            String id = SanctionDatabase.registerSanction(player.id().toString(), player.name(), new SanctionData(Sanction.MUTE, reason, new Date(), expiresOn), appealable, appealDelay);
            if (id == null) return null;
            ServerPlayer serverPlayer = AdminCraft.getOnlinePlayer(server, player);
            if (serverPlayer != null) {
                String msg;
                if (DiscordBot.enabled) {
                    msg = LangManager.tr(TrKeys.MUTE_BEGINS_REASON, Map.of("reason", reason, "id", id, "link", Config.invite_link));
                } else {
                    msg = LangManager.tr(TrKeys.MUTE_BEGINS_REASON_NOAPPEAL, Map.of("reason", reason));
                }
                serverPlayer.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.RED));

                if (expiresOn != null) {
                    String timeMsg = SanctionConfig.getDurationAsStringFromDate(expiresOn);
                    serverPlayer.sendSystemMessage(Component.literal(timeMsg).withStyle(ChatFormatting.YELLOW));
                }
            }
            AdminCraft.playerDataManager.addMuteEntry(new PlayerMuteData(player.name(), player.id().toString(), reason, expiresOn));
            return id;
        }
        return null;
    }

    public static void unMutePlayer(NameAndId player) {
        if (AdminCraft.mutedPlayersUUID.contains(player.id().toString())) {
            AdminCraft.playerDataManager.removeMuteEntry(AdminCraft.playerDataManager.getPlayerMuteDataByUUID(player.id().toString()));
            try {
                ServerPlayer serverPlayer = ServerHolder.getServer().getPlayerList().getPlayer(player.id());

                if (serverPlayer != null) {
                    Component messageComponent = Component.literal(LangManager.tr(TrKeys.MUTE_ENDS)).withStyle(ChatFormatting.GREEN);
                    serverPlayer.sendSystemMessage(messageComponent);
                }
            } catch (IllegalStateException e) {
                AdminCraft.LOGGER.error("Failed to display a message to the unmuted player {}", player.name(), e);
            }
        }
    }

    public static void warnPlayer(ServerPlayer player, @Nullable String reason, @Nullable String operator) {
        if (operator == null) {
            operator = "The Great Server";
        }
        if (reason == null) {
            reason = "Warned by an operator";
        }
        Component title = Component.literal(LangManager.tr(TrKeys.WARN_TITLE)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        Component message = Component.literal(LangManager.tr(TrKeys.WARN_MESSAGE, Map.of("operator", operator, "reason", reason)))
                .withStyle(ChatFormatting.YELLOW);

        player.sendSystemMessage(Component.literal("---------------------------------------------").withStyle(ChatFormatting.DARK_RED));
        player.sendSystemMessage(title);
        player.sendSystemMessage(message);
        player.sendSystemMessage(Component.literal("---------------------------------------------").withStyle(ChatFormatting.DARK_RED));

        SanctionDatabase.registerSanction(player.getStringUUID(), player.getPlainTextName(), new SanctionData(Sanction.WARN, reason, new Date(), null), false, null);
        //AdminCraft.playerDataManager.addSanction(player.getStringUUID(), Sanction.WARN, reason, null);
    }

    public static boolean applyAppealToSanction(DatabaseSanctionData data, AppealStatus status) {
        if (SanctionDatabase.changeAppealStatus(data.id(), status)) {
            NameAndId nameAndId = new NameAndId(UUID.fromString(data.uuid()), data.ign());
            switch (data.type()) {
                case MUTE -> unMutePlayer(nameAndId);
                case BAN -> ServerHolder.getServer().getPlayerList().getBans().remove(nameAndId);
                default -> AdminCraft.LOGGER.warn("An appeal was made for a non-appealable sanction type (sanction {}). Skipping appeal success procedure...", data.id());
            }
            return true;
        } else {
            AdminCraft.LOGGER.error("Failed to update appeal status for sanction id {} to {} in the database", data.id(), status.toString());
            return false;
        }
    }

    public static boolean changeDuration(@NotNull DatabaseSanctionData data, @Nullable Date newExpires) {
        NameAndId nameAndId = new NameAndId(UUID.fromString(data.id()), data.ign());
        if (SanctionDatabase.changeAppealStatus(data.id(), AppealStatus.REDUCED)) {
            switch (data.type()) {
                case MUTE -> changeMuteDuration(nameAndId, newExpires);
                case BAN -> changeBanDuration(nameAndId, newExpires);
                default -> AdminCraft.LOGGER.warn("An appeal was made for a non-appealable sanction type (sanction {}). Skipping sanction reducing procedure...", data.id());
            }
            if (!SanctionDatabase.editDuration(data.id(), newExpires)) AdminCraft.LOGGER.warn("Failed to edit the duration in the database. This wont impact the actual sanction.");
            return true;
        }
        AdminCraft.LOGGER.error("Failed to update appeal status for sanction id {} to REDUCED in the database", data.id());
        return false;
    }

    public static void changeMuteDuration(@NotNull NameAndId player, @Nullable Date newExpires) {
        if (AdminCraft.mutedPlayersUUID.contains(player.id().toString())) {
            PlayerMuteData oldData = AdminCraft.playerDataManager.getPlayerMuteDataByUUID(player.id().toString());
            AdminCraft.playerDataManager.removeMuteEntry(oldData);
            AdminCraft.playerDataManager.addMuteEntry(new PlayerMuteData(player.name(), player.id().toString(), Objects.requireNonNull(oldData).reason, newExpires));
            ServerPlayer serverPlayer = ServerHolder.getServer().getPlayerList().getPlayer(player.id());
            if (serverPlayer != null) {
                serverPlayer.sendSystemMessage(Component.literal("Your mute duration has been changed. It will now expire in " + SanctionConfig.getDurationAsStringFromDate(newExpires)).withStyle(ChatFormatting.GREEN));
            }
        }
    }

    public static void changeBanDuration(NameAndId player, @Nullable Date newExpires) {
        PlayerList list = ServerHolder.getServer().getPlayerList();
        if (list.getBans().isBanned(player)) {
            UserBanListEntry old = Objects.requireNonNull(list.getBans().get(player));
            list.getBans().remove(player);
            list.getBans().add(new UserBanListEntry(player, old.getCreated(), old.getSource(), newExpires, old.getReason()));
        }
    }
}
