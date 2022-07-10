package tech.bananaz.bot.services;

import java.util.*;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tech.bananaz.bot.models.Contract;
import tech.bananaz.bot.models.ContractCollection;
import tech.bananaz.bot.models.ListingConfig;
import tech.bananaz.bot.models.ListingsProperties;
import tech.bananaz.bot.repositories.ListingConfigRepository;
import tech.bananaz.bot.repositories.ListingEventRepository;

@Component
public class RunetimeScheduler {
	
	@Autowired
	private ListingConfigRepository configs;
	
	@Autowired
	private ListingEventRepository events;
	
	@Autowired
	private UpdateScheduler uScheduler;
	
	@Autowired
	private ContractCollection contracts;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RunetimeScheduler.class);
	
	@PostConstruct
	public void init() throws RuntimeException, InterruptedException {
		LOGGER.info("--- Main App Statup ---");
		List<ListingConfig> listingStartupItems = configs.findAll();
		for(ListingConfig confItem : listingStartupItems) {
			// Build required components for each entry
			Contract watcher = new ListingsProperties().configProperties(confItem, this.configs, this.events);
			watcher.startListingsScheduler();
			// Add this to internal memory buffer
			this.contracts.addContract(watcher);
		}
		LOGGER.info("--- Init the UpdateScheduler ---");
		this.uScheduler.start();
	}
}