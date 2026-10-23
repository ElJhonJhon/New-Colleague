package com.newcolleague.mod.entity;

/**
 * Enumerador de compatibilidad para roles de Kobold Girls.
 * Mapea directamente a KoboldGirlEntity.KoboldProfession.
 */
public enum KoboldRole {
    NONE,
    FARMER,
    GUARD,
    VIGILANT,
    HUNTER,
    BUILDER,
    ALCHEMIST,
    SCOUT,
    MINER;

    public KoboldGirlEntity.KoboldProfession toProfession() {
        return KoboldGirlEntity.KoboldProfession.valueOf(this.name());
    }

    public static KoboldRole fromProfession(KoboldGirlEntity.KoboldProfession prof) {
        if (prof == null) return NONE;
        try {
            return valueOf(prof.name());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
