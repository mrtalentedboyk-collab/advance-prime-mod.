package com.advanceprime;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
        // /village command: shows nearest village
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("village").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) return 0;
                    ServerWorld world = player.getEntityWorld();
                    BlockPos found = world.locateStructure(StructureTags.VILLAGE, player.getBlockPos(), 100, false);
                    if (found == null) {
                        player.sendMessage(Text.literal("No village found nearby.").formatted(Formatting.YELLOW), false);
                    } else {
                        int dx = found.getX() - player.getBlockX();
                        int dz = found.getZ() - player.getBlockZ();
                        int dist = (int) Math.sqrt((double) dx * dx + (double) dz * dz);
                        player.sendMessage(Text.literal("Nearest village: " + villageName(found)
                                + " | X: " + found.getX() + " Z: " + found.getZ()
                                + " | " + dist + " blocks away").formatted(Formatting.GREEN), false);
                    }
                    return Command.SINGLE_SUCCESS;
                })));

        // Welcome message (first join only)
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

        // Arrival detection: every 3 seconds
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 60 != 0) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = player.getEntityWorld();
                BlockPos found = world.locateStructure(StructureTags.VILLAGE, player.getBlockPos(), 6, false);
                if (found == null) {
                    lastVillage.remove(player.getUuid());
                    continue;
                }
                double dx = found.getX() - player.getX();
                double dz = found.getZ() - player.getZ();
                if (dx * dx + dz * dz > 70.0 * 70.0) {
                    lastVillage.remove(player.getUuid());
                    continue;
                }

                long key = found.asLong();
                Long last = lastVillage.get(player.getUuid());
                if (last != null && last == key) continue;
                lastVillage.put(player.getUuid(), key);

                String name = villageName(found);
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
