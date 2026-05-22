package ru.sansrus.autologin_mod.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sansrus.autologin_mod.client.MessageProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {
private static final Logger LOGGER = LoggerFactory.getLogger("AutoLogin");

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void onGameMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        MessageProcessor.processMessage(packet.content(), LOGGER);
    }

    @Inject(method = "handleBossUpdate", at = @At("TAIL"))
    private void onBossBar(ClientboundBossEventPacket packet, CallbackInfo ci) {
        Component bossBarText = extractBossBarText(packet);
        if (bossBarText == null) {
            return;
        }
         MessageProcessor.processMessage(bossBarText, LOGGER);
    }


    @Unique
    private Component extractBossBarText(ClientboundBossEventPacket packet) {
        UUID uuid = ((BossBarS2CPacketAccessor) packet).getUuid();

        Minecraft client = Minecraft.getInstance();

        if (client.gui == null) return null;
        BossHealthOverlay bossOverlay = client.gui.getBossOverlay();

        Map<UUID, LerpingBossEvent> bossBars = ((BossBarHudAccessor) bossOverlay).getEvents();

        LerpingBossEvent bossBar = bossBars.get(uuid);

        if (bossBar == null) {
            return null;
        }

        return bossBar.getName();
    }
}
