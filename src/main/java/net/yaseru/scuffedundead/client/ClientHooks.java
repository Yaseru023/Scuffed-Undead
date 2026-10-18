package net.yaseru.scuffedundead.client;

import net.minecraft.client.Minecraft;

/** Kept separate so dedicated servers never load client-only classes. */
public class ClientHooks {
    public static void openClassScreen() {
        Minecraft.getInstance().setScreen(new ClassSelectScreen());
    }
}