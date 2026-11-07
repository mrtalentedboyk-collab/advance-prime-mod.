package com.advanceprime;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RolePayload(String role) implements CustomPayload {
    public static final CustomPayload.Id<RolePayload> ID =
            new CustomPayload.Id<>(Identifier.of("advanceprime", "role"));
    public static final PacketCodec<RegistryByteBuf, RolePayload> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, RolePayload::role, RolePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
