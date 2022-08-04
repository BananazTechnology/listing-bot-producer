package tech.bananaz.bot.services;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.bananaz.bot.models.Contract;
import tech.bananaz.bot.models.ContractCollection;
import tech.bananaz.bot.utils.ContractBuilder;
import tech.bananaz.models.Listing;
import tech.bananaz.repositories.ListingConfigPagingRepository;
import tech.bananaz.repositories.EventPagingRepository;

@Component
public class RunetimeInitializer {
	
	@Autowired
	private ListingConfigPagingRepository configs;
	
	@Autowired
	private EventPagingRepository events;
	
	@Autowired
	private UpdateScheduler uScheduler;
	
	@Autowired
	private ContractCollection contracts;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RunetimeInitializer.class);
	
	@PostConstruct
	public void init() throws RuntimeException, InterruptedException {
		LOGGER.debug("--- Main App Statup ---");
		Iterable<Listing> listingStartupItems = configs.findAll();
		for(Listing confItem : listingStartupItems) {
			// Build required components for each entry
			Contract watcher = new ContractBuilder().configProperties(confItem, this.configs, this.events);
			watcher.startListingsScheduler();
			// Add this to internal memory buffer
			this.contracts.addContract(watcher);
		}
		LOGGER.debug("--- Init the ListingUpdateScheduler ---");
		this.uScheduler.start();
	}
}