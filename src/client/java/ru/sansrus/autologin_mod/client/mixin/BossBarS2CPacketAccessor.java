package ru.sansrus.autologin_mod.client.mixin;

import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ClientboundBossEventPacket.class)
public interface BossBarS2CPacketAccessor {
    @Accessor("id")
    UUID getUuid();
}
