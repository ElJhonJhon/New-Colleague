package com.newcolleague.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = NewColleagueMod.MODID)
public class KoboldLanguageCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 1. Comando principal unificado: /kobold <subcomando>
        LiteralArgumentBuilder<CommandSourceStack> koboldCmd = Commands.literal("kobold")
                .requires(source -> true)
                .then(Commands.literal("reset_language")
                        .executes(ctx -> executeResetLanguage(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> executeResetLanguage(ctx, EntityArgument.getPlayer(ctx, "target")))))
                .then(Commands.literal("reset_tribe")
                        .executes(ctx -> executeResetTribe(ctx, 128.0D))
                        .then(Commands.argument("radio", DoubleArgumentType.doubleArg(8.0D, 512.0D))
                                .executes(ctx -> executeResetTribe(ctx, DoubleArgumentType.getDouble(ctx, "radio")))))
                .then(Commands.literal("reset_all")
                        .executes(ctx -> executeResetAll(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> executeResetAll(ctx, EntityArgument.getPlayer(ctx, "target")))));

        dispatcher.register(koboldCmd);

        // 2. Comando directo de reseteo de idioma: /resetkoboldlanguage [jugador]
        LiteralArgumentBuilder<CommandSourceStack> resetLangCmd = Commands.literal("resetkoboldlanguage")
                .requires(source -> true)
                .executes(ctx -> executeResetLanguage(ctx, ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> executeResetLanguage(ctx, EntityArgument.getPlayer(ctx, "target"))));

        dispatcher.register(resetLangCmd);

        // 3. Comando estructurado: /koboldlanguage reset [jugador]
        LiteralArgumentBuilder<CommandSourceStack> koboldLangCmd = Commands.literal("koboldlanguage")
                .requires(source -> true)
                .then(Commands.literal("reset")
                        .executes(ctx -> executeResetLanguage(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> executeResetLanguage(ctx, EntityArgument.getPlayer(ctx, "target")))));

        dispatcher.register(koboldLangCmd);

        // 4. Comando directo de reseteo de tribu: /resetkoboldtribe [radio] o /resettribe [radio]
        LiteralArgumentBuilder<CommandSourceStack> resetTribeCmd = Commands.literal("resetkoboldtribe")
                .requires(source -> true)
                .executes(ctx -> executeResetTribe(ctx, 128.0D))
                .then(Commands.argument("radio", DoubleArgumentType.doubleArg(8.0D, 512.0D))
                                .executes(ctx -> executeResetTribe(ctx, DoubleArgumentType.getDouble(ctx, "radio"))));

        dispatcher.register(resetTribeCmd);

        LiteralArgumentBuilder<CommandSourceStack> resetTribeShortCmd = Commands.literal("resettribe")
                .requires(source -> true)
                .executes(ctx -> executeResetTribe(ctx, 128.0D))
                .then(Commands.argument("radio", DoubleArgumentType.doubleArg(8.0D, 512.0D))
                        .executes(ctx -> executeResetTribe(ctx, DoubleArgumentType.getDouble(ctx, "radio"))));

        dispatcher.register(resetTribeShortCmd);
    }

    private static boolean checkCreativeOrOp(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if (entity instanceof Player player) {
            if (!player.isCreative()) {
                source.sendFailure(Component.literal("Este comando solo puede ser ejecutado por jugadores en Modo Creativo.")
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        }
        return true;
    }

    private static int executeResetLanguage(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        CommandSourceStack source = ctx.getSource();
        if (!checkCreativeOrOp(source)) return 0;

        Entity executorEntity = source.getEntity();
        boolean previouslyUnderstood = KoboldGirlEntity.understandsKoboldLanguage(target);

        // Revocar habilidad, etiquetas y registros
        KoboldGirlEntity.revokeKoboldLanguage(target);

        // Sonido mágico de desvanecimiento
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 0.7F);

        if (previouslyUnderstood) {
            source.sendSuccess(() -> Component.literal("✨ Se ha eliminado la comprensión del lenguaje Kobold de " + target.getName().getString() + ". Ahora podrá volver a vivir la experiencia del ritual con la Reina.")
                    .withStyle(ChatFormatting.GREEN), true);

            if (executorEntity != target) {
                target.sendSystemMessage(Component.literal("Has olvidado la lengua de las Kobold Girls por intervención mágica.")
                        .withStyle(ChatFormatting.GOLD));
            }
        } else {
            source.sendSuccess(() -> Component.literal(target.getName().getString() + " no poseía la habilidad de entender el lenguaje Kobold. Se han limpiado sus registros de todos modos.")
                    .withStyle(ChatFormatting.YELLOW), true);
        }

        return 1;
    }

    private static int executeResetTribe(CommandContext<CommandSourceStack> ctx, double radius) {
        CommandSourceStack source = ctx.getSource();
        if (!checkCreativeOrOp(source)) return 0;

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Este comando debe ser ejecutado por un jugador cerca de la tribu.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        int count = KoboldGirlEntity.resetTribeOwnership(player, radius);
        if (count > 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
            source.sendSuccess(() -> Component.literal("👑 Se ha restablecido la propiedad de la tribu (" + count + " kobold(s) reseteadas). La Reina ha recuperado su trono y salud al 100%, las súbditas rebeldes han vuelto a la calma y puedes volver a retarla a un duelo.")
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            source.sendFailure(Component.literal("No se encontraron Kobold Girls en un radio de " + (int) radius + " bloques para resetear.")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return count;
    }

    private static int executeResetAll(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        int r1 = executeResetLanguage(ctx, target);
        int r2 = executeResetTribe(ctx, 128.0D);
        return r1 + r2;
    }
}
