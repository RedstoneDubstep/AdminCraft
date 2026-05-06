package fr.liveinground.admin_craft.storage.types.sanction;

import javax.annotation.Nullable;
import java.util.Date;

public record DatabaseSanctionData(
        String id,
        String uuid,
        String ign,
        Sanction type,
        String reason,
        Date date,
        @Nullable Date expiresOn,
        AppealStatus status,
        @Nullable Date appealDelay
) {

    public boolean appealable() {
        return status == AppealStatus.NOT_REQUESTED;
    }
}