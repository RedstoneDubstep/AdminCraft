package fr.liveinground.admin_craft.discord;

import fr.liveinground.admin_craft.storage.SanctionDatabase;
import fr.liveinground.admin_craft.storage.types.sanction.SanctionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;

public class BotListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equals("!AC ping")) {
            event.getMessage().reply("AdminCraft loaded and appeal system ready.").queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("status")) {

            String target = event.getOption("target").getAsString();
            if (target.equals("appeals")) {
                if (DiscordBot.enabled) {
                    event.reply("Appeal system is enabled.").queue();
                } else {
                    event.reply("Appeal system is disabled").queue();
                }
            } else if (target.equals("server")) {
                event.reply("Server is online.").queue();
            } else {
                event.reply("'target' argument has to be either 'appeals' or 'server'.").setEphemeral(true).queue();
            }
        }
        else if (event.getName().equals("post_embed")) {

            String title = event.getOption("title").getAsString();
            String description = event.getOption("description").getAsString();
            String emojiName = event.getOption("emoji").getAsString();

            Emoji emoji = Emoji.fromFormatted(emojiName);

            String label = event.getOption("label").getAsString();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(title);
            builder.setDescription(description);
            builder.setColor(Color.CYAN);

            event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(Button.secondary(DiscordBot.INFO_BUTTON_ID, label)
                    .withEmoji(emoji)).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getButton().getId().equals(DiscordBot.INFO_BUTTON_ID)) {
            Modal modal = Modal.create(DiscordBot.INFO_MODAL_ID, "Sanction data")
                    .addActionRow(TextInput.create("id", "Sanction ID", TextInputStyle.SHORT).setPlaceholder("e.g. ").build()) //todo: placeholder and min-max
                    .addActionRow(TextInput.create("ign", "Minecraft username", TextInputStyle.SHORT).setPlaceholder("e.g. Steve").setMinLength(3).setMaxLength(16).build())
                    .build();
            event.replyModal(modal).queue();
        } else if (event.getButton().getId().equals(DiscordBot.APPEAL_BUTTON_ID)) {
            Modal modal = Modal.create(DiscordBot.APPEAL_MODAL_ID, "Appeal form")
                    .addActionRow(TextInput.create("reason", "Appeal reason", TextInputStyle.PARAGRAPH).setPlaceholder("Why do you want to appeal your sanction ?").build())
                    .build();
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getId().equals(DiscordBot.INFO_MODAL_ID)) {
            String id = event.getValue("id").getAsString();
            String ign = event.getValue("ign").getAsString();
            //todo: check id with ign, get data
            if (!SanctionDatabase.isPlayerCurrentlySanctioned(id, ign)) {
                event.reply("No sanction matches this id and this player. Please check your input and try again").setEphemeral(true).queue();
                return;
            }
            SanctionData data;
            String uuid;

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Sanction information");
            embed.setDescription("Please review your sanction's details before appealing.\n:warning: **WARNING:** Any abuse may be punished!");
            embed.setColor(Color.RED);

            embed.addField("IGN", ign, true);
            embed.addField("UUID", uuid, true);
            embed.addField("Sanction ID", id, true);
            embed.addField("Sanction type", data.sanctionType, true);
            embed.addField("Date", data.date, true);
            if (data.expiresOn != null) {
                embed.addField("Expires on", data.expiresOn.toString(), true);
            }

            event.replyEmbeds(embed.build()).addActionRow(Button.success(DiscordBot.APPEAL_BUTTON_ID, "Appeal")).setEphemeral(true).queue();

        } else if (event.getId().equals(DiscordBot.APPEAL_MODAL_ID)) {
            //todo
        }
    }
}
