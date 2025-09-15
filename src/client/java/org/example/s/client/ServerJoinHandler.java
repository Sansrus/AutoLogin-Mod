package org.example.s.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.Objects;


public class ServerJoinHandler {
    public static void init() {
        // Отправляем команду сразу после подключения к серверу, если можно
        ClientPlayConnectionEvents.JOIN.register(((clientPlayNetworkHandler, packetSender, client) -> {
            // Загружаем конфиг для текущего игрока
            JSONConfigHandler.PlayerConfig cfg = JSONConfigHandler.getCurrentPlayerConfig();
            if (cfg == null) return;

            if (cfg.autosend) {
                // Выполняем действие на клиентском (игровом) потоке
                client.execute(() -> {
                    if (client.player != null) {
                        // Отправляем команду
                        Objects.requireNonNull(client.getNetworkHandler()).sendChatCommand("autologin");
                        cfg.currentCheck = false;                       // Меняем флаг и сохраняем в конфиг
                        JSONConfigHandler.saveCurrentPlayerConfig(cfg);
                    }
                });
            }
        }));
    }
}
