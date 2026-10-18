package net.yaseru.scuffedundead.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.yaseru.scuffedundead.classes.PlayerClass;
import net.yaseru.scuffedundead.network.SelectClassPacket;
import net.yaseru.scuffedundead.registry.ModNetwork;

public class ClassSelectScreen extends Screen {
    private PlayerClass selected;
    private Button confirm;

    public ClassSelectScreen() {
        super(Component.literal("Choose Your Doom"));
    }

    @Override
    protected void init() {
        PlayerClass[] all = PlayerClass.values();
        int w = 150, h = 20, gap = 4;
        int startY = 50;

        for (int i = 0; i < all.length; i++) {
            PlayerClass c = all[i];
            int x = width / 2 - w - 2 + (i % 2) * (w + 4);
            int y = startY + (i / 2) * (h + gap);
            addRenderableWidget(Button.builder(Component.literal(c.displayName), b -> {
                selected = c;
                confirm.active = true;
            }).bounds(x, y, w, h).build());
        }

        confirm = addRenderableWidget(Button.builder(Component.literal("Confirm (no refunds)"), b -> {
            if (selected == null) return;
            ModNetwork.CHANNEL.sendToServer(new SelectClassPacket(selected.ordinal()));
            onClose();
        }).bounds(width / 2 - 100, height - 40, 200, 20).build());
        confirm.active = false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawCenteredString(font, "§c§lCHOOSE YOUR DOOM", width / 2, 20, 0xFFFFFF);
        g.drawCenteredString(font, "§7The apocalypse is hiring.", width / 2, 34, 0xFFFFFF);

        if (selected != null) {
            int rows = (PlayerClass.values().length + 1) / 2;
            int y = 50 + rows * 24 + 14;
            g.drawCenteredString(font, "§e" + selected.displayName, width / 2, y, 0xFFFFFF);
            g.drawWordWrap(font, Component.literal(selected.description), width / 2 - 150, y + 14, 300, 0xDDDDDD);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // no escaping your destiny
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}