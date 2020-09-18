package com.playerrealms.droplet.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UploadLog {
    private static long rateLimit = System.currentTimeMillis();

    public static String uploadlog(boolean bypass){
        if (bypass == false) {
            if (rateLimit <= System.currentTimeMillis()) {
                rateLimit = System.currentTimeMillis() + 1000 * 60 * 5;
                try {
                    File latest = new File("./logs/latest.log");
                    if (latest.exists()) {
                        String inputLine;
                        StringBuilder param = new StringBuilder("content=");
                        param.append(URLEncoder.encode(UploadLog.readFile(latest.getPath(), StandardCharsets.UTF_8), "UTF-8"));
                        URL obj = new URL("");
                        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("User-Agent", "");
                        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        con.setDoInput(true);
                        con.setDoOutput(true);
                        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                        wr.write(param.toString());
                        wr.flush();
                        wr.close();
                        BufferedReader resp = new BufferedReader(new InputStreamReader((con.getInputStream())));
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = resp.readLine()) != null) {
                            response.append(inputLine);
                        }
                        resp.close();
                        String code = response.toString().replace("", "");
                        return code;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return "rateLimit";
            }
        }else if(bypass == true){
            try {
                File latest = new File("./logs/latest.log");
                if (latest.exists()) {
                    String inputLine;
                    StringBuilder param = new StringBuilder("content=");
                    param.append(URLEncoder.encode(UploadLog.readFile(latest.getPath(), StandardCharsets.UTF_8), "UTF-8"));
                    URL obj = new URL("");
                    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", "");
                    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                    wr.write(param.toString());
                    wr.flush();
                    wr.close();
                    BufferedReader resp = new BufferedReader(new InputStreamReader((con.getInputStream())));
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = resp.readLine()) != null) {
                        response.append(inputLine);
                    }
                    resp.close();
                    return response.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    static String readFile(String path, Charset encoding) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(path, new String[0]));
        return new String(encoded, encoding);
    }
}
