package com.newcolleague.mod.village;

public enum VillageTier {
    NOMAD(1, "Campamento Nómada", "🏕", 0),
    ESTABLISHED(2, "Aldea Establecida", "🌾", 100),
    DIAMOND_FORTRESS(3, "Fortaleza de Diamante", "💎", 300);

    private final int level;
    private final String displayName;
    private final String icon;
    private final int requiredProsperity;

    VillageTier(int level, String displayName, String icon, int requiredProsperity) {
        this.level = level;
        this.displayName = displayName;
        this.icon = icon;
        this.requiredProsperity = requiredProsperity;
    }

    public int getLevel() {
        return this.level;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getIcon() {
        return this.icon;
    }

    public int getRequiredProsperity() {
        return this.requiredProsperity;
    }

    public static VillageTier fromLevel(int lvl) {
        for (VillageTier t : values()) {
            if (t.level == lvl) return t;
        }
        return NOMAD;
    }
}
