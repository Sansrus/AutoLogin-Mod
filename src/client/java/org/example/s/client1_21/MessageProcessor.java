package org.example.s.client1_21;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Обработка входящего текста и отправка команды логина
public class MessageProcessor {
    private static JSONConfigHandler.PlayerConfig config;
    private static final Pattern WS_PATTERN = Pattern.compile("[^\\S\\r\\n]+");
    private static final Pattern TRAIL_COMMAS = Pattern.compile("[,]+$");
    public static List<String> triggers;

    static {
        reloadConfig();  // и там же заполнить triggers из config.triggers
    }

    public static void reloadConfig() {
        config = JSONConfigHandler.getCurrentPlayerConfig();
        triggers = Arrays.stream(config.triggers.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static void processMessage(Text message, Logger logger) {
        String text = TRAIL_COMMAS.matcher(
                WS_PATTERN.matcher(message.getString())
                        .replaceAll("")
                        .replace("/", "")
        ).replaceAll("");
        reloadConfig();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || config == null) return;

        if (!config.currentCheck) return;

        String serverAddress = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : null;
        if (serverAddress == null) return;

        String password = config.passwords.get(serverAddress);
        if (password == null) return;

        boolean isAutoSend = config.autosend;
        if (isAutoSend) return;

        for (String trigger : triggers) {
            if (text.contains(trigger.trim())) {
                Objects.requireNonNull(client.getNetworkHandler()).sendChatCommand(trigger + " " + password);
                reloadConfig();
                config.currentCheck = false;
                JSONConfigHandler.saveCurrentPlayerConfig(config);
                return;
            }
        }
    }
}

