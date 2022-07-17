package tech.bananaz.bot.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import lombok.ToString.Exclude;
import tech.bananaz.bot.repositories.ListingConfigRepository;
import tech.bananaz.bot.repositories.ListingEventRepository;
import tech.bananaz.bot.services.ListingsScheduler;

@ToString(includeFieldNames=true)
@Data
public class Contract {
	
	@Exclude
	@JsonIgnore
	private ListingsScheduler newRequest;
	
	@Exclude
	@JsonIgnore
	private ListingConfigRepository configs;
	
	@Exclude
	@JsonIgnore
	private ListingEventRepository events;

	// Pairs from DB definition
	private long id;
	private String contractAddress;
	private int interval;
	private boolean active 			  = true;

	// OpenSea settings
	private boolean excludeOpensea 	  = false;
	// Support for slug based API requests in OpenSea
	private boolean isSlug 			  = false;
	// Is Solana on OpenSea
	private boolean isSolana 		  = false;
	// For bundles support
	private boolean showBundles 	  = true;

	// Discord Settings
	// If enabled, will auto pull from LooksRare for all
	private boolean autoRarity 		  = false;
	// For when the slug in URL is not the same as Contract slug
	private String raritySlug;
	
	// LooksRare settings
	private boolean excludeLooks 	  = false;

	public void startListingsScheduler() {
		newRequest = new ListingsScheduler(this);
		newRequest.start();
	}
	
	public void stopListingsScheduler() {
		newRequest.stop();
	}
	
	public long getLastOpenseaId() {
		return this.newRequest.getOpenSeaIdBuffer();
	}
	
	public long getLastLooksrareId() {
		return this.newRequest.getPreviousLooksId();
	}
	
	public String getLastOpenseaHash() {
		return this.newRequest.getOpenSeaLastHash();
	}
}
