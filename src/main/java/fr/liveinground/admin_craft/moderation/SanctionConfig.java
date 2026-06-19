package fr.liveinground.admin_craft.moderation;

import fr.liveinground.admin_craft.lang.LangManager;
import fr.liveinground.admin_craft.lang.TrKeys;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SanctionConfig {

    public static boolean checkDuration(String input) {
        return (getDuration(input) != null);
    }

    public static @Nullable List<Integer> getDuration(@Nullable String input) {
        if (input == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(
                "^(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?$"
        );
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches() && !input.isEmpty()) {
            boolean hasAtLeastOne = false;
            List<Integer> output = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                String group = matcher.group(i);
                if (group != null) {
                    hasAtLeastOne = true;
                    output.add(Integer.parseInt(group));
                } else {
                    output.add(0);
                }
            }
            return hasAtLeastOne ? output : null;
        } else {
            return null;
        }
    }

    public static @Nullable Date getDurationAsDate(String input) {
        List<Integer> duration = SanctionConfig.getDuration(input);
        if (duration == null) return null;
        if (!(duration.size() == 4)) return null;
        Integer days = duration.get(0);
        Integer hours = duration.get(1);
        Integer minutes = duration.get(2);
        Integer seconds = duration.get(3);
        LocalDateTime expiresLocal = LocalDateTime.now()
                .plusHours(days * 24)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);
        return Date.from(expiresLocal.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static @Nullable Date getDurationAsDateSince(String input, Date date) {
        List<Integer> duration = SanctionConfig.getDuration(input);
        if (duration == null) return null;
        if (!(duration.size() == 4)) return null;
        Integer days = duration.get(0);
        Integer hours = duration.get(1);
        Integer minutes = duration.get(2);
        Integer seconds = duration.get(3);
        LocalDateTime expiresLocal = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .plusHours(days * 24)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);
        return Date.from(expiresLocal.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String getDurationAsStringFromDate(Date input) {
        if (input != null) {
            long diff = input.getTime() - System.currentTimeMillis();
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            diff -= TimeUnit.DAYS.toMillis(days);

            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            diff -= TimeUnit.HOURS.toMillis(hours);

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

            String daysStr = String.valueOf(days);
            String hoursStr = String.valueOf(hours);
            String minutesStr = String.valueOf(minutes);
            return LangManager.tr(TrKeys.TIME_REMAINING_SHORT, Map.of(
                    "days", daysStr,
                    "hours", hoursStr,
                    "minutes", minutesStr));
        } else {
            return "N/A";
        }
    }
}
