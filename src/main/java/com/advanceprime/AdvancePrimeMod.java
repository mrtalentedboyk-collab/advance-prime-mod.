package com.advanceprime;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AdvancePrimeMod implements ModInitializer {

    private static final String[] START = {"Silver", "Golden", "Oak", "Stone", "River",
            "Sun", "Moon", "Iron", "Green", "Crystal", "Eagle", "Wolf", "Maple", "Blue", "Amber", "Royal"};
    private static final String[] END = {"haven", "ford", "ridge", "vale", "crest",
            "field", "port", "gate", "brook", "wood", "hill", "bay", "stead", "peak", "shire", "mere"};

    private final Map<UUID, Long> lastVillage = new HashMap<>();

    public static String villageName(BlockPos pos) {
        Random r = new Random(pos.asLong());
        return START[r.nextInt(START.length)] + END[r.nextInt(END.length)];
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (player.getCommandTags().contains("apm_welcomed")) return;
            player.addCommandTag("apm_welcomed");

            player.sendMessage(Text.literal("Welcome to Advance Prime Mod!")
                    .formatted(Formatting.GREEN, Formatting.BOLD), false);

            ServerWorld world = player.getEntityWorld();
            BlockPos found = world.locateStructure(StructureTags.VILLAGE, player.getBlockPos(), 100, false);
            if (found != null) {
                player.sendMessage(Text.literal("Nearest village: " + villageName(found)
                        + " at X: " + found.getX() + " Z: " + found.getZ())
                        .formatted(Formatting.GREEN), false);
            } else {
                player.sendMessage(Text.literal("No village found nearby.")
                        .formatted(Formatting.YELLOW), false);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 40 != 0) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = player.getEntityWorld();
                StructureStart start = world.getStructureAccessor()
                        .getStructureContaining(player.getBlockPos(), StructureTags.VILLAGE);
                if (start == null || !start.hasChildren()) continue;

                BlockPos center = start.getBoundingBox().getCenter();
                long key = center.asLong();
                Long last = lastVillage.get(player.getUuid());
                if (last != null && last == key) continue;
                lastVillage.put(player.getUuid(), key);

                String name = villageName(center);
                player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 60, 20));
                player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal("Welcome to " + name).formatted(Formatting.GOLD)));
                player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal("A new village discovered").formatted(Formatting.GREEN)));
                player.sendMessage(Text.literal("Welcome to " + name + "!")
                        .formatted(Formatting.GOLD), false);
            }
        });
    }
        }
