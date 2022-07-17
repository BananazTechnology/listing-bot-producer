package tech.bananaz.bot.models;

import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import lombok.Data;
import lombok.ToString;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import tech.bananaz.bot.utils.CryptoConvertUtils;
import tech.bananaz.bot.utils.ENSUtils;
import tech.bananaz.bot.utils.EventType;
import tech.bananaz.bot.utils.MarketPlace;
import tech.bananaz.bot.utils.RarityEngine;
import tech.bananaz.bot.utils.Ticker;
import tech.bananaz.bot.utils.UrlUtils;
import tech.bananaz.bot.utils.CryptoConvertUtils.Unit;
import static java.util.Objects.nonNull;
import static java.util.Objects.isNull;

@ToString(includeFieldNames=true)
@Data
@Entity
@Table(name = "event")
public class ListingEvent implements Comparable<ListingEvent> {
	
	// Private / Transient
	private static final String DF_API_URL      = "http://proxy.aaronrenner.com/api/rarity/deadfellaz/";
	private static final String GEISHA_API_URL  = "http://proxy.aaronrenner.com/api/rarity/geisha/";
	private static final String AUTO_RARITY_URL = "https://api.traitsniper.com/api/projects/%s/nfts?token_id=%s&trait_count=true&trait_norm=true";
	private static final String ETHERSCAN_URL   = "https://etherscan.io/address/";
	private static final String SOLSCAN_URL     = "https://solscan.io/address/";
	private static UrlUtils urlUtils   			= new UrlUtils();
	private static ENSUtils ensUtils   		    = new ENSUtils();
	private static CryptoConvertUtils convert   = new CryptoConvertUtils();
	@Transient
	private Contract contract;
	
	// Required for entity
	@Id
	private long         id;
	@Column(columnDefinition = "VARCHAR(75)")
	private String       name;
	private Instant      createdDate;
	private Instant      startTime;
	private Instant      endTime;
	@Column(columnDefinition = "VARCHAR(50)")
	private String       tokenId;
	@Column(columnDefinition = "VARCHAR(75)")
	private String       collectionName;
	private String       collectionImageUrl;
	@Column(columnDefinition = "VARCHAR(50)")
	private String       slug;
	private String       imageUrl;
	@Column(columnDefinition = "VARCHAR(127)")
	private String       permalink;
	private int	         quantity;
	@Column(columnDefinition = "VARCHAR(50)")
	private String       sellerWalletAddy;
	@Column(columnDefinition = "VARCHAR(50)")
	private String       sellerName;
	@Column(columnDefinition = "VARCHAR(127)")
	private String       sellerUrl;
	@Column(columnDefinition = "VARCHAR(6)")
	private String       rarity;
	@Enumerated( EnumType.STRING )
	@Column(columnDefinition = "VARCHAR(50)")
	private RarityEngine rarityEngine;
	@Column(columnDefinition = "VARCHAR(50)")
	private String       rarityUrl;
	@Column(columnDefinition = "VARCHAR(25)")
	private BigDecimal   priceInCrypto;
	@Column(columnDefinition = "VARCHAR(25)")
	private BigDecimal   priceInUsd;
	@Enumerated( EnumType.STRING )
	@Column(columnDefinition = "VARCHAR(6)")
	private Ticker       cryptoType;
	@Enumerated( EnumType.STRING )
	@Column(columnDefinition = "VARCHAR(7)")
	private EventType    eventType;
	@Enumerated( EnumType.STRING )
	@Column(columnDefinition = "VARCHAR(50)")
	private MarketPlace  market;
	// These last few items are for the consumer
	private long         configId;
	@Column(columnDefinition = "VARCHAR(50)")
	private String       consumedBy;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 0")
	private boolean      consumed;
	
	public ListingEvent() {}
	
	public ListingEvent(Contract contract) {
		this.contract = contract;
	}
	
