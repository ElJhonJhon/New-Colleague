package com.newcolleague.mod.client;

import com.newcolleague.mod.NewColleagueMod;
import com.newcolleague.mod.entity.KoboldGirlEntity;
import com.newcolleague.mod.menu.KoboldGirlMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class KoboldGirlScreen extends AbstractContainerScreen<KoboldGirlMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "textures/gui/container/kobold_girl_inventory.png");

    private float xMouse;
    private float yMouse;

    public KoboldGirlScreen(KoboldGirlMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 236);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 138;
    }

    @Override
    protected void init() {
        super.init();
        KoboldGirlEntity kobold = this.menu.getKoboldGirl();
        if (kobold != null && this.minecraft != null && this.minecraft.player != null && kobold.isOwnedBy(this.minecraft.player)) {
            int x = this.leftPos;
            int y = this.topPos;
            this.addRenderableWidget(Button.builder(
                    Component.literal("✎ Renombrar").withStyle(ChatFormatting.YELLOW),
                    btn -> {
                        String currentName = kobold.getCustomGivenName();
                        if (currentName.isEmpty()) {
                            currentName = kobold.isAnyQueen() ? kobold.getQueenTitle() : kobold.getProfession().getDisplayName();
                        }
                        this.minecraft.setScreenAndShow(new RenameKoboldScreen(kobold.getId(), currentName, false));
                    }
            ).bounds(x + 102, y + 66, 66, 13).build());
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;

        // Dibujar textura de fondo
        guiGraphicsExtractor.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0.0F, 0.0F,
                this.imageWidth, this.imageHeight,
                256, 256
        );

        // Renderizar vista previa 3D de la Kobold Girl en el marco central
        KoboldGirlEntity kobold = this.menu.getKoboldGirl();
        if (kobold != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    guiGraphicsExtractor,
                    x + 26, y + 8,
                    x + 77, y + 78,
                    30, 0.0625F,
                    this.xMouse, this.yMouse,
                    kobold
            );
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY) {
        super.extractLabels(guiGraphicsExtractor, mouseX, mouseY);

        KoboldGirlEntity kobold = this.menu.getKoboldGirl();
        if (kobold != null) {
            int hp = (int) Math.ceil(kobold.getHealth());
            int maxHp = (int) kobold.getMaxHealth();
            int food = kobold.getFoodLevel();

            // 1. Placa/Distintivo de la Tribu y Profesión
            String tribeName = kobold.hasTribe() ? kobold.getTribeName() : "Sin Tribu";
            int tribeColor;
            if (kobold.isInPowerStruggle()) {
                tribeColor = 0xFFFF4422; // Naranja-rojo intenso de guerra civil
            } else if (kobold.isSurvivorQueen()) {
                tribeColor = 0xFFFFD700; // Dorado resplandeciente para Reina Sobreviviente
            } else if (kobold.isRebel()) {
                tribeColor = 0xFFFF5555; // Rojo para Súbdita Rebelde
            } else if (kobold.isQueen()) {
                tribeColor = 0xFFFFD700; // Dorado para Reina
            } else {
                tribeColor = switch (kobold.getProfession()) {
                    case FARMER -> 0xFF88DD55;   // Verde campo para Granjera
                    case GUARD -> 0xFF55AAFF;    // Azul férreo para Guardia
                    case VIGILANT -> 0xFF55FFFF; // Cian para Vigilante
                    case HUNTER -> 0xFFFFAA44;   // Naranja para Cazadora
                    case BUILDER -> 0xFFFFD700;  // Dorado para Constructora
                    case SORCERESS -> 0xFFFF55FF;// Púrpura / Rosa para Hechicera
                    default -> 0xFFEEDD88;       // Amarillo suave para Súbdita estándar
                };
            }

            // Fondo del distintivo de tribu
            guiGraphicsExtractor.fill(80, 7, 168, 22, 0x33000000);

            // Formato de texto ajustado al ancho disponible (88 px)
            String labelText;
            if (kobold.isInPowerStruggle()) {
                labelText = "Guerra: " + tribeName;
            } else if (kobold.isSurvivorQueen() || kobold.isQueen()) {
                labelText = "👑 " + tribeName;
            } else if (kobold.isRebel()) {
                labelText = "Rebelde: " + tribeName;
            } else if (kobold.getProfession() != KoboldGirlEntity.KoboldProfession.NONE) {
                labelText = kobold.getProfession().getDisplayName() + ": " + tribeName;
            } else {
                labelText = "Tribu: " + tribeName;
            }
            String textToDraw;
            if (this.font.width(labelText) <= 84) {
                textToDraw = labelText;
            } else if (this.font.width(tribeName) <= 84) {
                textToDraw = tribeName;
            } else {
                textToDraw = this.font.plainSubstrByWidth(tribeName, 76) + "...";
            }
            int textX = 80 + (88 - this.font.width(textToDraw)) / 2;
            guiGraphicsExtractor.text(this.font, Component.literal(textToDraw), textX, 11, tribeColor, true);

            // 2. Fondo sutil traslúcido para legibilidad perfecta de HP y Comida
            guiGraphicsExtractor.fill(100, 24, 168, 41, 0x33000000);
            guiGraphicsExtractor.fill(100, 46, 168, 63, 0x33000000);

            // Estadísticas visibles de Salud (Rojo 100% opaco) y Comida (Naranja 100% opaco) con sombra
            guiGraphicsExtractor.text(this.font, Component.literal("HP: " + hp + "/" + maxHp), 104, 29, 0xFFFF3333, true);
            guiGraphicsExtractor.text(this.font, Component.literal("Comida: " + food + "/20"), 104, 51, 0xFFFF9900, true);

            // Tooltips informativos al pasar el cursor sobre los indicadores
            int mouseXLocal = mouseX - this.leftPos;
            int mouseYLocal = mouseY - this.topPos;
            if (mouseXLocal >= 80 && mouseXLocal <= 168 && mouseYLocal >= 7 && mouseYLocal <= 22) {
                Component roleLine;
                if (kobold.isInPowerStruggle()) {
                    roleLine = Component.literal("Estado: Guerra por el Trono").withStyle(ChatFormatting.RED);
                } else if (kobold.isSurvivorQueen()) {
                    roleLine = Component.literal("Rango: Reina Sobreviviente").withStyle(ChatFormatting.GOLD);
                } else if (kobold.isQueen()) {
                    roleLine = Component.literal("Rango: Reina").withStyle(ChatFormatting.YELLOW);
                } else if (kobold.isRebel()) {
                    roleLine = Component.literal("Rango: Súbdita Rebelde").withStyle(ChatFormatting.RED);
                } else {
                    KoboldGirlEntity.KoboldProfession prof = kobold.getProfession();
                    ChatFormatting profFormat = switch (prof) {
                        case FARMER -> ChatFormatting.GREEN;
                        case GUARD -> ChatFormatting.BLUE;
                        case VIGILANT -> ChatFormatting.AQUA;
                        case HUNTER -> ChatFormatting.GOLD;
                        case BUILDER -> ChatFormatting.YELLOW;
                        case SORCERESS -> ChatFormatting.LIGHT_PURPLE;
                        default -> ChatFormatting.GRAY;
                    };
                    roleLine = Component.literal("Profesión: " + prof.getDisplayName()).withStyle(profFormat);
                }

                Component tooltip = Component.literal("Tribu: " + tribeName)
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("\n").append(roleLine));

                if (!kobold.isQueen() && !kobold.isSurvivorQueen() && !kobold.isRebel() && !kobold.isInPowerStruggle()) {
                    KoboldGirlEntity.KoboldProfession prof = kobold.getProfession();
                    String details = switch (prof) {
                        case FARMER -> "• Siembra, cosecha y hornea pan\n• Pacífica (autodefensa)";
                        case GUARD -> "• Espadas/mazos y escudos\n• +5% Daño Cuerpo a Cuerpo\n• Defiende a la tribu";
                        case VIGILANT -> "• Arcos y ballestas con munición\n• +5% Daño a Distancia\n• Defiende a la tribu";
                        case HUNTER -> "• Caza animales con hachas/arcos\n• Cocina carne en fogatas\n• Pacífica (autodefensa)";
                        case BUILDER -> "• Acondiciona el terreno de aldea\n• Construye proyectos y edificios\n• Pacífica (autodefensa)";
                        case SORCERESS -> "• Canaliza magia arcana ancestral\n• Rayos, Colmillos y Fuego\n• Defiende a la tribu";
                        default -> "";
                    };
                    if (!details.isEmpty()) {
                        tooltip = tooltip.copy().append(Component.literal("\n" + details).withStyle(ChatFormatting.GRAY));
                    }
                }
                guiGraphicsExtractor.setTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
            } else if (mouseXLocal >= 100 && mouseXLocal <= 168 && mouseYLocal >= 24 && mouseYLocal <= 41) {
                guiGraphicsExtractor.setTooltipForNextFrame(this.font, Component.literal("Salud: " + hp + " / " + maxHp), mouseX, mouseY);
            } else if (mouseXLocal >= 100 && mouseXLocal <= 168 && mouseYLocal >= 46 && mouseYLocal <= 63) {
                guiGraphicsExtractor.setTooltipForNextFrame(this.font, Component.literal("Hambre: " + food + " / 20"), mouseX, mouseY);
            }
        }
    }
}
