package tech.bananaz.bot.services;

import java.util.*;
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
import static java.util.Objects.nonNull;
import static tech.bananaz.utils.StringUtils.nonEquals;

@Component
public class UpdateScheduler extends TimerTask {
	
	@Autowired
	private ListingConfigPagingRepository configs;
	
	@Autowired
	private ContractCollection contracts;
	
	@Autowired
	private EventPagingRepository events;
	
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
			LOGGER.info(String.format("Starting new ListingUpdateScheduler"));
			// Starts this new timer, starts at random time and runs per <interval> milliseconds
			this.timer.schedule(task, 1, REFRESH_REQ);
		}
		return active;
	}
	
	public boolean stop() {
		this.active = false;
		LOGGER.info("Stopping ListingUpdateScheduler");
		return active;
	}

	@Override
	public void run() {
		if(nonNull(this.contracts) && active) {
			Iterable<Listing> allListingConfigs = this.configs.findAll();
			for(Listing conf : allListingConfigs) {
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
						Contract watcher = new ContractBuilder().configProperties(conf, this.configs, this.events);
						// Start the watcher
						watcher.startListingsScheduler();
						// Add this to internal memory buffer
						this.contracts.addContract(watcher);
						updatedItems.add(String.format("new: %s", watcher));
					}
					if(updatedItems.size() > 0) {
						if(nonNull(cont)) cont.setConfig(conf);
						LOGGER.debug("Contract {} updated {}", conf.getId(), Arrays.toString(updatedItems.toArray()));
					}
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