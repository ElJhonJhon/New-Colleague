package com.newcolleague.mod.client;

import com.newcolleague.mod.network.RenameKoboldPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class RenameKoboldScreen extends Screen {

    private final int entityId;
    private final String initialName;
    private final boolean isTribeRename;
    private EditBox nameEditBox;

    public RenameKoboldScreen(int entityId, String initialName, boolean isTribeRename) {
        super(Component.literal(isTribeRename ? "Renombrar Tribu" : "Renombrar Kobold"));
        this.entityId = entityId;
        this.initialName = initialName != null ? initialName : "";
        this.isTribeRename = isTribeRename;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameEditBox = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20, Component.literal("Nombre"));
        this.nameEditBox.setMaxLength(32);
        this.nameEditBox.setValue(this.initialName);
        this.addRenderableWidget(this.nameEditBox);
        this.setInitialFocus(this.nameEditBox);

        // Botón Confirmar
        this.addRenderableWidget(Button.builder(
                Component.literal("Confirmar").withStyle(ChatFormatting.GREEN),
                btn -> this.confirmAndClose()
        ).bounds(centerX - 102, centerY + 20, 100, 20).build());

        // Botón Cancelar
        this.addRenderableWidget(Button.builder(
                Component.literal("Cancelar").withStyle(ChatFormatting.RED),
                btn -> this.onClose()
        ).bounds(centerX + 2, centerY + 20, 100, 20).build());
    }

    private void confirmAndClose() {
        String newName = this.nameEditBox.getValue().trim();
        ClientPacketDistributor.sendToServer(new RenameKoboldPayload(this.entityId, newName, this.isTribeRename));
        this.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(guiGraphicsExtractor);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int cardLeft = centerX - 120;
        int cardRight = centerX + 120;
        int cardTop = centerY - 55;
        int cardBottom = centerY + 55;

        // Fondo y borde
        guiGraphicsExtractor.fill(cardLeft, cardTop, cardRight, cardBottom, 0xEE141418);
        int borderColor = this.isTribeRename ? 0xFFFFD700 : 0xFF55AAFF;
        guiGraphicsExtractor.fill(cardLeft, cardTop, cardRight, cardTop + 2, borderColor);
        guiGraphicsExtractor.fill(cardLeft, cardBottom - 2, cardRight, cardBottom, borderColor);
        guiGraphicsExtractor.fill(cardLeft, cardTop, cardLeft + 2, cardBottom, borderColor);
        guiGraphicsExtractor.fill(cardRight - 2, cardTop, cardRight, cardBottom, borderColor);

        // Título
        String title = this.isTribeRename ? "🏰 Cambiar Nombre de la Tribu" : "🏷 Renombrar Kobold Girl";
        int titleWidth = this.font.width(title);
        guiGraphicsExtractor.text(this.font, Component.literal(title), centerX - titleWidth / 2, cardTop + 10, borderColor, true);

        // Subtítulo
        String prompt = this.isTribeRename ? "Ingresa el nuevo nombre para la tribu:" : "Ingresa el nombre personalizado:";
        int promptWidth = this.font.width(prompt);
        guiGraphicsExtractor.text(this.font, Component.literal(prompt), centerX - promptWidth / 2, cardTop + 24, 0xFFFFFFAA, true);

        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
    }
}
