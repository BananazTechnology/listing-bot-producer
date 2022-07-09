package tech.bananaz.bot.models;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
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
@Table(name = "listingEvent")
public class ListingEvent implements Comparable<ListingEvent> {
	
	// Private / Transient
	private static final DecimalFormat dFormat = new DecimalFormat("####,###,###.00");
	private static final String DF_API_URL = "http://proxy.kong.aaronrenner.com/api/rarity/deadfellaz/";
	private static final String GEISHA_API_URL = "http://proxy.kong.aaronrenner.com/api/rarity/geisha/";
	private static final String AUTO_RARITY_URL = "https://api.traitsniper.com/api/projects/%s/nfts?token_id=%s&trait_count=true&trait_norm=true";
	private static UrlUtils urlUtils   = new UrlUtils();
	private static ENSUtils ensUtils   = new ENSUtils();
	private static CryptoConvertUtils convert = new CryptoConvertUtils();
	@Transient
	private Contract contract;
	
	// Required for entity
	@Id
	private long   id;
	private String name;
	private Instant createdDate;
	private Instant startTime;
	private Instant endTime;
	private String tokenId;
	private String collectionName;
	private String collectionImageUrl;
	private String slug;
	private String imageUrl;
	private String permalink;
	private int	   quantity;
	private String sellerWalletAddy;
	private String sellerName;
	private String displayNameOutput;
	private String rarity;
	private String rarityRedirect;
	private BigDecimal listingPriceInCrypto;
	private BigDecimal listingPriceInUsd;
	private String listingAmoutOutput;
	@Enumerated( EnumType.STRING )
	private Ticker cryptoPaymentType;
	@Enumerated( EnumType.STRING )
	private RarityEngine engine;
	@Enumerated( EnumType.STRING )
	private MarketPlace market;
	// These last few items are for the consumer
	private long contractId;
	private String consumedBy;
	private boolean consumed;
	
	public ListingEvent() {}
	
	public ListingEvent(Contract contract) {
		this.contract = contract;
	}
	
	public void buildLooksRare(JSONObject looksRareEvent) throws Exception {
		// Grab sub-objects in message
		JSONObject token 	   = (JSONObject) looksRareEvent.get("token");
		JSONObject collection  = (JSONObject) looksRareEvent.get("collection");
		JSONObject order  	   = (JSONObject) looksRareEvent.get("order");
		
		// Grab direct variables
		String listingInWei    = order.getAsString("price"); 
		String collectionAddy  = collection.getAsString("address");
		this.id 			   = Long.valueOf(looksRareEvent.getAsString("id"));
		this.collectionName    = collection.getAsString("name");
		this.slug              = String.valueOf(this.collectionName.toLowerCase().replace(" ", ""));
		this.createdDate	   = Instant.parse(looksRareEvent.getAsString("createdAt"));
		this.startTime	       = Instant.ofEpochSecond(order.getAsNumber("startTime").longValue());
		this.endTime	       = Instant.ofEpochSecond(order.getAsNumber("endTime").longValue());
		this.tokenId		   = token.getAsString("tokenId");
		this.imageUrl      	   = token.getAsString("imageURI");
		this.displayNameOutput = token.getAsString("name");
		this.permalink		   = String.format("https://looksrare.org/collections/%s/%s", collectionAddy, tokenId);
		this.sellerWalletAddy  = looksRareEvent.getAsString("from");
		String ensResolve      = null;
		try {
			ensResolve 		   = ensUtils.getENS(sellerWalletAddy);
		} catch (Exception e) {
			ensResolve 		   = sellerWalletAddy;
		}
		this.sellerName	  	   = (!ensResolve.equals(sellerWalletAddy)) ? ensResolve : sellerOrWinnerOrAddress(looksRareEvent, sellerWalletAddy);
		
		// Make calculations about price
		this.listingPriceInCrypto  = convert.convertToCrypto(listingInWei, Unit.ETH);
		this.listingAmoutOutput    = String.format("%s%s", this.listingPriceInCrypto, Ticker.ETH.getSymbol());
		this.quantity 	 	   	   = Integer.valueOf(order.getAsString("amount"));
		this.market 			   = MarketPlace.LOOKSRARE;
		this.cryptoPaymentType     = Ticker.ETH;
		
		// Process final things to complete the object
		getRarity();
		this.contractId = this.contract.getId();
		this.consumed = false;
	}
	
