package com.newcolleague.mod.item;

import com.newcolleague.mod.NewColleagueMod;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

public enum BaculoTier {
    COBBLESTONE(1, "cobblestone", "Adoquín", "baculo_cobblestone", ChatFormatting.GRAY, 1),
    DEEPSLATE_TILES(2, "deepslate_tiles", "Pizarra Profunda", "baculo_deepslate_tiles", ChatFormatting.DARK_GRAY, 2),
    COPPER(3, "copper", "Cobre", "baculo_copper", ChatFormatting.GOLD, 2),
    IRON(4, "iron", "Hierro", "baculo_iron", ChatFormatting.WHITE, 2),
    GOLD(5, "gold", "Oro", "baculo_gold", ChatFormatting.YELLOW, 2),
    DIAMOND(6, "diamond", "Diamante", "baculo_diamond", ChatFormatting.AQUA, 3),
    NETHERITE(7, "netherite", "Netherite", "baculo_netherite", ChatFormatting.DARK_PURPLE, 3);

    private final int level;
    private final String id;
    private final String displayName;
    private final String textureName;
    private final ChatFormatting formatting;
    private final int regenAmplifier; // 0 = Regen I, 1 = Regen II, 2 = Regen III, 3 = Regen IV
    private final Identifier textureLocation;

    BaculoTier(int level, String id, String displayName, String textureName, ChatFormatting formatting, int regenAmplifier) {
        this.level = level;
        this.id = id;
        this.displayName = displayName;
        this.textureName = textureName;
        this.formatting = formatting;
        this.regenAmplifier = regenAmplifier;
        this.textureLocation = Identifier.fromNamespaceAndPath(NewColleagueMod.MODID, "textures/item/" + textureName + ".png");
    }

    public int getLevel() {
        return this.level;
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getTextureName() {
        return this.textureName;
    }

    public ChatFormatting getFormatting() {
        return this.formatting;
    }

    public int getRegenAmplifier() {
        return this.regenAmplifier;
    }

    public Identifier getTextureLocation() {
        return this.textureLocation;
    }

    public static BaculoTier byLevel(int level) {
        for (BaculoTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }
        return level > 7 ? NETHERITE : COBBLESTONE;
    }

    public static BaculoTier byNameOrId(String query) {
        if (query == null) return null;
        String clean = query.trim().toLowerCase();
        for (BaculoTier tier : values()) {
            if (tier.id.equalsIgnoreCase(clean)
                    || tier.name().equalsIgnoreCase(clean)
                    || tier.displayName.equalsIgnoreCase(clean)
                    || String.valueOf(tier.level).equals(clean)) {
                return tier;
            }
        }
        // Aliases comunes en español
        if (clean.equals("adoquin") || clean.equals("adoquín") || clean.equals("piedra")) return COBBLESTONE;
        if (clean.equals("pizarra") || clean.equals("deepslate") || clean.equals("baldosa") || clean.equals("baldoza") || clean.equals("deepslate_tile")) return DEEPSLATE_TILES;
        if (clean.equals("cobre")) return COPPER;
        if (clean.equals("hierro")) return IRON;
        if (clean.equals("oro")) return GOLD;
        if (clean.equals("diamante")) return DIAMOND;
        if (clean.equals("netherita") || clean.equals("netherite")) return NETHERITE;
        return null;
    }
}
