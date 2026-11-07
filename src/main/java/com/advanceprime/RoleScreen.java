package com.advanceprime;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class RoleScreen extends Screen {
    private static final String[] ROLES = {"villager", "guard", "minister", "farmer", "trader"};
    private static final String[] NAMES = {"Villager", "Guard", "Minister", "Farmer", "Trader"};
    private final ItemStack[] icons = {
            new ItemStack(Items.EMERALD), new ItemStack(Items.IRON_SWORD),
            new ItemStack(Items.GOLDEN_HELMET), new ItemStack(Items.WHEAT),
            new ItemStack(Items.EMERALD_BLOCK)
    };

    public RoleScreen() {
        super(Text.literal("Choose your role"));
    }

    @Override
    protected void init() {
        int startX = this.width / 2 - (ROLES.length * 70) / 2;
        int y = this.height / 2;
        for (int i = 0; i < ROLES.length; i++) {
            final String role = ROLES[i];
            addDrawableChild(ButtonWidget.builder(Text.literal(NAMES[i]), b -> {
                ClientPlayNetworking.send(new RolePayload(role));
                this.close();
            }).dimensions(startX + i * 70, y + 10, 64, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        int startX = this.width / 2 - (ROLES.length * 70) / 2;
        for (int i = 0; i < ROLES.length; i++) {
            context.drawItem(icons[i], startX + i * 70 + 24, this.height / 2 - 12);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