	public void build(JSONObject openSeaEvent) {
		// Grab sub-objects in message 
		JSONObject asset 	    = (JSONObject) openSeaEvent.get("asset");
		JSONObject paymentToken = (JSONObject) openSeaEvent.get("payment_token");
		JSONObject sellerObj    = (JSONObject) openSeaEvent.get("seller");
		JSONObject sellerUser   = (JSONObject) sellerObj.get("user");
		String paymentSymbol    = null; 
		
		// Grab direct variables
		this.id 				= openSeaEvent.getAsNumber("id").longValue();
		this.createdDate		= Instant.parse(openSeaEvent.getAsString("created_date") + "Z");
		this.startTime		    = Instant.parse(openSeaEvent.getAsString("listing_time") + "Z");
		/// Try to get duration or assume OpenSea default 1 month
		long duration           = (nonNull(openSeaEvent.getAsNumber("duration"))) ? openSeaEvent.getAsNumber("duration").longValue() : 2630000;
		this.endTime		    = Instant.ofEpochSecond((this.startTime.getEpochSecond() + duration));
		
		// Get price info from body 
		int decimals = 0;
		String usdOfPayment = null;
		String listingInWei = openSeaEvent.getAsString("starting_price");
		if(nonNull(paymentToken)) {
			paymentSymbol = paymentToken.getAsString("symbol");
			usdOfPayment   	   = paymentToken.getAsString("usd_price");
			decimals           = Integer.valueOf(paymentToken.getAsString("decimals"));
		} else {
			if(this.contract.isSolana()) {
				// For the purpose of rolling out SOL quickly we need a quick fix for missing currency formatting
				usdOfPayment = "0";
				paymentSymbol = Ticker.SOL.toString();
				decimals = CryptoConvertUtils.Unit.SOL.getDecimal();
			}
		}
		
		this.quantity 	  		= openSeaEvent.getAsNumber("quantity").intValue();
		this.sellerWalletAddy 	= sellerObj.getAsString("address");
		this.sellerName	  		= sellerOrWinnerOrAddress(sellerUser, sellerWalletAddy);

		// null for single item, else for bundle
		if(nonNull(asset)) {
			parseCollectionInfo(asset);
		} else {
			JSONObject assetBundleObj = (JSONObject) openSeaEvent.get("asset_bundle");
			JSONObject assetContractObj = (JSONObject) assetBundleObj.get("asset_contract");
			
			parseCollectionInfo(assetContractObj);
			// Name & permalink override
			this.name = assetBundleObj.getAsString("name");
			this.permalink = assetBundleObj.getAsString("permalink");
		}
		
		// Setup name and rarity
		this.displayNameOutput  = getNftDisplayName(this.name, this.collectionName, this.tokenId);
		
		// Make calculations about price
		this.listingPriceInCrypto  = convert.convertToCrypto(listingInWei, decimals);
		double priceOfOnePayment   = Double.parseDouble(usdOfPayment);
		this.listingPriceInUsd 	   = this.listingPriceInCrypto.multiply(BigDecimal.valueOf(priceOfOnePayment));
		
		// Build price display for pretty output
		this.listingAmoutOutput    = null;
		// This is wrapped in try/catch for if the searched paymentSymbol has no Ticker symbol available
		try {
			Ticker symbol = Ticker.fromString(paymentSymbol);
			this.cryptoPaymentType = symbol;
			String newSymbol = symbol.getSymbol();
			this.listingAmoutOutput = String.format("%s%s %s", 
														this.listingPriceInCrypto, 
														newSymbol, 
														(this.listingPriceInUsd.doubleValue() > 0.0) ? "($" + dFormat.format(this.listingPriceInUsd.doubleValue()) + ")" : "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// When no Ticker is available this will build a default
		if(isNull(this.listingAmoutOutput)) this.listingAmoutOutput = String.format("%s %s %s", this.listingPriceInCrypto, paymentSymbol, (this.listingPriceInUsd.doubleValue() > 0.0) ? "($" + dFormat.format(this.listingPriceInUsd.doubleValue()) + ")" : "");
		if(decimals == 0 && isNull(usdOfPayment)) this.listingAmoutOutput = "ERROR";
		this.market = MarketPlace.OPENSEA;
		
		// Process final things to complete the object
		getRarity();
		this.contractId = this.contract.getId();
		this.consumed = false;
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
			this.rarity = response.getAsString("rarity");
			
			// Formats a URL for Discord
			this.engine = RarityEngine.RARITY_TOOLS;
			String raritySlugOverride = (nonNull(this.contract.getRaritySlug())) ? this.contract.getRaritySlug() : this.slug;
			this.rarityRedirect = String.format(this.contract.getEngine().getUrl(), raritySlugOverride, this.tokenId);
		} catch (Exception e) {}
	}
	
	private void getGeishaRarity(String tokenId) {
		try {
			JSONObject response = urlUtils.getObjectRequest(GEISHA_API_URL + tokenId, null);
			this.rarity = response.getAsString("rarity");
			
			// Formats a URL for Discord
			this.engine = RarityEngine.RARITY_TOOLS;
			String raritySlugOverride = (nonNull(this.contract.getRaritySlug())) ? this.contract.getRaritySlug() : this.slug;
			this.rarityRedirect = String.format(this.contract.getEngine().getUrl(), raritySlugOverride, this.tokenId);
		} catch (Exception e) {}
	}
	
	private void getAutoRarity() {
		try {
			String buildUrl = String.format(AUTO_RARITY_URL, this.contract.getContractAddress(), this.tokenId);
			// Add User-Agent for legality
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.USER_AGENT, "PostmanRuntime/7.29.0");
			HttpEntity<String> prop = new HttpEntity<>(headers);
			JSONObject response = urlUtils.getObjectRequest(buildUrl, prop);
			JSONArray nfts = (JSONArray) response.get("nfts");
			JSONObject firstItem = (JSONObject) nfts.get(0);
			this.rarity = firstItem.getAsString("rarity_rank");
			
			// Formats a URL for Discord
			this.engine = RarityEngine.TRAIT_SNIPER;
			this.rarityRedirect = String.format(this.contract.getEngine().getUrl(), this.contract.getContractAddress(), this.tokenId);
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
			if(isNull(this.engine)) this.engine = this.contract.getEngine();
		}
		return this.rarity;
	}
	
