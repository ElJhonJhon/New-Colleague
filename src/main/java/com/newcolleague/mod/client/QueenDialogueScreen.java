package com.newcolleague.mod.client;

import com.newcolleague.mod.network.QueenDialogueActionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class QueenDialogueScreen extends Screen {

    private final int queenEntityId;
    private final String queenTitle;
    private final String tribeName;
    private final boolean pacifistRouteChosen;
    private final boolean isRuler;

    private enum Stage {
        MAIN_MENU,
        TRIBE_CHANGE_SUBMENU,
        RULER_GOVERNANCE
    }

    private Stage stage;

    public QueenDialogueScreen(int queenEntityId, String queenTitle, String tribeName, boolean pacifistRouteChosen, boolean isRuler) {
        super(Component.literal(queenTitle));
        this.queenEntityId = queenEntityId;
        this.queenTitle = queenTitle;
        this.tribeName = tribeName;
        this.pacifistRouteChosen = pacifistRouteChosen;
        this.isRuler = isRuler;
        this.stage = isRuler ? Stage.RULER_GOVERNANCE : Stage.MAIN_MENU;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int btnWidth = 240;
        int btnHeight = 20;

        if (this.stage == Stage.RULER_GOVERNANCE) {
            int startY = centerY - 55;

            // 1. Orden General: ¡Todas síganme!
            this.addRenderableWidget(Button.builder(
                    Component.literal("📢 ¡Toda la tribu, síganme! (Acompañar)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                    btn -> {
                        ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                this.queenEntityId,
                                QueenDialogueActionPayload.ACTION_RULER_CALL_ALL_FOLLOW
                        ));
                        this.onClose();
                    }
            ).bounds(centerX - btnWidth / 2, startY, btnWidth, btnHeight).build());

            // 2. Orden General: ¡Todas a trabajar!
            this.addRenderableWidget(Button.builder(
                    Component.literal("🌾 ¡Todas a sus puestos de trabajo! (Rutina)").withStyle(ChatFormatting.GREEN),
                    btn -> {
                        ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                this.queenEntityId,
                                QueenDialogueActionPayload.ACTION_RULER_CALL_ALL_WORK
                        ));
                        this.onClose();
                    }
            ).bounds(centerX - btnWidth / 2, startY + 24, btnWidth, btnHeight).build());

            // 3. Orden General: ¡Toda la tribu en guardia!
            this.addRenderableWidget(Button.builder(
                    Component.literal("🛡 ¡Toda la tribu en guardia! (Defender)").withStyle(ChatFormatting.YELLOW),
                    btn -> {
                        ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                this.queenEntityId,
                                QueenDialogueActionPayload.ACTION_RULER_CALL_ALL_GUARD
                        ));
                        this.onClose();
                    }
            ).bounds(centerX - btnWidth / 2, startY + 48, btnWidth, btnHeight).build());

            // 4. Recaudar tributos de la tribu
            this.addRenderableWidget(Button.builder(
                    Component.literal("📦 Recaudar tributos del día").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    btn -> {
                        ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                this.queenEntityId,
                                QueenDialogueActionPayload.ACTION_RULER_COLLECT_TRIBUTES
                        ));
                        this.onClose();
                    }
            ).bounds(centerX - btnWidth / 2, startY + 72, btnWidth, btnHeight).build());

            // 5. Censo y reporte de la tribu
            this.addRenderableWidget(Button.builder(
                    Component.literal("📋 Reporte y censo de la tribu").withStyle(ChatFormatting.WHITE),
                    btn -> {
                        ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                this.queenEntityId,
                                QueenDialogueActionPayload.ACTION_RULER_TRIBE_REPORT
                        ));
                        this.onClose();
                    }
            ).bounds(centerX - btnWidth / 2, startY + 96, btnWidth, btnHeight).build());

            // 6. Cerrar
            this.addRenderableWidget(Button.builder(
                    Component.literal("Cerrar").withStyle(ChatFormatting.GRAY),
                    btn -> this.onClose()
            ).bounds(centerX - 60, startY + 122, 120, btnHeight).build());

        } else if (this.stage == Stage.MAIN_MENU) {
            // Opción 1: "Solo estoy de paso"
            this.addRenderableWidget(Button.builder(
                    Component.literal("Solo estoy de paso").withStyle(ChatFormatting.WHITE),
                    btn -> {
                        ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                this.queenEntityId,
                                QueenDialogueActionPayload.ACTION_JUST_PASSING
                        ));
                        this.onClose();
                    }
            ).bounds(centerX - btnWidth / 2, centerY - 10, btnWidth, btnHeight).build());

            // Opción 2: Si ya eligió la ruta pacifista, ingresar directamente a los tradeos sin menú redundante
            if (this.pacifistRouteChosen) {
                this.addRenderableWidget(Button.builder(
                        Component.literal("🌱 Desarrollo de la tribu (Entregar ofrendas)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                        btn -> {
                            ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                    this.queenEntityId,
                                    QueenDialogueActionPayload.ACTION_PASSIVE_PROGRESS
                            ));
                            this.onClose();
                        }
                ).bounds(centerX - btnWidth / 2, centerY + 18, btnWidth, btnHeight).build());
            } else {
                this.addRenderableWidget(Button.builder(
                        Component.literal("Esta tribu necesita un cambio").withStyle(ChatFormatting.GOLD),
                        btn -> {
                            this.stage = Stage.TRIBE_CHANGE_SUBMENU;
                            this.init();
                        }
                ).bounds(centerX - btnWidth / 2, centerY + 18, btnWidth, btnHeight).build());
            }
        } else {
            if (!this.pacifistRouteChosen) {
                // Opción A: "Te reto por el dominio" (Ruta Agresiva)
                this.addRenderableWidget(Button.builder(
                        Component.literal("⚔ Te reto por el dominio").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                        btn -> {
                            ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                    this.queenEntityId,
                                    QueenDialogueActionPayload.ACTION_CHALLENGE_DUEL
                            ));
                            this.onClose();
                        }
                ).bounds(centerX - btnWidth / 2, centerY - 20, btnWidth, btnHeight).build());

                // Opción B: "Ustedes necesitan más recursos para progresar" (Ruta Pasiva)
                this.addRenderableWidget(Button.builder(
                        Component.literal("🌱 Ustedes necesitan más recursos para progresar").withStyle(ChatFormatting.GREEN),
                        btn -> {
                            ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                    this.queenEntityId,
                                    QueenDialogueActionPayload.ACTION_PASSIVE_PROGRESS
                            ));
                            this.onClose();
                        }
                ).bounds(centerX - btnWidth / 2, centerY + 8, btnWidth, btnHeight).build());

                // Botón Volver
                this.addRenderableWidget(Button.builder(
                        Component.literal("↩ Volver").withStyle(ChatFormatting.GRAY),
                        btn -> {
                            this.stage = Stage.MAIN_MENU;
                            this.init();
                        }
                ).bounds(centerX - 50, centerY + 38, 100, btnHeight).build());
            } else {
                this.addRenderableWidget(Button.builder(
                        Component.literal("🌱 Entregar ofrendas de desarrollo (Tradeo)").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                        btn -> {
                            ClientPacketDistributor.sendToServer(new QueenDialogueActionPayload(
                                    this.queenEntityId,
                                    QueenDialogueActionPayload.ACTION_PASSIVE_PROGRESS
                            ));
                            this.onClose();
                        }
                ).bounds(centerX - btnWidth / 2, centerY - 10, btnWidth, btnHeight).build());

                // Botón Volver
                this.addRenderableWidget(Button.builder(
                        Component.literal("↩ Volver").withStyle(ChatFormatting.GRAY),
                        btn -> {
                            this.stage = Stage.MAIN_MENU;
                            this.init();
                        }
                ).bounds(centerX - 50, centerY + 22, 100, btnHeight).build());
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(guiGraphicsExtractor);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int cardWidth = 280;
        int cardHeight = this.stage == Stage.RULER_GOVERNANCE ? 210 : 150;
        int cardLeft = centerX - cardWidth / 2;
        int cardTop = this.stage == Stage.RULER_GOVERNANCE ? centerY - 100 : centerY - 65;
        int cardRight = cardLeft + cardWidth;
        int cardBottom = cardTop + cardHeight;

        // Fondo del diálogo oscuro translúcido
        guiGraphicsExtractor.fill(cardLeft, cardTop, cardRight, cardBottom, 0xEE141418);

        // Borde decorativo dorado
        guiGraphicsExtractor.fill(cardLeft, cardTop, cardRight, cardTop + 2, 0xFFFFD700);
        guiGraphicsExtractor.fill(cardLeft, cardBottom - 2, cardRight, cardBottom, 0xFFFFD700);
        guiGraphicsExtractor.fill(cardLeft, cardTop, cardLeft + 2, cardBottom, 0xFFFFD700);
        guiGraphicsExtractor.fill(cardRight - 2, cardTop, cardRight, cardBottom, 0xFFFFD700);

        // Título de la Reina
        String header = this.stage == Stage.RULER_GOVERNANCE
                ? "👑 " + this.queenTitle + " (Tribu " + this.tribeName + ")"
                : "👑 " + this.queenTitle;
        int headerWidth = this.font.width(header);
        guiGraphicsExtractor.text(this.font, Component.literal(header), centerX - headerWidth / 2, cardTop + 10, 0xFFFFD700, true);

        // Subtítulo / Pregunta de la Reina
        String prompt;
        if (this.stage == Stage.RULER_GOVERNANCE) {
            prompt = "¡A sus órdenes, mi Gobernador! ¿Qué ordenas a la tribu?";
        } else if (this.stage == Stage.MAIN_MENU) {
            prompt = this.pacifistRouteChosen
                    ? "Nuestra alianza avanza. ¿Qué asuntos te traen hoy?"
                    : "¿Qué asuntos te traen ante mi presencia?";
        } else {
            prompt = this.pacifistRouteChosen
                    ? "¿Deseas entregar ofrendas para el progreso del pueblo?"
                    : "¿Un cambio dices? ¿Qué pretendes para mi pueblo?";
        }
        int promptWidth = this.font.width(prompt);
        guiGraphicsExtractor.text(this.font, Component.literal(prompt), centerX - promptWidth / 2, cardTop + 26, 0xFFFFFFAA, true);

        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
