package tech.bananaz.bot.utils;

import net.minidev.json.JSONObject;

public class KeyUtils {

	private final String APIKEY = "a2V5OmJ0";
	private final String KEY_URL = "http://proxy.kong.aaronrenner.com/api/keys?apikey="+APIKEY;
	private UrlUtils uUtils = new UrlUtils();
	
	public KeyUtils() {}
	
	public String getKey() throws Exception {
		JSONObject keyResponse = this.uUtils.getObjectRequest(KEY_URL, null);
		return keyResponse.getAsString("key");
	}
	
}
