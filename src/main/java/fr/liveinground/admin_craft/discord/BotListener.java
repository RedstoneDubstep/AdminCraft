package fr.liveinground.admin_craft.discord;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.Config;
import fr.liveinground.admin_craft.moderation.CustomSanctionSystem;
import fr.liveinground.admin_craft.moderation.SanctionConfig;
import fr.liveinground.admin_craft.storage.SanctionDatabase;
import fr.liveinground.admin_craft.storage.types.sanction.AppealStatus;
import fr.liveinground.admin_craft.storage.types.sanction.Sanction;
import fr.liveinground.admin_craft.storage.types.sanction.SanctionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BotListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().equals("!AC ping") && event.isFromGuild() && event.getGuild().getId().equals(Config.guild_id)) {
            event.getMessage().reply("AdminCraft loaded and appeal system ready.").queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild() && event.getGuild().getId().equals(Config.guild_id)) {
            return;
        }
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
        if (!event.isFromGuild() && event.getGuild().getId().equals(Config.guild_id)) {
            return;
        }
        Guild guild = event.getGuild();
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
        String[] splitted = event.getButton().getId().split("_");
        if (splitted.length == 3) {
            String memberID = splitted[0];
            String buttonID = "_" + splitted[1] + "_";
            String id = splitted[2];
            Member member = guild.getMemberById(memberID);
            if (member == null) {
                event.reply("Error: The member who submitted the appeal is no longer in the guild, or doesn't exists.").queue();
                return;
            }
            if (buttonID.equals(DiscordBot.REFUSE_APPEAL_BUTTON_ID)) {
                if (event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(Config.staff_role_id))) {
                    event.replyModal(Modal.create(memberID + DiscordBot.STAFF_REASON_MODAL_ID + id, "Closing appeal")
                                    .addActionRow(TextInput.create("reason", "Deny reason (optional)", TextInputStyle.PARAGRAPH)
                                            .setPlaceholder("Why is this appeal denied ?")
                                            .setRequired(false)
                                            .build())
                                    .build())
                            .queue();
                    return;
                }
            }
            if (buttonID.equals(DiscordBot.ACCEPT_TOTAL_APPEAL_BUTTON_ID)) {
                if (event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(Config.staff_role_id))) {
                    Map<UUID, SanctionData> dataset = SanctionDatabase.getSanctionData(id);
                    if (dataset == null) {
                        event.reply("Failure: no data found with associated ID " + id).setEphemeral(true).queue();
                        return;
                    }
                    Sanction type = dataset.get(dataset.keySet().stream().findFirst().get()).sanctionType;
                    if (SanctionDatabase.changeAppealStatus(id, AppealStatus.ACCEPTED)) {
                        switch (type) {
                            case MUTE:
                                //todo
                                break;
                            case BAN:
                                //todo
                                break;
                            default:
                                AdminCraft.LOGGER.warn("An appeal was made for a non-appealable sanction type (sanction {}). Skipping appeal success procedure...", id);
                                break;
                        }
                        event.getChannel().asTextChannel().upsertPermissionOverride(member).setDenied(EnumSet.of(Permission.VIEW_CHANNEL)).queue();
                        member.getUser().openPrivateChannel().queue(channel -> {
                            channel.sendMessage(
                                    "Hello,\n\n" +
                                            "Your appeal regarding sanction " + id + " on **" + guild.getName() + "** has been reviewed and approved by the staff team.\n\n" +
                                            "The following action has been taken:\n" +
                                            "- The sanction has been removed\n\n" +
                                            "Thank you for your understanding.\n\n" +
                                            "-# Powered by AdminCraft • Please do not reply to this automated message."
                            ).queue();
                        });
                        event.reply("Appeal successfully approved").queue();
                        AdminCraft.LOGGER.info("Appeal for sanction {} has been approved.", id);
                    } else {
                        event.reply("Failure: issue while updating the database.").setEphemeral(true).queue();
                        return;
                    }
                }
            }
            if (buttonID.equals(DiscordBot.ACCEPT_LIGHT_APPEAL_BUTTON_ID)) {
                if (event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(Config.staff_role_id))) {
                    event.replyModal(Modal.create(memberID + DiscordBot.STAFF_DURATION_MODAL_ID + id, "Approvation details")
                            .addActionRow(TextInput.create("duration", "New duration", TextInputStyle.SHORT).setRequired(true).build()).build()).queue();
                    return;
                }
            }
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.isFromGuild() && event.getGuild().getId().equals(Config.guild_id)) {
            return;
        }
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("An issue occurred.").setEphemeral(true).queue();
            return;
        }
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

            Role staff = guild.getRoleByBot(Config.staff_role_id);
            if (staff == null) {
                event.reply("An issue occurred with the configuration. Please inform the staff.").setEphemeral(true).queue();
                return;
            }

            TextChannel appealChannel = guild.createTextChannel("appeal-" + id, event.getChannel().asTextChannel().getParentCategory())
                    .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                    .addPermissionOverride(staff, EnumSet.of(
                            Permission.VIEW_CHANNEL,
                            Permission.MESSAGE_HISTORY,
                            Permission.MESSAGE_SEND), null)
                    .complete();
            appealChannel.sendMessage(staff.getAsMention()).queue();
            appealChannel.sendMessageEmbeds(embed.build()).setActionRow(
                    Button.secondary(member.getId() + DiscordBot.OPEN_DISCUSSION_APPEAL_BUTTON_ID + id, "Discuss with the player"),
                    Button.success(member.getId() + DiscordBot.ACCEPT_TOTAL_APPEAL_BUTTON_ID + id, "Remove sanction"),
                    Button.success(member.getId() + DiscordBot.ACCEPT_LIGHT_APPEAL_BUTTON_ID + id, "Reduce duration"),
                    Button.danger(member.getId() + DiscordBot.REFUSE_APPEAL_BUTTON_ID + id, "Refuse appeal")
            ).queue();
            SanctionDatabase.changeAppealStatus(id, AppealStatus.IN_PROGRESS);
            event.reply("Your appeal was submitted successfully.").setEphemeral(true).queue();
        }
        String[] splitted = event.getModalId().split("_");
        if (splitted.length == 3) {
            String memberID = splitted[0];
            String modalID = "_" + splitted[1] + "_";
            String id = splitted[2];

            Member member = guild.getMemberById(memberID);
            if (member == null) {
                event.reply("Error: The member who submitted the appeal is no longer in the guild, or doesn't exists.").queue();
                return;
            }

            if (modalID.equals(DiscordBot.STAFF_REASON_MODAL_ID)) {
                ModalMapping reasonMapping = event.getValue("reason");
                String reason;
                if (reasonMapping == null) {
                    reason = "No reason provided";
                } else {
                    reason = reasonMapping.getAsString();
                }
                if (SanctionDatabase.changeAppealStatus(id, AppealStatus.REFUSED)) {
                    member.getUser().openPrivateChannel().queue(channel -> {
                        channel.sendMessage(
                                "Hello,\n\n" +
                                        "Your appeal regarding sanction " + id + " on **" + guild.getName() + "** has been reviewed and denied by the staff team.\n\n" +
                                        "## Staff Response\n" +
                                        reason + "\n\n" +
                                        "If this sanction is temporary, its expiration date remains unchanged.\n\n" +
                                        "Thank you for your understanding.\n" +
                                        "-# Powered by AdminCraft • Please do not reply to this automated message."
                        ).queue();
                    });
                    event.getChannel().asTextChannel().upsertPermissionOverride(member).setDenied(EnumSet.of(Permission.VIEW_CHANNEL)).queue();
                    event.reply("Appeal successfully denied.").queue();
                    AdminCraft.LOGGER.info("Appeal for sanction {} has been denied.", id);
                } else {
                    event.reply("An issue occurred: failed to update appeal status.").setEphemeral(true).queue();
                }
            }
            if (modalID.equals(DiscordBot.STAFF_DURATION_MODAL_ID)) {
                Map<UUID, SanctionData> dataset = SanctionDatabase.getSanctionData(id);
                if (dataset == null) {
                    event.reply("Failure: no data found with associated ID " + id).setEphemeral(true).queue();
                    return;
                }
                String duration = event.getValue("duration").getAsString();
                Date dateFromNow = SanctionConfig.getDurationAsDateSince(duration, dataset.get(dataset.keySet().stream().findFirst()).date);
                if (dateFromNow == null) {
                    event.reply("Failure: Invalid duration format.").setEphemeral(true).queue();
                    return;
                }
                if (dateFromNow.before(new Date())) {
                    //todo: cancel sanction

                    if (SanctionDatabase.changeAppealStatus(id, AppealStatus.REDUCED)) {
                        member.getUser().openPrivateChannel().queue(channel -> {
                            channel.sendMessage(
                                    "Hello,\n\n" +
                                            "Your appeal regarding sanction " + id + " on **" + guild.getName() + "** has been reviewed and approved by the staff team.\n\n" +
                                            "The following action has been taken:\n" +
                                            "- The sanction duration has been changed\n\n" +
                                            "Thank you for your understanding.\n\n" +
                                            "-# Powered by AdminCraft • Please do not reply to this automated message."
                            ).queue();
                        });
                    } else {
                        event.reply("Failure: Failed to update database.").setEphemeral(true).queue();
                        return;
                    }
                } else {
                    //todo: change duration

                    if (SanctionDatabase.changeAppealStatus(id, AppealStatus.REDUCED)) {
                        member.getUser().openPrivateChannel().queue(channel -> {
                            channel.sendMessage(
                                    "Hello,\n\n" +
                                            "Your appeal regarding sanction " + id + " on **" + guild.getName() + "** has been reviewed and approved by the staff team.\n\n" +
                                            "The following action has been taken:\n" +
                                            "- The sanction duration has been changed\n" +
                                            "- The sanction has been removed because the expires date is reached.\n\n" +
                                            "Thank you for your understanding.\n\n" +
                                            "-# Powered by AdminCraft • Please do not reply to this automated message."
                            ).queue();
                        });
                    } else {
                        event.reply("Failure: Failed to update database.").setEphemeral(true).queue();
                        return;
                    }
                }
                event.getChannel().asTextChannel().upsertPermissionOverride(member).setDenied(EnumSet.of(Permission.VIEW_CHANNEL)).queue();
                event.reply("Successfully approved appeal and reduced the sanction.").queue();
                AdminCraft.LOGGER.info("Appeal for sanction {} successfully approved and sanction is reduced.", id);
                return;
            }
        }
    }
}
