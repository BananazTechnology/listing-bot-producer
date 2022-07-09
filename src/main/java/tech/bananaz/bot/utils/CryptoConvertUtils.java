package tech.bananaz.bot.utils;

import java.math.BigDecimal;

/**
 * 
 * @author aaronrenner
 * @version 1.0.1
 */
public class CryptoConvertUtils {

	public BigDecimal convertToCrypto(String value, int factor) {
		// Convert the value into a larger memory buffer
		BigDecimal valueToBD = new BigDecimal(value);
		// Convert factor
		BigDecimal divisor 	 = BigDecimal.TEN.pow(factor);
		BigDecimal result  	 = valueToBD.divide(divisor);
		return result;
	}
	
	public BigDecimal convertToCrypto(String value, Unit unit) {
		BigDecimal valueToBD = new BigDecimal(value);
		BigDecimal divisor 	 = unit.getFactor();
		BigDecimal result  	 = valueToBD.divide(divisor);
		return result;
	}
	
	public enum Unit {
        ETH("eth", 18),
        SOL("sol", 9);

        private String name;
        private int factor;

        Unit(String name, int factor) {
            this.name = name;
            this.factor = factor;
        }
        
        public Integer getDecimal() {
        	return this.factor;
        }

        public BigDecimal getFactor() {
        	return BigDecimal.TEN.pow(this.factor);
        }

        @Override
        public String toString() {
            return name;
        }

        public static Unit fromString(String name) {
            if (name != null) {
                for (Unit unit : Unit.values()) {
                    if (name.equalsIgnoreCase(unit.name)) {
                        return unit;
                    }
                }
            }
            return Unit.valueOf(name);
        }
    }
}