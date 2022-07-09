package tech.bananaz.bot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum RarityEngine {
    RARITY_TOOLS("raritytools", "https://rarity.tools/%s/view/%s", "rarity.tools"),
    RARITY_SNIPER("raritysniper", "https://raritysniper.com/%s/%s", "raritysniper.com"),
    TRAIT_SNIPER("traitsniper", "https://app.traitsniper.com/%s?view=%s", "traitsniper.com");

    private String engineSlug;
    @Getter
    private String url;
    @Getter
    private String displayName;

    @Override
    public String toString() {
        return this.engineSlug;
    }

    public static RarityEngine fromString(String engineSlug) {
    	for (RarityEngine unit : RarityEngine.values()) {
            if (engineSlug.equalsIgnoreCase(unit.engineSlug)) {
                return unit;
            }
        }
        return RarityEngine.valueOf(engineSlug);
    }
}