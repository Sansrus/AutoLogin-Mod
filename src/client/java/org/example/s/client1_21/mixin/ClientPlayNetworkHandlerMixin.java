package org.example.s.client1_21.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;
import org.example.s.client1_21.MessageProcessor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
private static final Logger LOGGER = LogManager.getLogger("AutoLogin");

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        MessageProcessor.processMessage(packet.content(), LOGGER);
    }

    @Inject(method = "onBossBar", at = @At("TAIL"))
    private void onBossBar(BossBarS2CPacket packet, CallbackInfo ci) {
        Text bossBarText = extractBossBarText(packet);
        if (bossBarText == null) {
            return;
        }
         MessageProcessor.processMessage(bossBarText, LOGGER);
    }

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


    @Unique
    private Text extractBossBarText(BossBarS2CPacket packet) {
        UUID uuid = ((BossBarS2CPacketAccessor) packet).getUuid();

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.inGameHud == null) return null;
        BossBarHud bossBarHud = client.inGameHud.getBossBarHud();

        Map<UUID, ClientBossBar> bossBars = ((BossBarHudAccessor) bossBarHud).getBossBars();

        ClientBossBar bossBar = bossBars.get(uuid);

        if (bossBar == null) {
            return null;
        }

        return bossBar.getName();
    }
}

