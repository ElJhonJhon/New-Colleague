package com.newcolleague.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.ModItems;
import com.newcolleague.mod.item.BaculoItem;
import com.newcolleague.mod.item.BaculoTier;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = NewColleagueMod.MODID)
public class BaculoCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_TIERS = (context, builder) -> {
        for (BaculoTier tier : BaculoTier.values()) {
            builder.suggest(String.valueOf(tier.getLevel()));
            builder.suggest(tier.getId());
        }
        builder.suggest("diamante");
        builder.suggest("netherita");
        builder.suggest("cobre");
        builder.suggest("hierro");
        builder.suggest("oro");
        builder.suggest("pizarra");
        return builder.buildFuture();
    };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> baculoCmd = Commands.literal("baculo")
                .requires(source -> true)
                // /baculo level <tier> [target]
                .then(Commands.literal("level")
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests(SUGGEST_TIERS)
                                .executes(ctx -> executeSetLevel(ctx, StringArgumentType.getString(ctx, "tier"), ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> executeSetLevel(ctx, StringArgumentType.getString(ctx, "tier"), EntityArgument.getPlayer(ctx, "target"))))))
                // /baculo setlevel <tier> [target] (alias)
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests(SUGGEST_TIERS)
                                .executes(ctx -> executeSetLevel(ctx, StringArgumentType.getString(ctx, "tier"), ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> executeSetLevel(ctx, StringArgumentType.getString(ctx, "tier"), EntityArgument.getPlayer(ctx, "target"))))))
                // /baculo give <tier> [target]
                .then(Commands.literal("give")
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests(SUGGEST_TIERS)
                                .executes(ctx -> executeGive(ctx, StringArgumentType.getString(ctx, "tier"), ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> executeGive(ctx, StringArgumentType.getString(ctx, "tier"), EntityArgument.getPlayer(ctx, "target"))))))
                // /baculo get [target]
                .then(Commands.literal("get")
                        .executes(ctx -> executeGet(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> executeGet(ctx, EntityArgument.getPlayer(ctx, "target")))));

        dispatcher.register(baculoCmd);
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

    private static int executeSetLevel(CommandContext<CommandSourceStack> ctx, String tierQuery, ServerPlayer target) {
        CommandSourceStack source = ctx.getSource();
        if (!checkCreativeOrOp(source)) return 0;

        BaculoTier tier = BaculoTier.byNameOrId(tierQuery);
        if (tier == null) {
            source.sendFailure(Component.literal("Nivel o material no válido: '" + tierQuery + "'. Usa 1-7 o cobblestone, deepslate_tiles, copper, iron, gold, diamond, netherite.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        int targetLevel = tier.getLevel();

        // 1. Guardar progreso desbloqueado en el jugador
        target.getPersistentData().putInt("UnlockedBaculoLevel", targetLevel);

        // 2. Modificar báculo sostenido o inventario
        boolean modifiedAny = false;
        ItemStack main = target.getMainHandItem();
        if (main.is(ModItems.BACULO.get())) {
            BaculoItem.setStaffLevel(main, targetLevel);
            modifiedAny = true;
        }
        ItemStack off = target.getOffhandItem();
        if (off.is(ModItems.BACULO.get())) {
            BaculoItem.setStaffLevel(off, targetLevel);
            modifiedAny = true;
        }

        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            ItemStack stack = target.getInventory().getItem(i);
            if (stack.is(ModItems.BACULO.get())) {
                BaculoItem.setStaffLevel(stack, targetLevel);
                modifiedAny = true;
            }
        }

        // Si el jugador no tenía ningún báculo, otorgarle uno directamente con ese nivel
        if (!modifiedAny) {
            ItemStack newStaff = new ItemStack(ModItems.BACULO.get());
            BaculoItem.setStaffLevel(newStaff, targetLevel);
            if (!target.getInventory().add(newStaff)) {
                target.drop(newStaff, false);
            }
        }

        Component msg = Component.literal("✔ Nivel del Báculo establecido a ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal("Nivel " + targetLevel + " (" + tier.getDisplayName() + ")")
                        .withStyle(tier.getFormatting(), ChatFormatting.BOLD))
                .append(Component.literal(" para " + target.getName().getString() + ".").withStyle(ChatFormatting.GREEN));

        source.sendSuccess(() -> msg, true);
        if (source.getEntity() != target) {
            target.sendSystemMessage(msg);
        }
        return 1;
    }

    private static int executeGive(CommandContext<CommandSourceStack> ctx, String tierQuery, ServerPlayer target) {
        CommandSourceStack source = ctx.getSource();
        if (!checkCreativeOrOp(source)) return 0;

        BaculoTier tier = BaculoTier.byNameOrId(tierQuery);
        if (tier == null) {
            source.sendFailure(Component.literal("Nivel o material no válido: '" + tierQuery + "'. Usa 1-7 o nombres de tier.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        ItemStack staff = new ItemStack(ModItems.BACULO.get());
        BaculoItem.setStaffLevel(staff, tier.getLevel());

        if (!target.getInventory().add(staff)) {
            target.drop(staff, false);
        }

        Component msg = Component.literal("✔ Entregado Báculo de Mando ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal("[" + tier.getDisplayName() + " - Nivel " + tier.getLevel() + "]")
                        .withStyle(tier.getFormatting(), ChatFormatting.BOLD))
                .append(Component.literal(" a " + target.getName().getString() + ".").withStyle(ChatFormatting.GREEN));

        source.sendSuccess(() -> msg, true);
        return 1;
    }

    private static int executeGet(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        CommandSourceStack source = ctx.getSource();
        ItemStack held = target.getMainHandItem().is(ModItems.BACULO.get())
                ? target.getMainHandItem()
                : target.getOffhandItem();

        int unlockedLevel = target.getPersistentData().getInt("UnlockedBaculoLevel").orElse(1);
        BaculoTier unlockedTier = BaculoTier.byLevel(Math.max(1, unlockedLevel));

        Component msg;
        if (held.is(ModItems.BACULO.get())) {
            BaculoTier heldTier = BaculoItem.getStaffTier(held);
            msg = Component.literal("ℹ " + target.getName().getString() + " sostiene un Báculo ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal("Nivel " + heldTier.getLevel() + " (" + heldTier.getDisplayName() + ")")
                            .withStyle(heldTier.getFormatting(), ChatFormatting.BOLD))
                    .append(Component.literal(". Nivel permanente desbloqueado: " + Math.max(1, unlockedLevel) + " (" + unlockedTier.getDisplayName() + ").")
                            .withStyle(ChatFormatting.GRAY));
        } else {
            msg = Component.literal("ℹ " + target.getName().getString() + " no sostiene un Báculo. Nivel permanente desbloqueado: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(Math.max(1, unlockedLevel) + " (" + unlockedTier.getDisplayName() + ")")
                            .withStyle(unlockedTier.getFormatting(), ChatFormatting.BOLD));
        }

        source.sendSuccess(() -> msg, false);
        return 1;
    }
}
