package com.newcolleague.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.entity.KoboldTribeVariant;
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
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

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
                .then(Commands.literal("reset")
                        .executes(ctx -> executeResetAll(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("radio", DoubleArgumentType.doubleArg(8.0D, 512.0D))
                                .executes(ctx -> executeResetTribe(ctx, DoubleArgumentType.getDouble(ctx, "radio"))))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> executeResetAll(ctx, EntityArgument.getPlayer(ctx, "target")))))
                .then(Commands.literal("reset_all")
                        .executes(ctx -> executeResetAll(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> executeResetAll(ctx, EntityArgument.getPlayer(ctx, "target")))))
                .then(Commands.literal("variant")
                        .then(Commands.argument("id", IntegerArgumentType.integer(0, KoboldTribeVariant.getTotalVariants() - 1))
                                .executes(ctx -> executeSetVariant(ctx, IntegerArgumentType.getInteger(ctx, "id")))))
                .then(Commands.literal("tribe_variant")
                        .then(Commands.argument("id", IntegerArgumentType.integer(0, KoboldTribeVariant.getTotalVariants() - 1))
                                .executes(ctx -> executeSetTribeVariant(ctx, IntegerArgumentType.getInteger(ctx, "id")))))
                .then(Commands.literal("survivor")
                        .executes(ctx -> executeSetSurvivor(ctx, true))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(ctx -> executeSetSurvivor(ctx, BoolArgumentType.getBool(ctx, "enabled")))));

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

    private static int executeSetVariant(CommandContext<CommandSourceStack> ctx, int variantId) {
        CommandSourceStack source = ctx.getSource();
        if (!checkCreativeOrOp(source)) return 0;

        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        AABB box = player.getBoundingBox().inflate(12.0D);
        List<KoboldGirlEntity> nearby = player.level().getEntitiesOfClass(KoboldGirlEntity.class, box, Entity::isAlive);

        KoboldGirlEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (KoboldGirlEntity k : nearby) {
            double d = k.distanceToSqr(player);
            if (d < closestDist) {
                closestDist = d;
                closest = k;
            }
        }

        if (closest != null) {
            closest.setTribeVariant(variantId);
            KoboldTribeVariant varEnum = KoboldTribeVariant.byId(variantId);
            KoboldGirlEntity targetKobold = closest;
            source.sendSuccess(() -> Component.literal("🎨 Variante cambiada a ID " + variantId + " (" + varEnum.getDisplayName() + ") para: " + targetKobold.getDisplayNameWithTribe())
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("No hay ninguna Kobold Girl cerca (radio 12 bloques).")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int executeSetTribeVariant(CommandContext<CommandSourceStack> ctx, int variantId) {
        CommandSourceStack source = ctx.getSource();
        if (!checkCreativeOrOp(source)) return 0;

        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        AABB box = player.getBoundingBox().inflate(48.0D);
        List<KoboldGirlEntity> nearby = player.level().getEntitiesOfClass(KoboldGirlEntity.class, box, Entity::isAlive);

        KoboldGirlEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (KoboldGirlEntity k : nearby) {
            double d = k.distanceToSqr(player);
            if (d < closestDist) {
                closestDist = d;
                closest = k;
            }
        }

        if (closest == null) {
            source.sendFailure(Component.literal("No se encontraron Kobold Girls cerca en un radio de 48 bloques.")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        String targetTribe = closest.getTribeName();
        int count = 0;
        for (KoboldGirlEntity k : nearby) {
            if (targetTribe.isEmpty() || k.getTribeName().equalsIgnoreCase(targetTribe)) {
                k.setTribeVariant(variantId);
                count++;
            }
        }

        KoboldTribeVariant varEnum = KoboldTribeVariant.byId(variantId);
        final int affected = count;
        final String tribeNameDisplay = targetTribe.isEmpty() ? "Sin Tribu" : targetTribe;
        source.sendSuccess(() -> Component.literal("🎨 Se cambió la variante a ID " + variantId + " (" + varEnum.getDisplayName() + ") para " + affected + " miembro(s) de la tribu [" + tribeNameDisplay + "].")
                .withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int executeSetSurvivor(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        CommandSourceStack source = ctx.getSource();
        if (!checkCreativeOrOp(source)) return 0;

        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        AABB box = player.getBoundingBox().inflate(12.0D);
        List<KoboldGirlEntity> nearby = player.level().getEntitiesOfClass(KoboldGirlEntity.class, box, Entity::isAlive);

        KoboldGirlEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (KoboldGirlEntity k : nearby) {
            double d = k.distanceToSqr(player);
            if (d < closestDist) {
                closestDist = d;
                closest = k;
            }
        }

        if (closest != null) {
            closest.setSurvivorQueen(enabled);
            if (enabled) {
                closest.setQueen(false);
            }
            KoboldGirlEntity targetKobold = closest;
            source.sendSuccess(() -> Component.literal("⚔️ Estado de Reina Sobreviviente establecido en [" + enabled + "] para: " + targetKobold.getDisplayNameWithTribe())
                    .withStyle(ChatFormatting.GOLD), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("No hay ninguna Kobold Girl cerca (radio 12 bloques).")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }
}
