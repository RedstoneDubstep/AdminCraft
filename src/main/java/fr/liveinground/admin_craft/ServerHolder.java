package fr.liveinground.admin_craft;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ServerHolder {
    private static MinecraftServer server;

    public static MinecraftServer getServer() {
        if (server == null) {
            throw new IllegalStateException("Server not available yet");
        }
        return server;
    }

    @EventBusSubscriber(modid = AdminCraft.MODID)
    public static class Events {

        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            server = event.getServer();
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            server = null;
        }
    }

}