	public void buildLooksRare(JSONObject looksRareEvent) throws Exception {
		// Grab sub-objects in message
		JSONObject token 	   = (JSONObject) looksRareEvent.get("token");
		JSONObject collection  = (JSONObject) looksRareEvent.get("collection");
		JSONObject order  	   = (JSONObject) looksRareEvent.get("order");
		String listingInWei    = order.getAsString("price");
		
		// Grab direct variables
		this.id 			   = Long.valueOf(looksRareEvent.getAsString("id"));
		this.name			   = token.getAsString("name");
		this.tokenId		   = token.getAsString("tokenId");
		this.collectionName    = collection.getAsString("name");
		this.slug              = String.valueOf(this.collectionName.toLowerCase().replace(" ", ""));
		this.createdDate	   = Instant.parse(looksRareEvent.getAsString("createdAt"));
		this.startTime	       = Instant.ofEpochSecond(order.getAsNumber("startTime").longValue());
		this.endTime	       = Instant.ofEpochSecond(order.getAsNumber("endTime").longValue());
		this.imageUrl      	   = token.getAsString("imageURI");
		this.permalink		   = String.format("https://looksrare.org/collections/%s/%s", this.contract.getContractAddress(), tokenId);
		this.sellerWalletAddy  = looksRareEvent.getAsString("from");
		String ensResolve      = null;
		try {
			ensResolve 		   = ensUtils.getENS(sellerWalletAddy);
		} catch (Exception e) {
			ensResolve 		   = sellerWalletAddy;
		}
		this.sellerName	  	   = (!ensResolve.equals(sellerWalletAddy)) ? ensResolve : sellerOrWinnerOrAddress(looksRareEvent, sellerWalletAddy);
		this.sellerUrl		   = String.format("%s%s", ETHERSCAN_URL, this.sellerWalletAddy);
		
		// Make calculations about price
		this.priceInCrypto     = convert.convertToCrypto(listingInWei, Unit.ETH);
		this.quantity 	 	   = Integer.valueOf(order.getAsString("amount"));
		
		// Process final things to complete the object
		this.market 		   = MarketPlace.LOOKSRARE;
		this.cryptoType  	   = Ticker.ETH;
		this.eventType		   = EventType.LISTING;
		this.configId 	       = this.contract.getId();
		getImageUrl();
		getRarity();
	}
	
	public void build(JSONObject openSeaEvent) {
		// Grab sub-objects in message 
		JSONObject asset 	    = (JSONObject) openSeaEvent.get("asset");
		JSONObject paymentToken = (JSONObject) openSeaEvent.get("payment_token");
		JSONObject sellerObj    = (JSONObject) openSeaEvent.get("seller");
		JSONObject sellerUser   = (JSONObject) sellerObj.get("user");
		
		// Grab direct variables
		this.id 				= openSeaEvent.getAsNumber("id").longValue();
		this.createdDate		= Instant.parse(openSeaEvent.getAsString("created_date") + "Z");
		this.startTime		    = Instant.parse(openSeaEvent.getAsString("listing_time") + "Z");
		/// Try to get duration or assume OpenSea default 1 month
		long duration           = (nonNull(openSeaEvent.getAsNumber("duration"))) ? openSeaEvent.getAsNumber("duration").longValue() : 2630000;
		this.endTime		    = Instant.ofEpochSecond((this.startTime.getEpochSecond() + duration));
		this.quantity 	  		= openSeaEvent.getAsNumber("quantity").intValue();
		this.sellerWalletAddy 	= sellerObj.getAsString("address");
		this.sellerName	  		= sellerOrWinnerOrAddress(sellerUser, sellerWalletAddy);
		
		// Get price info from body 
		int decimals = 0;
		String usdOfPayment = null;
		String listingInWei = openSeaEvent.getAsString("starting_price");
		
		// Process ETH on OpenSea
		if(nonNull(paymentToken)) {
			this.cryptoType 	   = Ticker.fromString(paymentToken.getAsString("symbol"));
			usdOfPayment   	       = paymentToken.getAsString("usd_price");
			decimals               = Integer.valueOf(paymentToken.getAsString("decimals"));
			this.sellerUrl	       = String.format("%s%s", ETHERSCAN_URL, this.sellerWalletAddy);
		} else {
			// Process SOL on OpenSea
			if(this.contract.isSolana()) {
				this.cryptoType    = Ticker.SOL;
				decimals 	   	   = CryptoConvertUtils.Unit.SOL.getDecimal();
				this.sellerUrl 	   = String.format("%s%s", SOLSCAN_URL, this.sellerWalletAddy);
			}
		}

		// null for single item, else for bundle
		if(nonNull(asset)) {
			parseCollectionInfo(asset);
		} else {
			JSONObject assetBundleObj   = (JSONObject) openSeaEvent.get("asset_bundle");
			JSONObject assetContractObj = (JSONObject) assetBundleObj.get("asset_contract");
			
			parseCollectionInfo(assetContractObj);
			// Name & permalink override
			this.name      = assetBundleObj.getAsString("name");
			 this.permalink = assetBundleObj.getAsString("permalink");
		}
		
		// Name formatting for when null, must be processed after if/else above
		this.name = getNftDisplayName(this.name, this.collectionName, this.tokenId);
		
		// Make calculations about price
		this.priceInCrypto  	 = convert.convertToCrypto(listingInWei, decimals);
		if(nonNull(usdOfPayment)) {
			double priceOfOnePayment = Double.parseDouble(usdOfPayment);
			this.priceInUsd 	   	 = this.priceInCrypto.multiply(BigDecimal.valueOf(priceOfOnePayment));
		}
		
		// Process final things to complete the object
		this.market    = MarketPlace.OPENSEA;
		this.configId  = this.contract.getId();
		this.eventType = EventType.LISTING;
		getImageUrl();
		getRarity();
	}
	
	private String getNftDisplayName(String nftName, String collectionName, String tokenId) throws NullPointerException {
		return (nonNull(nftName)) ? nftName : collectionName + " #" + tokenId;
	}
	
