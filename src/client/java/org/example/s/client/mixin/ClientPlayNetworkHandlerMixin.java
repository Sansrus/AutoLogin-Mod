package org.example.s.client.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;
import org.example.s.client.MessageProcessor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
private static final Logger LOGGER = LogManager.getLogger("AutoLogin");

    //Чтение чата
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        MessageProcessor.processMessage(packet.content(), LOGGER);
    }

    //Чтение босс-бара
    @Inject(method = "onBossBar", at = @At("TAIL"))
    private void onBossBar(BossBarS2CPacket packet, CallbackInfo ci) {
        Text bossBarText = extractBossBarText(packet);
        if (bossBarText == null) {
            return;
        }
        MessageProcessor.processMessage(bossBarText, LOGGER);

    }

    //Чтение Заголовка
    @Inject(method = "onTitle", at = @At("TAIL"))
    private void onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        try {
            for (Field field : packet.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(packet);
                if (value instanceof Text) {
                    MessageProcessor.processMessage((Text) value, LOGGER);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Метод для извлечения текста босс-бара с fallback-парсингом
    private Text extractBossBarText(Object obj) {
        if (obj == null) return null;
        try {
            // Получаем nameObject из field_12075
            Field field = obj.getClass().getDeclaredField("field_12075");
            field.setAccessible(true);
            Object nameObject = field.get(obj);
            if (nameObject == null) {
                LOGGER.warn("(AutoLogin) nameObject turned out to be null!");
                return null;
            }

            // Попытка получить текстовый компонент через comp_2260
            try {
                Field compField = nameObject.getClass().getDeclaredField("comp_2260");
                compField.setAccessible(true);
                Object compObj = compField.get(nameObject);
                if (compObj instanceof Text) {
                    Text textComponent = (Text) compObj;

                    // Собираем итоговый текст из основного компонента и его siblings
                    StringBuilder bossBarText = new StringBuilder();
                    bossBarText.append(textComponent.getString());
                    for (Text sibling : textComponent.getSiblings()) {
                        bossBarText.append(sibling.getString());
                    }
                    String finalText = bossBarText.toString();
                    return Text.literal(finalText);
                } else {
                    LOGGER.error("(AutoLogin) The comp_2260 field is not a text component: " + compObj);
                }
            } catch (NoSuchFieldException e) {
                LOGGER.error("(AutoLogin) The comp_2260 field was not found: " + e.getMessage());
            }

            // Если не удалось получить через comp_2260, используем fallback: рекурсивный поиск Text
            Text fallback = findTextRecursively(nameObject);
            if (fallback != null) {
                String finalText = fallback.getString();
                return Text.literal(finalText);
            } else {
                LOGGER.error("(AutoLogin) It was not possible to extract the text of the boss bar either in the main way or through the fallback.");
            }
        } catch (Exception e) {
            LOGGER.error("(AutoLogin) Error when extracting the boss bar text: ");
        }
        return null;
    }

    private Text findTextRecursively(Object obj, int depth, int maxDepth, Set<Object> visited) {
        if (obj == null || depth > maxDepth || visited.contains(obj)) {
            return null;
        }
        visited.add(obj);
        if (obj instanceof Text) {
            String s = ((Text) obj).getString();
            if (!s.isEmpty()) {
                return (Text) obj;
            }
        }
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                Text found = findTextRecursively(value, depth + 1, maxDepth, visited);
                if (found != null) {
                    return found;
                }
            } catch (Exception e) {
                // Игнорируем ошибки доступа
            }
        }
        return null;
    }

    // Упрощённый метод-обёртка:
    private Text findTextRecursively(Object obj) {
        return findTextRecursively(obj, 0, 10, new HashSet<>());
    }

    // Метод для рефлексии, чтобы доставать нужные поля, если их нет в API
    private static Object extractField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Error receiving the {}field: {}", fieldName, e.getMessage());
            return null;
        }
    }
}

