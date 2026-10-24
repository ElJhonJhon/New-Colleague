package com.newcolleague.mod.entity;

/**
 * Enumerador de compatibilidad para roles de Kobold Girls.
 * Mapea directamente a AbstractTribalEntity.KoboldProfession.
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
    MINER,
    SORCERESS;

    public AbstractTribalEntity.KoboldProfession toProfession() {
        return AbstractTribalEntity.KoboldProfession.valueOf(this.name());
    }

    public static KoboldRole fromProfession(AbstractTribalEntity.KoboldProfession prof) {
        if (prof == null) return NONE;
        try {
            return valueOf(prof.name());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
