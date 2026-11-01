package com.newcolleague.mod.entity;

import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public enum KoboldTribeVariant {
    DEFAULT(0, "default", "Original", "textures/entity/kobold_girl.png", "textures/entity/kobold_girl_survivor.png"),
    RED(1, "red", "Roja", "textures/entity/kobold_girl_red.png", "textures/entity/kobold_girl_red_survivor.png"),
    BLUE(2, "blue", "Azul", "textures/entity/kobold_girl_blue.png", "textures/entity/kobold_girl_blue_survivor.png"),
    GREEN(3, "green", "Verde", "textures/entity/kobold_girl_green.png", "textures/entity/kobold_girl_green_survivor.png"),
    PURPLE(4, "purple", "Morada", "textures/entity/kobold_girl_purple.png", "textures/entity/kobold_girl_purple_survivor.png");

    private final int id;
    private final String serializedName;
    private final String displayName;
    private final String texturePath;
    private final String survivorTexturePath;

    KoboldTribeVariant(int id, String serializedName, String displayName, String texturePath, String survivorTexturePath) {
        this.id = id;
        this.serializedName = serializedName;
        this.displayName = displayName;
        this.texturePath = texturePath;
        this.survivorTexturePath = survivorTexturePath;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.serializedName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getTexturePath() {
        return this.texturePath;
    }

    public String getSurvivorTexturePath() {
        return this.survivorTexturePath;
    }

    public static KoboldTribeVariant byId(int id) {
        for (KoboldTribeVariant variant : values()) {
            if (variant.id == id) {
                return variant;
            }
        }
        return DEFAULT;
    }

    public static @Nullable KoboldTribeVariant byName(String name) {
        for (KoboldTribeVariant variant : values()) {
            if (variant.serializedName.equalsIgnoreCase(name) || variant.name().equalsIgnoreCase(name)) {
                return variant;
            }
        }
        return null;
    }

    public static KoboldTribeVariant getRandom(RandomSource random) {
        KoboldTribeVariant[] values = values();
        return values[random.nextInt(values.length)];
    }

    public static int getTotalVariants() {
        return values().length;
    }
}
