package fr.liveinground.admin_craft.discord;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.Config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordBot {
    public static JDA jda;
    public static boolean enabled;
    public static Guild guild;

    public static final String INFO_BUTTON_ID = "info_button";
    public static final String APPEAL_BUTTON_ID = "appeal_button";
    public static final String INFO_MODAL_ID = "info_modal";
    public static final String APPEAL_MODAL_ID = "appeal_modal";

    public static Map<String, List<String>> playerCache = new HashMap<>();

    public static void start() {
        AdminCraft.LOGGER.info("Starting appeal bot...");
        if (Config.enable_appeals && !Config.bot_token.equals("configthisplease")) {
            JDABuilder builder = JDABuilder.createDefault(Config.bot_token);
            builder.addEventListeners(new BotListener());
            jda = builder.build();
            register_commands();
            try {
                jda.awaitReady();
                AdminCraft.LOGGER.info("Appeal system enabled!");
                enabled = true;
            } catch (InterruptedException e) {
                AdminCraft.LOGGER.error("Error while starting appeal bot: {}", String.valueOf(e));
                enabled = false;
            }

        } else {
            if (!Config.enable_appeals) {
                AdminCraft.LOGGER.info("Appeal system disabled, skipping...");
            } else {
                AdminCraft.LOGGER.warn("Bot token was not configured, skipping...");
            }
            enabled = false;
        }
    }

    private static void register_commands() {
        guild.updateCommands().addCommands(Commands.slash("status", "Get bot or server status")
                .addOption(OptionType.STRING, "target", "What do you want to get the status of?", true)).queue();
        guild.updateCommands().addCommands(Commands.slash("post_embed", "Makes the bot send the appeal embed")
                .addOption(OptionType.STRING, "title", "The embed title", true)
                .addOption(OptionType.STRING, "description", "The embed description", true)
                .addOption(OptionType.STRING, "emoji", "The button emoji", true)
                .addOption(OptionType.STRING, "label", "The button label", true)).queue();
    }
}
