package org.example.s.client1_21;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;


public class ServerLeaveHandler {
    public static void init() {
        // Сбрасываем флаг отправки команды после отключения игрока
        ClientPlayConnectionEvents.DISCONNECT.register((phase, client) -> {
                JSONConfigHandler.PlayerConfig config = JSONConfigHandler.getCurrentPlayerConfig();
                if (!config.currentCheck) {
                    config.currentCheck = true;
                    JSONConfigHandler.saveCurrentPlayerConfig(config);
                }
        });
    }
}