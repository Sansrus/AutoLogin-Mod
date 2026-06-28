package ru.sansrus.autologin_mod.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ServerJoinHandler {
    private static final ScheduledExecutorService DELAY_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static final Random RANDOM = new Random();

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register(((clientPlayNetworkHandler, packetSender, client) -> {
            JSONConfigHandler.PlayerConfig cfg = JSONConfigHandler.getCurrentPlayerConfig();
            if (cfg == null) return;

            if (cfg.autosend) {
                int delayTicks = 40 + RANDOM.nextInt(41);
                DELAY_EXECUTOR.schedule(() -> {
                    client.execute(() -> {
                        if (client.player != null) {
                            Objects.requireNonNull(client.getConnection()).sendCommand("autologin");
                            cfg.currentCheck = false;
                            JSONConfigHandler.saveCurrentPlayerConfig(cfg);
                        }
                    });
                }, delayTicks * 50L, TimeUnit.MILLISECONDS);
            }
        }));
    }
}
