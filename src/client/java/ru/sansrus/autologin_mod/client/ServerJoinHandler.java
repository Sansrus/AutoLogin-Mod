package ru.sansrus.autologin_mod.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.Objects;


public class ServerJoinHandler {
    public static void init() {
        ClientPlayConnectionEvents.JOIN.register(((clientPlayNetworkHandler, packetSender, client) -> {
            JSONConfigHandler.PlayerConfig cfg = JSONConfigHandler.getCurrentPlayerConfig();
            if (cfg == null) return;

            if (cfg.autosend) {
                client.execute(() -> {
                    if (client.player != null) {
                        Objects.requireNonNull(client.getConnection()).sendCommand("autologin");
                        cfg.currentCheck = false;
                        JSONConfigHandler.saveCurrentPlayerConfig(cfg);
                    }
                });
            }
        }));
    }
}