	private String sellerOrWinnerOrAddress(JSONObject data, String address) {
		String response = "N/A";
		try {
			response = data.getAsString("username");
			if(isNull(response)) {
				response = address.substring(0, 8);
			}
		} catch (Exception e) {
			 response = address.substring(0, 8);
		}
		return response;
	}
	
	private void getDeadfellazRarity(String tokenId) {
		try {
			JSONObject response = urlUtils.getObjectRequest(DF_API_URL + tokenId, null);
			this.rarity 		= response.getAsString("rarity");
			
			// Formats a URL for Discord
			this.rarityEngine 		  = RarityEngine.RARITY_TOOLS;
			String raritySlugOverride = (nonNull(this.contract.getRaritySlug())) ? this.contract.getRaritySlug() : this.slug;
			this.rarityUrl		      = String.format(RarityEngine.RARITY_TOOLS.getUrl(), raritySlugOverride, this.tokenId);
		} catch (Exception e) {}
	}
	
	private void getGeishaRarity(String tokenId) {
		try {
			JSONObject response = urlUtils.getObjectRequest(GEISHA_API_URL + tokenId, null);
			this.rarity 		= response.getAsString("rarity");
			
			// Formats a URL for Discord
			this.rarityEngine 		  = RarityEngine.RARITY_TOOLS;
			String raritySlugOverride = (nonNull(this.contract.getRaritySlug())) ? this.contract.getRaritySlug() : this.slug;
			this.rarityUrl 	  		  = String.format(RarityEngine.RARITY_TOOLS.getUrl(), raritySlugOverride, this.tokenId);
		} catch (Exception e) {}
	}
	
	private void getAutoRarity() {
		try {
			String buildUrl 		= String.format(AUTO_RARITY_URL, this.contract.getContractAddress(), this.tokenId);
			// Add User-Agent for legality
			HttpHeaders headers 	= new HttpHeaders();
			headers.add(HttpHeaders.USER_AGENT, "PostmanRuntime/7.29.0");
			HttpEntity<String> prop = new HttpEntity<>(headers);
			JSONObject response 	= urlUtils.getObjectRequest(buildUrl, prop);
			JSONArray nfts 			= (JSONArray) response.get("nfts");
			JSONObject firstItem 	= (JSONObject) nfts.get(0);
			this.rarity 			= firstItem.getAsString("rarity_rank");
			
			// Formats a URL for Discord
			this.rarityEngine 		= RarityEngine.TRAIT_SNIPER;
			this.rarityUrl		    = String.format(RarityEngine.TRAIT_SNIPER.getUrl(), this.contract.getContractAddress(), this.tokenId);
		} catch (Exception e) {}
	}
	
	public String getRarity() {
		// Ensure all variables are set to run rarity checks INCLUDING a null rarity so that we don't make extra HTTP requests
		if(nonNull(this.slug) && nonNull(this.tokenId) && isNull(this.rarity)) {
			// Deadfellaz
			if(this.slug.equalsIgnoreCase("deadfellaz"))  getDeadfellazRarity(this.tokenId);
			// Super Geisha
			if(this.slug.equalsIgnoreCase("supergeisha")) getGeishaRarity(this.tokenId);
			// If auto rarity and rairty has not just been set above
			if(this.contract.isAutoRarity() && isNull(this.rarity)) {
				getAutoRarity();
			}
		}
		return this.rarity;
	}
	
	public String getImageUrl() {
		if(isNull(this.imageUrl)) this.imageUrl = this.collectionImageUrl;
		if(nonNull(this.imageUrl)) {
			if(this.imageUrl.contains(".svg") || this.imageUrl.isBlank() || this.imageUrl.isEmpty()) {
				this.imageUrl = this.collectionImageUrl;
			}
		}
		return this.imageUrl;
	}
	
	@Override
	public int compareTo(ListingEvent that) {
		Instant thisCreatedDate = this.createdDate;
		Instant thatCreatedDate = that.getCreatedDate();
		return thatCreatedDate.compareTo(thisCreatedDate); 
	}
	
	public String getHash() {
		return String.format("%s:%s", this.sellerWalletAddy, this.priceInCrypto.toPlainString());
	}
	
	private void parseCollectionInfo(JSONObject asset) {
		this.name 		  		= asset.getAsString("name");
		this.tokenId 		  	= (nonNull(asset.getAsString("token_id"))) ? asset.getAsString("token_id") : "0";
		this.imageUrl 	  		= (nonNull(asset.getAsString("image_preview_url"))) ? asset.getAsString("image_preview_url") : "";
		this.permalink 	  		= asset.getAsString("permalink");

		JSONObject collection   = (JSONObject) asset.get("collection");
		this.collectionName 	= collection.getAsString("name");
		this.collectionImageUrl = collection.getAsString("image_url");
		this.slug			    = collection.getAsString("slug");
	}

}
