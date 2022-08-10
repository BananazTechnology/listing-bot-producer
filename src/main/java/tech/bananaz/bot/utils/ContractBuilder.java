package tech.bananaz.bot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.bananaz.repositories.ListingConfigPagingRepository;
import tech.bananaz.bot.models.Contract;
import tech.bananaz.models.Listing;
import tech.bananaz.repositories.EventPagingRepository;

@Component
public class ContractBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContractBuilder.class);

	public Contract configProperties(Listing config, ListingConfigPagingRepository configs, EventPagingRepository events) throws RuntimeException, InterruptedException {
		Contract output = null;
		try {
			// If no server or outputChannel then throw exception
			output = new Contract();
			output.setEvents(events);
			output.setConfigs(configs);
			output.setConfig(config);
			output.setId(config.getId());
			output.setContractAddress(config.getContractAddress());
			output.setInterval(config.getInterval());
			output.setExcludeOpensea(config.getExcludeOpensea());
			output.setExcludeLooks(config.getExcludeLooksrare());
			output.setAutoRarity(config.getAutoRarity());
			output.setRaritySlug(config.getRaritySlugOverwrite());
			output.setSlug(config.getIsSlug());
			output.setShowBundles(config.getShowBundles());
			// If SOL then address is always a slug
			if(config.getSolanaOnOpensea()) output.setSlug(true);
			if(config.getPolygonOnOpensea()) output.setSlug(true);
			
		} catch (Exception e) {
			LOGGER.error("Check properties {}, Exception: {}", config.toString(), e.getMessage());
			throw new RuntimeException("Check properties " + config.toString() + ", Exception: " + e.getMessage());
		}
		return output;
	}

}
