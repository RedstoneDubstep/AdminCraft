package fr.liveinground.admin_craft.commands.moderation;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.Config;
import fr.liveinground.admin_craft.PlaceHolderSystem;
import fr.liveinground.admin_craft.moderation.CustomSanctionSystem;
import fr.liveinground.admin_craft.moderation.SanctionConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

public class MuteCommand {

    private MuteCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("mute")
                .requires(commandSource -> commandSource.hasPermission(Config.mute_level))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> {
                            mute(ctx, null, null);
                            return 1;
                        })
                        .then(Commands.argument("args", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> {
                                    String remaining = builder.getRemaining();
                                    if (!remaining.contains("--noappeal")) {
                                        builder.suggest("--noappeal");
                                    }
                                    if (!remaining.contains("--delayedappeal:")) {
                                        builder.suggest("--delayedappeal:");
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    mute(ctx, StringArgumentType.getString(ctx, "args"), null);
                                    return 1;
                                }))));

        dispatcher.register(Commands.literal("unmute")
                .requires(source -> source.hasPermission(Config.mute_level))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> {
                            Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                            if (!profiles.isEmpty()) {

                                NameAndId targetProfile = profiles.iterator().next();

                                if (!AdminCraft.mutedPlayersUUID.contains(targetProfile.id().toString())) {
                                    String msg = PlaceHolderSystem.replacePlaceholders(Config.unmute_failed_not_muted, Map.of("player", targetProfile.name()));
                                    Component messageToOperator = Component.literal(msg);
                                    ctx.getSource().sendFailure(messageToOperator);
                                    return 1;
                                }

                                CustomSanctionSystem.unMutePlayer(targetProfile);

                                String msg = PlaceHolderSystem.replacePlaceholders(Config.unmute_success, Map.of("player", targetProfile.name()));
                                Component messageToOperator = Component.literal(msg);
                                ctx.getSource().sendSuccess(() -> messageToOperator, true);
                            } else {
                                ctx.getSource().sendFailure(Component.literal("No player with this username was found."));
                            }
                            return 1;
                        })
                ));

        dispatcher.register(Commands.literal("tempmute")
                .requires(commandSource -> commandSource.hasPermission(Config.mute_level))
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .then(Commands.argument("duration", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String duration = StringArgumentType.getString(ctx, "duration");
                                            mute(ctx, "Muted by an operator", duration);
                                            return 1;
                                        })
                                        .then(Commands.argument("args", StringArgumentType.greedyString())
                                                .suggests((ctx, builder) -> {
                                                    String remaining = builder.getRemaining();
                                                    if (!remaining.contains("--noappeal")) {
                                                        builder.suggest("--noappeal");
                                                    }
                                                    if (!remaining.contains("--delayedappeal:")) {
                                                        builder.suggest("--delayedappeal:");
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> {
                                                    String duration = StringArgumentType.getString(ctx, "duration");
                                                    mute(ctx, StringArgumentType.getString(ctx, "args"), duration);
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private static void mute(@NotNull CommandContext<CommandSourceStack> ctx, String args, @Nullable String duration) throws CommandSyntaxException {
        NameAndId profile = AdminCraft.getOneProfile(GameProfileArgument.getGameProfiles(ctx, "player"));
        if (profile == null) {
            ctx.getSource().sendFailure(Component.literal("No player was found").withStyle(ChatFormatting.RED));
            return;
        }
        if (AdminCraft.mutedPlayersUUID.contains(String.valueOf(profile.id()))) {
            ctx.getSource().sendFailure(Component.literal(PlaceHolderSystem.replacePlaceholders(Config.mute_failed_already_muted, Map.of("player", profile.name()))));
            return;
        }
        Date sanctionDuration = SanctionConfig.getDurationAsDate(duration);
        if (sanctionDuration == null) {
            ctx.getSource().sendFailure(Component.literal("Invalid format for the sanction duration.").withStyle(ChatFormatting.RED));
            return;
        }

        String reason;
        boolean appealable = true;
        Date appealDelay = null;
        String[] splitted = args.split(" ");
        StringBuilder builder = new StringBuilder();

        for (String i: splitted) {
            if (i.equals("--noappeal")) appealable = false;
            else if (i.contains("--delayedappeal:")) {
                String[] delaysplit = i.split(":");
                if (delaysplit.length == 2) {
                    appealDelay = SanctionConfig.getDurationAsDate(delaysplit[1]);
                    if (appealDelay == null) {
                        ctx.getSource().sendFailure(Component.literal("Invalid duration format for the appeal delay.").withStyle(ChatFormatting.RED));
                        return;
                    }
                }
            } else builder.append(" ").append(i);
        }
        if (!builder.isEmpty()) {
            builder.delete(0, 0);
            reason = builder.toString();
        }
        else {
            reason = "Muted by an operator";
        }
        CustomSanctionSystem.mutePlayer(ctx.getSource().getServer(), profile, reason, sanctionDuration, appealable, appealDelay);

        String msgToOperator = PlaceHolderSystem.replacePlaceholders(Config.mute_success, Map.of("player", profile.name(), "reason", reason));
        ctx.getSource().sendSuccess(() -> Component.literal(msgToOperator), true);
    }
}