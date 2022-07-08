package com.aaronrenner.discordnftbot.services;

import java.util.*;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.aaronrenner.discordnftbot.models.Contract;
import com.aaronrenner.discordnftbot.models.ContractCollection;
import com.aaronrenner.discordnftbot.models.ListingConfig;
import com.aaronrenner.discordnftbot.models.ListingsProperties;
import com.aaronrenner.discordnftbot.repositories.ListingConfigRepository;
import com.aaronrenner.discordnftbot.repositories.ListingEventRepository;

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
		//this.uScheduler.buildUpdateScheduler(this.contracts);
		this.uScheduler.start();
	}
}