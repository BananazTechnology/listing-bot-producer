package tech.bananaz.bot.services;

import java.util.*;
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
import tech.bananaz.bot.utils.RarityEngine;

import static java.util.Objects.nonNull;
import static tech.bananaz.bot.utils.StringUtils.nonEquals;

@Component
public class UpdateScheduler extends TimerTask {
	
	@Autowired
	private ListingConfigRepository configs;
	
	@Autowired
	private ContractCollection contracts;
	
	@Autowired
	private ListingEventRepository events;
	
	/** Important variables needed for Runtime */
	private final int REFRESH_REQ = 60000;
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateScheduler.class);
	private Timer timer = new Timer(); // creating timer
    private TimerTask task; // creating timer task
    private boolean active = false;
	
	public boolean start() {
		if(nonNull(this.contracts)) {
			this.active = true;
			this.task   = this;
			LOGGER.info(String.format("Starting new UpdateScheduler"));
			// Starts this new timer, starts at random time and runs per <interval> milliseconds
			this.timer.schedule(task, 1, REFRESH_REQ);
		}
		return active;
	}
	
	public boolean stop() {
		this.active = false;
		LOGGER.info("Stopping UpdateScheduler");
		return active;
	}

	@Override
	public void run() {
		if(nonNull(this.contracts) && active) {
			List<ListingConfig> allListingConfigs = this.configs.findAll();
			for(ListingConfig conf : allListingConfigs) {
				try {
					List<String> updatedItems = new ArrayList<>();
					Contract cont = this.contracts.getContractById(conf.getId());
					// Update existing object in memory
					if(nonNull(cont)) {
						// Strings and Integers
						// Contract Address
						if(nonEquals(cont.getContractAddress(), conf.getContractAddress())) {
							updatedItems.add(String.format("contractAddress: %s->%s", cont.getContractAddress(), conf.getContractAddress()));
							cont.setContractAddress(conf.getContractAddress());
						}
						// Interval
						if(nonEquals(cont.getInterval(), conf.getInterval())) {
							updatedItems.add(String.format("interval: %s->%s", cont.getInterval(), conf.getInterval()));
							cont.setInterval(conf.getInterval());
						}
						// Rarity Slug
						if(nonEquals(cont.getRaritySlug(), conf.getRaritySlugOverwrite())) {
							updatedItems.add(String.format("raritySlug: %s->%s", cont.getRaritySlug(), conf.getRaritySlugOverwrite()));
							cont.setRaritySlug(conf.getRaritySlugOverwrite());
						}
						// Rarity Engine
						RarityEngine rarityEngine = (conf.getRarityEngine() != null) ? RarityEngine.fromString(conf.getRarityEngine()): RarityEngine.RARITY_TOOLS;
						if(nonEquals(cont.getEngine().toString(), rarityEngine)) {
							updatedItems.add(String.format("raritySlug: %s->%s", cont.getEngine().toString(), conf.getAutoRarity()));
							cont.setEngine(rarityEngine);
						}
						

						// Booleans
						// Auto Rarity
						if(nonEquals(cont.isAutoRarity(), conf.getAutoRarity())) {
							updatedItems.add(String.format("autoRarity: %s->%s", cont.isAutoRarity(), conf.getAutoRarity()));
							cont.setAutoRarity(conf.getAutoRarity());
						}
						// Show Bundles
						if(nonEquals(cont.isShowBundles(), conf.getShowBundles())) {
							updatedItems.add(String.format("showBundles: %s->%s", cont.isShowBundles(), conf.getShowBundles()));
							cont.setShowBundles(conf.getShowBundles());
						}
						// Exclude OpenSea
						if(nonEquals(cont.isExcludeOpensea(), conf.getExcludeOpensea())) {
							updatedItems.add(String.format("excludeOpensea: %s->%s", cont.isExcludeOpensea(), conf.getExcludeOpensea()));
							cont.setExcludeOpensea(conf.getExcludeOpensea());
						}
						// Exclude Looksrare
						if(nonEquals(cont.isExcludeLooks(), conf.getExcludeLooksrare())) {
							updatedItems.add(String.format("excludeLooksrare: %s->%s", cont.isExcludeLooks(), conf.getExcludeLooksrare()));
							cont.setExcludeLooks(conf.getExcludeLooksrare());
						}
						// Active
						if(nonEquals(cont.isActive(), conf.getActive())) {
							updatedItems.add(String.format("active: %s->%s", cont.isActive(), conf.getActive()));
							cont.setActive(conf.getActive());
						}

					} 
					// Add new contract
					else {
						LOGGER.debug("Object NOT found in memory, building new");
						// Build required components for each entry
						Contract watcher = new ListingsProperties().configProperties(conf, this.configs, this.events);
						// Start the watcher
						watcher.startListingsScheduler();
						// Add this to internal memory buffer
						this.contracts.addContract(watcher);
						updatedItems.add(String.format("new: %s", watcher));
					}
					if(updatedItems.size() > 0) LOGGER.debug("Contract {} updated {}", cont.getId(), Arrays.toString(updatedItems.toArray()));
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		// Cleanup
		for(Contract c : this.contracts.getContracts()) {
			if(!c.isActive()) {
				LOGGER.debug("Object was found to not be active, removing: {}", c.toString());
				c.stopListingsScheduler();
				this.contracts.removeContract(c);
			}
		}
	}
}