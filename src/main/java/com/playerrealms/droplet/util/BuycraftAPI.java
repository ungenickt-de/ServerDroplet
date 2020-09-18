package com.playerrealms.droplet.util;

import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.fail;

public class BuycraftAPI {

	public static final String PACKAGE = "package";
	
	public static final String EXPIRE_LIMIT = "limit";
	
	public static final int ULTRA_PREMIUM_PACKAGE = 0;
	public static final int MVP_PACKAGE = 0;
	public static final int VIP_PACKAGE = 0;
	
	public static final String CREATE_URL = "https://plugin.buycraft.net/coupons";
	
	private static final String API_KEY = "";
	
	@SuppressWarnings("unchecked")
	public static boolean createCoupon(String code, String effectiveOn, int[] packages, int discountPercent, String expireType, int limit, String startDate, String username, String note) throws IOException {
		
		URL url = new URL(CREATE_URL);
		
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("X-Buycraft-Secret", API_KEY);
		
		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("effective_on", effectiveOn);
		JSONArray array = new JSONArray();
		array.add(packages[0]);
		json.put("packages", array);
		json.put("discount_type", "percentage");
		json.put("discount_amount", 0);
		json.put("discount_percentage", discountPercent);
		json.put("expire_type", expireType);
		json.put("expire_limit", limit);
		json.put("expire_never", true);
		json.put("basket_type", "both");
		json.put("minimum", 0);
		json.put("note", note);
		json.put("start_date", startDate);
		json.put("redeem_limit", limit);
		json.put("username", username);
		
		byte[] data = json.toString().getBytes();
		
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Content-Length", String.valueOf(data.length));
		con.setRequestProperty("User-Agent", "PlayerRealms");
		
		con.getOutputStream().write(data);
		
		int response = con.getResponseCode();
		
		if(response != 200) {
			
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()))) {
				String line;
				
				while((line = reader.readLine()) != null) {
					System.err.println(line);
				}
			}finally {
				fail("Failed response "+response);
			}

		}
		
		
		return true;
	}
	
}
