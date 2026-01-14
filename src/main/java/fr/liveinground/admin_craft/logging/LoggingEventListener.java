package fr.liveinground.admin_craft.logging;

public class LoggingEventListener {

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onExplode(ExplosionEvent event) {
        if (!event.isCancelled) {
            for (BlockPos pos : event.getExplosion().getToBlow()) {
                // todo: regiser log entry for #explosion
            }
        }
    }
}
