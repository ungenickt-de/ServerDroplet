package com.playerrealms.droplet.util;

import com.mongodb.client.MongoCollection;
import com.playerrealms.droplet.ServerDroplet;
import org.bson.Document;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class TextFileUploadUtil {
	
	public static void checkLegalDownloadURL(String potentialUrl) throws MalformedURLException{
		URL url = new URL(potentialUrl);
		
		if(!(url.getHost().equalsIgnoreCase("pastebin.com") || url.getHost().equalsIgnoreCase("paste.mcua.net"))){
			throw new MalformedURLException();
		}
	}
	
	public static String downloadText(String u) throws IOException{
		URL url = new URL(u);
		
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setRequestProperty("User-Agent", "PlayerIslands");
		conn.setUseCaches(false);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String text = "";
		
		String line;
		
		while((line = reader.readLine()) != null){
			text += line+"\n";
		}
		
		return text;
	}
	
	/**
	 * Uploads texts
	 * @param text the text to upload
	 * @return the url it is at
	 * @throws Exception if it wasn't able to be uploaded, the exception message is the error
	 */
	public static String uploadText(String title, String text) throws Exception {
		
		MongoCollection<Document> textCollection = ServerDroplet.getInstance().getDatabase().getCollection("text_upload");
		
		Document doc = new Document();
		
		doc.append("content", text);
		doc.append("title", title);
		doc.append("date", System.currentTimeMillis());
		
		String code = "";
		String possible = "abcdefgh12345";
		Random r = new Random();
		
		for(int i = 0; i < 12;i++) {
			code += possible.charAt(r.nextInt(possible.length()));
		}
		
		doc.append("code", code);
		
		textCollection.insertOne(doc);
		
		return "https://playerislands.com/text?code="+code+" ";
		
	}
	
}
