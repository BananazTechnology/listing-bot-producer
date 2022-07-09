package tech.bananaz.bot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MarketPlace {
    OPENSEA("opensea"),
    LOOKSRARE("looksrare");
	
    @Getter
    private String displayName;

    @Override
    public String toString() {
        return this.displayName;
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