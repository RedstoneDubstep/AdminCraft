package fr.liveinground.admin_craft.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.Config;
import fr.liveinground.admin_craft.moderation.CustomSanctionSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;

import java.util.Collection;

public class BanCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean override) {
        String commandName;
        if (override) {
            commandName = "ban";
        } else {
            commandName = "acban";
        }
        dispatcher.register(Commands.literal(commandName)
                .requires(source -> source.hasPermission(Config.ban_level))
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(ctx -> {
                            ban(ctx, "Banned by an operator");
                            return 1;
                        })
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ban(ctx, StringArgumentType.getString(ctx, "reason"));
                                    return 1;
                                })
                        )
                )
        );
    }

    private static void ban(CommandContext<CommandSourceStack> ctx, String reason) throws CommandSyntaxException {
        Collection<NameAndId> collection = GameProfileArgument.getGameProfiles(ctx, "player");
        NameAndId target = AdminCraft.getOneProfile(collection);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("No player was found").withStyle(ChatFormatting.RED));
            return;
        }
        CustomSanctionSystem.banPlayer(ctx.getSource().getServer(), ctx.getSource().getTextName(), target, reason, null, false, null);
        ctx.getSource().sendSuccess(() -> Component.literal("Banned " + target.name() + ": " + reason), true);
    }
}
