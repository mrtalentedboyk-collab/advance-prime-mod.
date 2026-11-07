package com.advanceprime;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class RoleNetworking {
    public static final List<String> ROLES = List.of("villager", "guard", "minister", "farmer", "trader");

    public static void init() {
        PayloadTypeRegistry.playC2S().register(RolePayload.ID, RolePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RolePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> apply(player, payload.role()));
        });
    }

    private static void apply(ServerPlayerEntity player, String role) {
        if (!ROLES.contains(role)) return;
        boolean already = player.getCommandTags().contains("apm_role_" + role);
        for (String r : ROLES) player.removeCommandTag("apm_role_" + r);
        player.addCommandTag("apm_role_" + role);

        if (!already) {
            switch (role) {
                case "guard" -> {
                    player.giveItemStack(new ItemStack(Items.IRON_SWORD));
                    player.giveItemStack(new ItemStack(Items.IRON_CHESTPLATE));
                }
                case "minister" -> player.giveItemStack(new ItemStack(Items.GOLD_INGOT, 5));
                case "farmer" -> {
                    player.giveItemStack(new ItemStack(Items.IRON_HOE));
                    player.giveItemStack(new ItemStack(Items.BREAD, 5));
                }
                case "trader" -> player.giveItemStack(new ItemStack(Items.EMERALD, 5));
                default -> { }
            }
        }
        player.sendMessage(Text.literal("You are now a " + role + "!").formatted(Formatting.GREEN), false);
    }
}
