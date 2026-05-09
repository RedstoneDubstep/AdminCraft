package fr.liveinground.admin_craft.moderation;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.Config;
import fr.liveinground.admin_craft.PlaceHolderSystem;
import fr.liveinground.admin_craft.storage.SanctionDatabase;
import fr.liveinground.admin_craft.storage.types.PlayerMuteData;
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

//todo: remove unused blocks
//todo: check mute method (what if player offline...?)

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

            ServerPlayer serverPlayer = AdminCraft.getOnlinePlayer(server, player);
            if (serverPlayer != null) {
                String msg = PlaceHolderSystem.replacePlaceholders(Config.mute_message, Map.of("reason", reason));
                serverPlayer.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.RED));

                if (expiresOn != null) {
                    String timeMsg = SanctionConfig.getDurationAsStringFromDate(expiresOn);
                    serverPlayer.sendSystemMessage(Component.literal(timeMsg).withStyle(ChatFormatting.YELLOW));
                }
            }
        }
        AdminCraft.playerDataManager.addMuteEntry(new PlayerMuteData(player.name(), player.id().toString(), reason, expiresOn));
        return SanctionDatabase.registerSanction(player.id().toString(), player.name(), new SanctionData(Sanction.MUTE, reason, new Date(), expiresOn), appealable, appealDelay);
    }

    public static void unMutePlayer(ServerPlayer player) {
        if (AdminCraft.mutedPlayersUUID.contains(player.getStringUUID())) {
            AdminCraft.playerDataManager.removeMuteEntry(AdminCraft.playerDataManager.getPlayerMuteDataByUUID(player.getStringUUID()));
            Component messageComponent = Component.literal(Config.unmute_message).withStyle(ChatFormatting.GREEN);
            player.sendSystemMessage(messageComponent);
        }
    }

    public static void warnPlayer(ServerPlayer player, @Nullable String reason, @Nullable String operator) {
        if (operator == null) {
            operator = "The Great Server";
        }
        if (reason == null) {
            reason = "Warned by an operator";
        }
        Component title = Component.literal(Config.warn_title).withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        Component message = Component.literal(PlaceHolderSystem.replacePlaceholders(Config.warn_message,
                Map.of("operator", operator,
                        "reason", reason))).withStyle(ChatFormatting.YELLOW);

        player.sendSystemMessage(Component.literal("---------------------------------------------").withStyle(ChatFormatting.DARK_RED));
        player.sendSystemMessage(title);
        player.sendSystemMessage(message);
        player.sendSystemMessage(Component.literal("---------------------------------------------").withStyle(ChatFormatting.DARK_RED));

        SanctionDatabase.registerSanction(player.getStringUUID(), player.getPlainTextName(), new SanctionData(Sanction.WARN, reason, new Date(), null), false, null);
        //AdminCraft.playerDataManager.addSanction(player.getStringUUID(), Sanction.WARN, reason, null);
    }
}
