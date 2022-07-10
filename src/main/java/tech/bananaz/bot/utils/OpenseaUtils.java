package tech.bananaz.bot.utils;

import java.net.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.*;

import net.minidev.json.*;
import tech.bananaz.bot.models.Contract;

public class OpenseaUtils {
	
	private static final String OS_BASE					   = "https://api.opensea.io/api/v1/";
	private static final String OPENSEAEVENTSURL   		   = OS_BASE+"events?event_type=created&only_opensea=false&limit=25&asset_contract_address=%s";
	private static final String OPENSEAASSETURL 		   = OS_BASE+"asset/%s/%s/";
	private static final String OPENSEACOLLECTIONURL 	   = OS_BASE+"collections?limit=300&asset_owner=%s";
	private static final String OPENSEACOLLECTIONFORSTATS  = OS_BASE+"collection/%s/stats";
	private static final String OPENSEAASSETCONTRACTURL    = OS_BASE+"asset_contract/%s";
	private static final String OPENSEASTATUS			   = "https://status.opensea.io/api/v2/status.json";
	private static final String OPENSEASLUGURL       	   = OS_BASE+"events?event_type=created&only_opensea=false&limit=25&collection_slug=%s";
	private static final String APIKEYHEAD 		   		   = "x-api-key";
	private RestTemplate restTemplate 			   		   = new RestTemplate();
	private StringUtils sUtils  				   		   = new StringUtils();
	private JsonUtils jsonUtils 						   = new JsonUtils();
	private String apiKey;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenseaUtils.class);
	
	public OpenseaUtils(String apiKey) {
		if(!apiKey.equals("")) {
			this.apiKey = apiKey;
		} else {
			throw new RuntimeException("Opensea API key needed for library calls!");
		}
	}
	
	/**
	 * Gets the events for a uptime stats
	 * @param contract
	 * @return
	 */
	public JSONObject getStatus() throws Exception {
		return getAllRequest(OPENSEASTATUS, null);
	}
	
	/**
	 * Gets the events for a specific contract
	 * @param contract
	 * @return
	 */
	public JSONObject getCollectionListedWithSlug(String slug) throws Exception {
		String buildUrl = String.format(OPENSEASLUGURL, slug);
		
		// Create header for validating
		HttpHeaders headers = new HttpHeaders();
		headers.set(APIKEYHEAD, this.apiKey);
		// Wrap headers in request body
		HttpEntity<String> wrappedRequest = new HttpEntity<>(headers);
		
		return getAllRequest(buildUrl, wrappedRequest);
		
	}
	
	/**
	 * Gets the events for a specific contract
	 * @param contract
	 * @return
	 */
	public JSONObject getCollectionListed(String contractAddress) throws Exception {
		String buildUrl = String.format(OPENSEAEVENTSURL, contractAddress);
		
		// Create header for validating
		HttpHeaders headers = new HttpHeaders();
		headers.set(APIKEYHEAD, this.apiKey);
		// Wrap headers in request body
		HttpEntity<String> wrappedRequest = new HttpEntity<>(headers);
		
		return getAllRequest(buildUrl, wrappedRequest);
		
	}
	
	/**
	 * Gets the events for a specific contract
	 * @param contract
	 * @return
	 */
	public JSONObject getSingleAsset(String contractAddress, String tokenId) throws Exception {
		String buildUrl = String.format(OPENSEAASSETURL, contractAddress, tokenId);
		
		// Create header for validating
		HttpHeaders headers = new HttpHeaders();
		headers.set(APIKEYHEAD, this.apiKey);
		// Wrap headers in request body
		HttpEntity<String> wrappedRequest = new HttpEntity<>(headers);
				
		return getAllRequest(buildUrl, wrappedRequest);
	}
	
	/**
	 * Gets the events for a specific contract
	 * @param contract
	 * @return
	 */
	public JSONArray getCollectionsOwnedByAddress(String ownerWalletAddress) throws Exception {
		String buildUrl = String.format(OPENSEACOLLECTIONURL, ownerWalletAddress);
		return getAllRequestArray(buildUrl);
	}
	
	/**
	 * Gets the events for a specific contract
	 * @param contract
	 * @return
	 */
	public JSONObject getCollectionStatsBySlug(String slug) throws Exception {
		String buildUrl = String.format(OPENSEACOLLECTIONFORSTATS, slug);
		
		// Create header for validating
		HttpHeaders headers = new HttpHeaders();
		headers.set(APIKEYHEAD, this.apiKey);
		// Wrap headers in request body
		HttpEntity<String> wrappedRequest = new HttpEntity<>(headers);
		
		return getAllRequest(buildUrl, wrappedRequest);
	}
	
	/**
	 * Extra helper method
	 * Gets the events for a specific contract
	 * @param contract
	 * @return
	 */
	public JSONObject getCollectionStatsByContractAddress(String contractAddress) throws Exception {
		JSONObject contractItem  = this.getSingleCollection(contractAddress);
		/** Get Objects */
		JSONObject getCollection = (JSONObject) contractItem.get("collection");
		/** Get slug */
		String slug 			 = getCollection.getAsString("slug");
		
		return getCollectionStatsBySlug(slug);
	}
	
	/**
	 * Gets the events for a specific contract
	 * @param contract
	 * @return
	 */
	public JSONObject getSingleCollection(String contractAddress) throws Exception {
		String buildUrl = String.format(OPENSEAASSETCONTRACTURL, contractAddress);
		
		// Create header for validating
		HttpHeaders headers = new HttpHeaders();
		headers.set(APIKEYHEAD, this.apiKey);
		// Wrap headers in request body
		HttpEntity<String> wrappedRequest = new HttpEntity<>(headers);
		
		return getAllRequest(buildUrl, wrappedRequest);
	}
	
	/**
	 * Calls getSingleAsset to obtain the JSONObject data for an NFT, 
	 * this helper method allows all the processing to occur here and return a Java Map :)!
	 * @param contract
	 * @param tokenId
	 * @return Sorted metadata K/V pair in map!
	 * @throws HttpException 
	 */
	public Map<String, String> tokenMetadata(Contract contract, String tokenId) throws NullPointerException, Exception {
		JSONObject metadata  		  = getSingleAsset(contract.getContractAddress(), tokenId);
		JSONArray metadataTraits	  = (JSONArray) metadata.get("traits");
		/** Using TreeMap here allows for automatic alphabetical sort, nice for UI */
		Map<String, String> metadataArray = new TreeMap<>();
		for(int t = 0; t < metadataTraits.size(); t++) {
			JSONObject metadataTrait = (JSONObject) metadataTraits.get(t);
			String traitName 		 = metadataTrait.getAsString("trait_type");
			String traitValue 		 = metadataTrait.getAsString("value");
			metadataArray.put(traitName, traitValue);
		}
		return metadataArray;
	}
	
	/**
	 * This is a helper method, in this method you can provide a String of the URL for 
	 * requesting and it will return a JSONObject of the response from OpenSea.
	 * 
	 * @param getURL Pass in a String of the API request URL.
	 * @return A json-smart object of the
	 * @throws HttpException 
	 * @throws InterruptedException
	 */
	private JSONObject getAllRequest(String getURL, HttpEntity<String> requestMetadata) throws Exception {
		// Variables for runtime
		URI createURI = sUtils.getURIFromString(getURL);
		ResponseEntity<String> result = null;
		JSONObject newResponse = new JSONObject();
		// Variables for timing
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		try {
			// Runs for events endpoint, this to to append api key
			if(requestMetadata != null) {
				// Create HTTP Call
				result = restTemplate.exchange(createURI, HttpMethod.GET, requestMetadata, String.class);
				// Parse String response for JSONObject
				newResponse = jsonUtils.stringToJsonObject(result.getBody());
				endTime = System.currentTimeMillis();
			} else {
				result = restTemplate.getForEntity(createURI, String.class);
				newResponse = jsonUtils.stringToJsonObject(result.getBody());
			}
		} catch (HttpClientErrorException e) {
			LOGGER.error(String.format("Failed HTTP GET: [%s] %s - %s", e.getRawStatusCode(), e.getStatusText(), e.getResponseHeaders().toSingleValueMap()));
			throw new Exception(String.format("Failed HTTP GET: [%s] %s - %s", e.getRawStatusCode(), e.getStatusText(), e.getResponseHeaders().toSingleValueMap()));
		}
		LOGGER.debug(String.format("GET request took %sms", Long.valueOf(endTime-startTime).toString()));
		return newResponse;
	}
	
	/**
	 * This is a helper method, in this method you can provide a String of the URL for 
	 * requesting and it will return a JSONArray of the response from OpenSea.
	 * 
	 * @param getURL Pass in a String of the API request URL.
	 * @return A json-smart object of the
	 * @throws HttpException
	 */
	private JSONArray getAllRequestArray(String getURL) throws Exception {
		// Variables for runtime
		URI createURI = sUtils.getURIFromString(getURL);
		ResponseEntity<String> result = null;
		JSONArray newResponse = new JSONArray();
		// Variables for timing
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		try {
			// Create header for validating
			HttpHeaders headers = new HttpHeaders();
			headers.set(APIKEYHEAD, this.apiKey);
			// Wrap headers in request body
			HttpEntity<String> wrappedRequest = new HttpEntity<>(headers);
			// Create HTTP Call
			result = restTemplate.exchange(createURI, HttpMethod.GET, wrappedRequest, String.class);
			// Parse String response for JSONObject
			newResponse = jsonUtils.stringToJsonArray(result.getBody());
			endTime = System.currentTimeMillis();
		// Catch when HTTP fails
		} catch (HttpClientErrorException e) {
			LOGGER.error(String.format("Failed HTTP GET: [%s] %s - %s", e.getRawStatusCode(), e.getStatusText(), e.getResponseHeaders().toSingleValueMap()));
			throw new Exception(String.format("Failed HTTP GET: [%s] %s - %s", e.getRawStatusCode(), e.getStatusText(), e.getResponseHeaders().toSingleValueMap()));
		}
		LOGGER.debug(String.format("GET request took %sms", Long.valueOf(endTime-startTime).toString()));
		return newResponse;
	}
}
