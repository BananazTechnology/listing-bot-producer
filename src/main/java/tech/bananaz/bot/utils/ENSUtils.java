package tech.bananaz.bot.utils;

import java.net.URI;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class ENSUtils {
	
	// Base URL is the query endpoint for the subgraph we need to query
	private static String BASEURL      = "https://api.thegraph.com/subgraphs/name/ensdomains/ens";
	// These next few variables are the raw queries, they're kinda in JSON format but without colons
	// The JSON strings entered here must be string escaped
	// For variables, we use %s to represent location in the string
	private static String GETDOMAINS   = "{ accounts(where:{ id: \"%s\"}) { registrations(orderDirection: asc, orderBy: registrationDate, where: { expiryDate_gt: %s }) { domain { name }}}}";
	private RestTemplate restTemplate  = new RestTemplate();
	private StringUtils sUtils         = new StringUtils();
	private JsonUtils jsonUtils 	   = new JsonUtils();
	private static final Logger LOGGER = LoggerFactory.getLogger(ENSUtils.class);
	
	/**
	 * Gets the events for a specific contract
	 * @param contract
	 * @return
	 * @throws HttpException 
	 */
	public String getENS(String address) {
		String ensResponse = address;
		try {
			String requestQuery = String.format(GETDOMAINS, address.toLowerCase(), Instant.now().toEpochMilli()/1000);
			JSONObject response = getAllRequest(requestQuery);
			JSONObject data     = (JSONObject) response.get("data");
			JSONArray accounts  = (JSONArray) data.get("accounts");
			if(accounts.size() > 0) {
				JSONObject selectAccounts 	  = (JSONObject) accounts.get(0);
				JSONArray registrationObj 	  = (JSONArray) selectAccounts.get("registrations");
				JSONObject selectRegistration = (JSONObject) registrationObj.get(0);
				JSONObject domainObj          = (JSONObject) selectRegistration.get("domain");
				ensResponse = domainObj.getAsString("name");
			}
		} catch (Exception e) {}
		return ensResponse;
	}
	
	/**
	 * This is a helper method, in this method you can provide a String of the URL for 
	 * requesting and it will return a JSONObject of the response from Steam.
	 * 
	 * @param getURL Pass in a String of the API request URL.
	 * @return A json-smart object of the
	 * @throws HttpException 
	 * @throws InterruptedException
	 */
	private JSONObject getAllRequest(String queryString) throws Exception {
		// Variables for runtime
		URI createURI = sUtils.getURIFromString(BASEURL);
		ResponseEntity<String> result = null;
		JSONObject newResponse = new JSONObject();
		// Build Request Body
		JSONObject requestBody = new JSONObject();
		requestBody.put("query", queryString);
		// Buld Request Headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> wrappedRequest = new HttpEntity<String>(requestBody.toJSONString(), headers);
		// Variables for timing
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis();
		try {
			// Create HTTP Call
			result = restTemplate.exchange(createURI, HttpMethod.POST, wrappedRequest, String.class);
			// Parse String response for JSONObject
			newResponse = jsonUtils.stringToJsonObject(result.getBody());
			endTime = System.currentTimeMillis();
		} catch (HttpClientErrorException e) {
			LOGGER.error(String.format("Failed HTTP GET: [%s] %s - %s", e.getRawStatusCode(), e.getStatusText(), e.getResponseHeaders().toSingleValueMap()));
			throw new Exception(String.format("Failed HTTP GET: [%s] %s - %s", e.getRawStatusCode(), e.getStatusText(), e.getResponseHeaders().toSingleValueMap()));
		}
		LOGGER.debug(String.format("GET request took %sms", Long.valueOf(endTime-startTime).toString()));
		return newResponse;
	}

}
