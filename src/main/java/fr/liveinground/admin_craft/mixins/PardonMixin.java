package fr.liveinground.admin_craft.mixins;

import fr.liveinground.admin_craft.AdminCraft;
import fr.liveinground.admin_craft.storage.SanctionDatabase;
import fr.liveinground.admin_craft.storage.types.sanction.DatabaseSanctionData;
import fr.liveinground.admin_craft.storage.types.sanction.Sanction;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Date;
import java.util.Optional;

@Mixin(UserBanList.class)
public class PardonMixin {

    @Inject(method = "remove(Lnet/minecraft/server/players/NameAndId;)Z", at = @At("HEAD"))
    private void onPardon(NameAndId p_443373_, CallbackInfoReturnable<Boolean> cir) {
        UserBanList self = (UserBanList)(Object)this;
        UserBanListEntry entry = self.get(p_443373_);
        Date now = new Date();
        Optional<DatabaseSanctionData> banData;

        if (entry != null) {
            String reason = entry.getReason();
            Date created = entry.getCreated();
            banData = SanctionDatabase.getCurrentSanctions(p_443373_.id().toString()).stream().filter(
                    data -> data.type().equals(Sanction.BAN)
                            && !data.removed()
                            && (data.expiresOn() == null || data.expiresOn().after(now)
                            && data.reason().equals(reason)
                            && data.date().equals(created)
                    )
            ).findFirst();
        } else {
            banData = SanctionDatabase.getCurrentSanctions(p_443373_.id().toString()).stream().filter(
                    data -> data.type().equals(Sanction.BAN)
                            && !data.removed()
                            && (data.expiresOn() == null || data.expiresOn().after(now)
                    )
            ).findFirst();
        }
        if (banData.isEmpty()) {
            AdminCraft.LOGGER.warn("Couldn't find any suitable sanction in the database corresponding to {}'s ban, skipping database change while unbanning.", p_443373_.name());
        } else {
            SanctionDatabase.removeSanction(banData.get().id());
        }
    }
}