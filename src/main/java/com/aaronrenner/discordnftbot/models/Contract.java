package com.aaronrenner.discordnftbot.models;

import com.aaronrenner.discordnftbot.repositories.ListingConfigRepository;
import com.aaronrenner.discordnftbot.repositories.ListingEventRepository;
import com.aaronrenner.discordnftbot.services.ListingsScheduler;
import com.aaronrenner.discordnftbot.utils.RarityEngine;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import lombok.ToString.Exclude;

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
	boolean excludeOpensea 			  = false;
	// Support for slug based API requests in OpenSea
	private boolean isSlug 			  = false;
	// Is Solana on OpenSea
	private boolean isSolana 		  = false;
	// For bundles support
	boolean showBundles 			  = true;

	// Discord Settings
	// If enabled, will auto pull from LooksRare for all
	private boolean autoRarity 		  = false;
	// Proves the URLs for formatting Discord
	private RarityEngine engine;
	// For when the slug in URL is not the same as Contract slug
	private String raritySlug;
	
	// LooksRare settings
	boolean excludeLooks 			  = false;
	
	// For API Manager
	@SuppressWarnings("unused")
	private long lastOpenseaId;
	@SuppressWarnings("unused")
	private long lastLooksrareId;

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
}
