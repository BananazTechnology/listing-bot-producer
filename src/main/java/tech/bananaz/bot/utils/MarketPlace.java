package tech.bananaz.bot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MarketPlace {
    OPENSEA("opensea", "OS"),
    LOOKSRARE("looksrare", "LR");
	
    @Getter
    private String displayName;
    private String slug;

    @Override
    public String toString() {
        return this.displayName;
    }
    
    public String getSlug() {
    	return this.slug;
    }

    public static MarketPlace fromString(String displayName) {
    	for (MarketPlace unit : MarketPlace.values()) {
            if (displayName.equalsIgnoreCase(unit.displayName)) {
                return unit;
            }
        }
        return MarketPlace.valueOf(displayName);
    }
}