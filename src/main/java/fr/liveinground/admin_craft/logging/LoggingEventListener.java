package fr.liveinground.admin_craft.logging;

import fr.liveinground.admin_craft.Config;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LoggingEventListener {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        if (Config.enable_logging) {
            LoggingDatabase.start();
        }
    }

    // ---------------
    // -- Placement --
    // ---------------

    @SubscribeEvent
    public static void blockPlacedByEntityEvent(BlockEvent.EntityPlaceEvent event) {

    }

    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent event) {

    }

    @SubscribeEvent
    public static void onTreeGrowEvent(SaplingGrowTreeEvent event) {

    }

    // --------------
    // -- Breaking --
    // --------------

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {

    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {

    }

    @SubscribeEvent
    public static void onGrief(EntityMobGriefingEvent event) {

    }

    // -----------------
    // -- Interacting --
    // -----------------

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

    }

    @SubscribeEvent
    public static void onToolUse(BlockEvent.BlockToolModificationEvent event) {

    }

    // -------------
    // -- Killing --
    // -------------

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {

    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {

    }
}