	public String getImageUrl() {
		String response = null;
		if(nonNull(this.imageUrl)) {
			if(!this.imageUrl.contains(".svg") && !this.imageUrl.isBlank()) {
				response = this.imageUrl;
			}
		}
		response = (nonNull(response)) ? response : this.collectionImageUrl;
		return response;
	}
	
	@Override
	public int compareTo(ListingEvent that) {
		Instant thisCreatedDate = this.createdDate;
		Instant thatCreatedDate = that.getCreatedDate();
		return thatCreatedDate.compareTo(thisCreatedDate); 
	}
	
	public String getHash() {
		return String.format("%s:%s", this.sellerWalletAddy, this.listingPriceInCrypto.toString());
	}
	
	private void parseCollectionInfo(JSONObject asset) {
		this.name 		  		= asset.getAsString("name");
		this.tokenId 		  	= (asset.getAsString("token_id") != null) ? asset.getAsString("token_id") : "0";
		this.imageUrl 	  		= (asset.getAsString("image_preview_url") != null) ? asset.getAsString("image_preview_url") : "";
		this.permalink 	  		= asset.getAsString("permalink");

		JSONObject collection   = (JSONObject) asset.get("collection");
		this.collectionName 	= collection.getAsString("name");
		this.collectionImageUrl = collection.getAsString("image_url");
		this.slug			    = collection.getAsString("slug");
	}

}
