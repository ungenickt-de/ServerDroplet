package com.playerrealms.droplet.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONObject;

public class UploadUtil {

	public static UploadResult uploadFile(File file) throws IOException{
		MultipartEntityBuilder builder = MultipartEntityBuilder.create().addPart("file", new FileBody(file));
        
        HttpResponse returnResponse = Request.Post("https://file.io")
            .body(builder.build())
            .execute().returnResponse();
        
        HttpEntity ent = returnResponse.getEntity();
        
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(ent.getContent()))){
        	String json = reader.readLine();
        	
        	return UploadResult.create(new JSONObject(json));
        }
	}
	
	public static class UploadResult {
		private final boolean success;
		private final String url;
		private final String error, message;
		
		static UploadResult create(JSONObject obj){
			boolean s = obj.getBoolean("success");
			String url = "";
			String error = "";
			String msg = "";
			if(s){
				url = "https://file.io/"+obj.getString("key");
			}else{
				error = obj.getString("error");
				msg = obj.getString("message");
			}
			return new UploadResult(s, url, error, msg);
		}
		
		UploadResult(boolean success, String url, String error, String message) {
			this.success = success;
			this.url = url;
			this.error = error;
			this.message = message;
		}
		
		public boolean isSuccess() {
			return success;
		}
		
		public String getUrl() {
			return url;
		}
		
		public String getError() {
			return error;
		}
		
		public String getMessage() {
			return message;
		}
	}
	
}
