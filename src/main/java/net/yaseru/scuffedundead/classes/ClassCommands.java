package net.yaseru.scuffedundead.classes;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yaseru.scuffedundead.ScuffedUndead;

import java.util.Arrays;

/**
 * Testing commands (op only):
 *   /scuffedclass get             - show your current class
 *   /scuffedclass set <CLASS>     - switch class (no kit)
 *   /scuffedclass kit             - give the kit of your current class
 *   /scuffedclass reset           - forget your class (selection screen appears on next login)
 */
@Mod.EventBusSubscriber(modid = ScuffedUndead.MOD_ID)
public class ClassCommands {

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("scuffedclass")
                .requires(src -> src.hasPermission(2))

                .then(Commands.literal("get").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    String name = ClassData.get(p).map(c -> c.name()).orElse("none");
                    ctx.getSource().sendSuccess(() -> Component.literal("Your class: " + name), false);
                    return 1;
                }))

                .then(Commands.literal("set")
                        .then(Commands.argument("class", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(PlayerClass.values()).map(Enum::name), builder))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    String input = StringArgumentType.getString(ctx, "class");
                                    try {
                                        PlayerClass c = PlayerClass.valueOf(input.toUpperCase());
                                        ClassData.set(p, c);
                                        ctx.getSource().sendSuccess(() -> Component.literal("Class set to " + c.name()), false);
                                        return 1;
                                    } catch (IllegalArgumentException e) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown class: " + input));
                                        return 0;
                                    }
                                })))

                .then(Commands.literal("kit").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    return ClassData.get(p).map(c -> {
                        c.giveKit(p);
                        return 1;
                    }).orElseGet(() -> {
                        ctx.getSource().sendFailure(Component.literal("You have no class yet"));
                        return 0;
                    });
                }))

                .then(Commands.literal("reset").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    ClassData.clear(p);
                    ctx.getSource().sendSuccess(() -> Component.literal("Class cleared. Relog to see the selection screen."), false);
                    return 1;
                })));
    }
}