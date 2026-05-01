package fr.liveinground.admin_craft.discord;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.storage.SanctionDatabase;
import fr.liveinground.admin_craft.storage.types.sanction.AppealStatus;
import fr.liveinground.admin_craft.storage.types.sanction.SanctionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
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
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

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
            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply("You don't have the permission to run this command. Required node: **Administrator**.").setEphemeral(true).queue();
                return;
            }

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
                    .addActionRow(TextInput.create("id", "Sanction ID", TextInputStyle.SHORT).setPlaceholder("e.g. ").setRequired(true).build()) //todo: placeholder and min-max
                    .addActionRow(TextInput.create("ign", "Minecraft username", TextInputStyle.SHORT).setPlaceholder("e.g. Steve").setRequired(true).setMinLength(3).setMaxLength(16).build())
                    .build();
            event.replyModal(modal).queue();
        } else if (event.getButton().getId().equals(DiscordBot.APPEAL_BUTTON_ID)) {
            Modal modal = Modal.create(DiscordBot.APPEAL_MODAL_ID, "Appeal form")
                    .addActionRow(TextInput.create("reason", "Appeal reason", TextInputStyle.PARAGRAPH).setPlaceholder("Why do you want to appeal your sanction ?").setRequired(true).build())
                    .build();
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getId().equals(DiscordBot.INFO_MODAL_ID)) {
            String id = event.getValue("id").getAsString();
            String ign = event.getValue("ign").getAsString();
            if (!SanctionDatabase.isPlayerCurrentlySanctioned(id, ign)) {
                event.reply("No sanction matches this id and this player. Please check your input and try again").setEphemeral(true).queue();
                return;
            }
            Map<UUID, SanctionData> map = SanctionDatabase.getSanctionData(id, ign);
            if (map == null) {
                event.reply("No sanction matches this id and this player. Please check your input and try again").setEphemeral(true).queue();
                return;
            }
            UUID uuid=null;
            for (UUID i: map.keySet()) {
                uuid = i;
            }
            if (uuid == null) {
                event.reply("No sanction matches this id and this player. Please check your input and try again").setEphemeral(true).queue();
                return;
            }
            SanctionData data = map.get(uuid);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Sanction information");
            embed.setDescription("Please review your sanction's details before appealing.\n:warning: **WARNING:** Any abuse may be punished!");
            embed.setColor(Color.RED);

            embed.addField("IGN", ign, true);
            embed.addField("UUID", uuid.toString(), true);
            embed.addField("Sanction ID", id, true);
            embed.addField("Sanction type", data.sanctionType.toString(), true);
            embed.addField("Reason", data.reason, true);
            embed.addField("Date", data.date.toString(), true);
            if (data.expiresOn != null) {
                embed.addField("Expires on", data.expiresOn.toString(), true);
            }
            AppealStatus status = SanctionDatabase.getAppealStatus(id);
            if (status == null) {
                AdminCraft.LOGGER.error("Couldn't get appeal status for sanction id {} getAppealStatus returned null: ", id);
                event.reply("An issue occurred. Please check your input and try again.").setEphemeral(true).queue();
                return;
            }
            Date delay = null;
            embed.addField("Appeal status", status.status(), true);
            if (status.equals(AppealStatus.DELAYED)) {
                delay = SanctionDatabase.getAppealDelay(id);
                if (delay == null) {
                    AdminCraft.LOGGER.error("Could not get appeal delay for sanction id {}", id);
                    event.reply("An issue occurred. Please check your input and try again.").setEphemeral(true).queue();
                    return;
                }
                if (delay.before(new Date())) {
                    status = AppealStatus.NOT_REQUESTED;
                    SanctionDatabase.changeAppealStatus(id, status);
                }
            }
            embed.addField("Appeal status", status.status(), true);
            if (delay != null && status.equals(AppealStatus.DELAYED)) {
                embed.addField("Appeal delay", "You will be able to appeal after this date: " + delay, true);
            }
            event.replyEmbeds(embed.build()).addActionRow(Button.success(DiscordBot.APPEAL_BUTTON_ID, "Appeal").withDisabled(!status.equals(AppealStatus.NOT_REQUESTED))).setEphemeral(true).queue();
            List<String> cache = new ArrayList<>();
            cache.add(id);
            cache.add(uuid.toString());
            cache.add(ign);
            DiscordBot.playerCache.put(event.getMember().getId(), cache);

        } else if (event.getId().equals(DiscordBot.APPEAL_MODAL_ID)) {
            Member member = event.getMember();
            if (member == null) {
                AdminCraft.LOGGER.error("An interaction failed, because event.getMember() is null.");
                event.reply("An issue occurred.").setEphemeral(true).queue();
                return;
            }
            String id = DiscordBot.playerCache.get(member.getId()).getFirst();
            String uuidStr = DiscordBot.playerCache.get(member.getId()).get(1);
            String ign = DiscordBot.playerCache.get(member.getId()).get(2);
            if (id == null) {
                event.reply("AdminCraft cache was reset. Please submit your sanction id again.").setEphemeral(true).queue();
                return;
            }
            String appealReason = event.getInteraction().getValue("reason").getAsString();

            Map<UUID, SanctionData> map = SanctionDatabase.getSanctionData(id, ign);
            if (map == null) {
                event.reply("Could not get sanction information. Try again later.").setEphemeral(true).queue();
                return;
            }
            UUID uuid=UUID.fromString(uuidStr);
            SanctionData data = map.get(uuid);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("New appeal");
            embed.setDescription("A new appeal was submitted for review.");
            embed.setColor(Color.RED);

            embed.addField("IGN", ign, true);
            embed.addField("UUID", uuid.toString(), true);
            embed.addField("Sanction ID", id, true);
            embed.addField("Sanction type", data.sanctionType.toString(), true);
            embed.addField("Reason", data.reason, true);
            embed.addField("Date", data.date.toString(), true);
            if (data.expiresOn != null) {
                embed.addField("Expires on", data.expiresOn.toString(), true);
            }
            embed.addField("Player's message", appealReason, true);
            embed.addField("Discord user", member.getAsMention() + " (" + member.getEffectiveName() + ")", true);

            //todo: create channel
            //todo: send embed with buttons
        }
    }
}
