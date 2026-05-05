package fr.liveinground.admin_craft.commands.tools;

import com.mojang.brigadier.CommandDispatcher;
import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.Config;
import fr.liveinground.admin_craft.moderation.SanctionConfig;
import fr.liveinground.admin_craft.storage.types.reports.PlayerReportsData;
import fr.liveinground.admin_craft.storage.types.reports.ReportData;
import fr.liveinground.admin_craft.storage.types.sanction.SanctionData;
import fr.liveinground.admin_craft.storage.types.tools.PlayerHistoryData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.Collection;
import java.util.Date;

public class HistoryCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("history")
                .requires(commandSource -> commandSource.hasPermission(Config.history_level))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> {
                            Collection<NameAndId> profiles = GameProfileArgument.getGameProfiles(ctx, "player");
                            if (profiles.isEmpty()) {
                                ctx.getSource().sendFailure(Component.literal("No player was found"));
                                return 1;
                            }
                            NameAndId targetProfile = profiles.iterator().next();
                            ServerPlayer player = ctx.getSource().getServer().getPlayerList().getPlayer(targetProfile.id());

                            assert player != null;

                            MutableComponent output = Component.literal("");
                            output.append(Component.literal(player.getName().getString() + "'s history:\n").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD, ChatFormatting.UNDERLINE));
                            PlayerHistoryData playerHistory = AdminCraft.playerDataManager.getHistoryFromUUID(player.getStringUUID());
                            PlayerReportsData reportsData = AdminCraft.playerDataManager.getReportDatasByUUID(player.getStringUUID());
                            if (!((playerHistory == null || playerHistory.sanctionList.isEmpty()) && (reportsData == null || reportsData.reports().isEmpty()))) {
                                if (playerHistory!=null && !playerHistory.sanctionList.isEmpty()) {
                                    for (SanctionData data : playerHistory.sanctionList) {
                                        if (data.expiresOn != null) {
                                            if (data.expiresOn.before(new Date())) {
                                                output.append(Component.literal("- " + data.sanctionType.name() + ": ")
                                                                .withStyle(ChatFormatting.DARK_RED))
                                                        .append(Component.literal(data.reason)
                                                                .withStyle(ChatFormatting.YELLOW))
                                                        .append(Component.literal(" (" + data.date.toString() + "), ")
                                                                .withStyle(ChatFormatting.RED))
                                                        .append(Component.literal("expired on " + data.expiresOn.toString())
                                                                .withStyle(ChatFormatting.AQUA));
                                            } else {
                                                output.append(Component.literal("- " + data.sanctionType.name() + ": ")
                                                                .withStyle(ChatFormatting.DARK_RED))
                                                        .append(Component.literal(data.reason)
                                                                .withStyle(ChatFormatting.YELLOW))
                                                        .append(Component.literal(" (" + data.date.toString() + "), ")
                                                                .withStyle(ChatFormatting.RED))
                                                        .append(Component.literal(SanctionConfig.getDurationAsStringFromDate(data.expiresOn))
                                                                .withStyle(ChatFormatting.AQUA));
                                            }
                                        } else {

                                            output.append(Component.literal("- " + data.sanctionType.name() + ": ")
                                                            .withStyle(ChatFormatting.DARK_RED))
                                                    .append(Component.literal(data.reason)
                                                            .withStyle(ChatFormatting.YELLOW))
                                                    .append(Component.literal(" (" + data.date.toString() + ")")
                                                            .withStyle(ChatFormatting.RED));
                                        }
                                        output.append("\n");
                                    }
                                } else {
                                    output.append(Component.literal("This player was never sanctioned.\n").withStyle(ChatFormatting.GREEN));
                                }
                                output.append(Component.literal("Reports:\n").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD, ChatFormatting.UNDERLINE));
                                if (reportsData != null && !reportsData.reports().isEmpty()) {
                                    for (ReportData data: reportsData.reports()) {
                                        output.append(Component.literal("- REPORT: ")
                                                        .withStyle(ChatFormatting.DARK_RED))
                                                .append(Component.literal(data.reason())
                                                        .withStyle(ChatFormatting.YELLOW))
                                                .append(Component.literal(" (reported by " + data.sourceUUID() + ", ")
                                                        .withStyle(ChatFormatting.AQUA))
                                                .append(Component.literal(data.date().toString())
                                                        .withStyle(ChatFormatting.RED))
                                                .append(Component.literal(")\n").withStyle(ChatFormatting.AQUA));
                                    }
                                } else {
                                    output.append(Component.literal("This player was never reported.\n").withStyle(ChatFormatting.GREEN));
                                }
                            } else {
                                output.append(Component.literal("This player has no history").withStyle(ChatFormatting.GREEN));

                            }

                            ctx.getSource().sendSuccess(() -> output, false);

                            return 1;
                        }))
        );
    }
}