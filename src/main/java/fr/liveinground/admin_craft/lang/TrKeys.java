package fr.liveinground.admin_craft.lang;

public class TrKeys {
    private TrKeys() {}

    // =========================
    // Spawn protection
    // =========================
    public static final String SPAWN_ENTER = "spawn.enter";
    public static final String SPAWN_LEAVE = "spawn.leave";

    // =========================
    // Time system
    // =========================
    public static final String TIME_REMAINING = "misc.time_remaining";
    public static final String TIME_REMAINING_SHORT = "misc.time_remaining.short";

    // =========================
    // Mute system
    // =========================
    public static final String MUTE_BEGINS_REASON = "mute.begins";

    public static final String MUTE_MESSAGE_CANCELLED = "mute.message_cancelled";
    public static final String MUTE_MESSAGE_CANCELLED_LOG = "mute.message_cancelled_log";

    public static final String MUTE_ENDS = "mute.ends";

    // =========================
    // Commands - mute/unmute
    // =========================
    public static final String COMMAND_MUTE_SUCCESS = "command.mute.success";
    public static final String COMMAND_MUTE_FAIL_ALREADY_MUTED = "command.mute.fail.already_muted";

    public static final String COMMAND_UNMUTE_SUCCESS = "command.unmute.success";
    public static final String COMMAND_UNMUTE_FAILED_NOT_MUTED = "command.unmute.failed.not_muted";

    // =========================
    // Disconnect messages
    // =========================
    public static final String DISCONNECT_BANNED_APPEAL_LINK = "disconnect.banned.appeal_link";
    public static final String DISCONNECT_BANNED_APPEAL_NOT_ALLOWED = "disconnect.banned.appeal_not_allowed";
    public static final String DISCONNECT_BANNED_DURATION_EXPIRES_IN = "disconnect.banned.duration.expires_in";
    public static final String DISCONNECT_BANNED_DURATION_PERMANENT = "disconnect.banned.duration.permanent";
    public static final String DISCONNECT_BANNED_ID = "disconnect.banned.id";
    public static final String DISCONNECT_BANNED_REASON = "disconnect.banned.reason";
    public static final String DISCONNECT_BANNED_TITLE = "disconnect.banned.title";

    // =========================
    // Warn system
    // =========================
    public static final String WARN_TITLE = "warn.title";
    public static final String WARN_MESSAGE = "warn.message";

    // =========================
    // Report system
    // =========================
    public static final String REPORT_SUCCESS = "command.report.success";
    public static final String REPORT_FAILED_WEBHOOK = "command.report.failed.webhook";
    public static final String REPORT_FAILED_SELF = "command.report.failed.self";

    // =========================
    // Freeze system
    // =========================
    public static final String FREEZE_BEGINS = "freeze.begins";
    public static final String FREEZE_ENDS = "freeze.ends";

    // =========================
    // Discord integration
    // =========================
    public static final String DISCORD_COMMAND_STATUS_APPEAL_ENABLED = "discord.command.status.appeal.enabled";
    public static final String DISCORD_COMMAND_STATUS_APPEAL_DISABLED = "discord.command.status.appeal.disabled";
    public static final String DISCORD_COMMAND_STATUS_SERVER_ONLINE = "discord.command.status.server.online";
    public static final String DISCORD_COMMAND_STATUS_FAILURE_ARGUMENT = "discord.command.status.failure.argument";
    public static final String DISCORD_STATUS = "discord.status";

    public static final String DISCORD_COMMAND_POST_EMBED_FAILURE_PERMISSION = "discord.command.post_embed.failure.permission";

    // =========================
    // Discord modals
    // =========================
    public static final String DISCORD_MODAL_INFO_TITLE = "discord.modal.info.title";
    public static final String DISCORD_MODAL_INFO_ID = "discord.modal.info.id";
    public static final String DISCORD_MODAL_INFO_IGN = "discord.modal.info.ign";
    public static final String DISCORD_MODAL_INFO_ID_PLACEHOLDER = "discord.modal.info.id.placeholder";
    public static final String DISCORD_MODAL_INFO_IGN_PLACEHOLDER = "discord.modal.info.ign.placeholder";

    public static final String DISCORD_MODAL_APPEAL_TITLE = "discord.modal.appeal.title";
    public static final String DISCORD_MODAL_APPEAL_REASON = "discord.modal.appeal.reason";
    public static final String DISCORD_MODAL_APPEAL_REASON_PLACEHOLDER = "discord.modal.appeal.reason.placeholder";

    public static final String DISCORD_MODAL_INFO_FAILURE_NOT_FOUND = "discord.modal.info.failure.not_found";

    // =========================
    // Discord staff system
    // =========================
    public static final String DISCORD_STAFF_BUTTON_ERROR_MEMBER_NOT_FOUND = "discord.staff.button.error.member_not_found";
    public static final String DISCORD_STAFF_BUTTON_ERROR_SANCTION_NOT_FOUND = "discord.staff.button.error.sanction_not_found";

    public static final String DISCORD_STAFF_MODAL_REFUSE_TITLE = "discord.staff.modal.refuse.title";
    public static final String DISCORD_STAFF_MODAL_REFUSE_REASON = "discord.staff.modal.refuse.reason";
    public static final String DISCORD_STAFF_MODAL_REFUSE_REASON_PLACEHOLDER = "discord.staff.modal.refuse.reason.placeholder";

    public static final String DISCORD_STAFF_MODAL_DURATION_TITLE = "discord.staff.modal.duration.title";
    public static final String DISCORD_STAFF_MODAL_DURATION_DURATION = "discord.staff.modal.duration.duration";

    public static final String DISCORD_STAFF_BUTTON_ACCEPT_SUCCESS = "discord.staff.button.accept.success";
    public static final String DISCORD_STAFF_BUTTON_ACCEPT_FAILURE = "discord.staff.button.accept.failure";

    public static final String DISCORD_STAFF_BUTTON_DELETE = "discord.staff.button.delete";

    // =========================
    // Discord embeds
    // =========================
    public static final String DISCORD_EMBED_INFO_TITLE = "discord.embed.info.title";
    public static final String DISCORD_EMBED_INFO_DESCRIPTION = "discord.embed.info.description";

    public static final String DISCORD_EMBED_INFO_IGN = "discord.embed.info.ign";
    public static final String DISCORD_EMBED_INFO_UUID = "discord.embed.info.uuid";
    public static final String DISCORD_EMBED_INFO_ID = "discord.embed.info.id";
    public static final String DISCORD_EMBED_INFO_TYPE = "discord.embed.info.type";
    public static final String DISCORD_EMBED_INFO_REASON = "discord.embed.info.reason";
    public static final String DISCORD_EMBED_INFO_DATE = "discord.embed.info.date";

    public static final String DISCORD_EMBED_INFO_EXPIRES = "discord.embed.info.expires";
    public static final String DISCORD_EMBED_INFO_EXPIRES_EXPIRED = "discord.embed.info.expires.expired";

    // =========================
    // Discord direct messages
    // =========================
    public static final String DISCORD_DM_APPEAL_ACCEPTED = "discord.dm.appeal.accepted";

    // =========================
    // Generic discord errors
    // =========================
    public static final String DISCORD_FAILURE = "discord.failure";
}
