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
}
