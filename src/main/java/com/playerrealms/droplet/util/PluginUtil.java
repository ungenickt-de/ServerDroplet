package com.playerrealms.droplet.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class PluginUtil {

	private PluginUtil() {}
	
	public static YamlConfiguration loadPluginYmlFromZip(File plugin) throws FileNotFoundException, IOException, InvalidConfigurationException{
		
		YamlConfiguration config = new YamlConfiguration();
		
		try(InputStream is = readFileFromZip("plugin.yml", plugin)){
			config.load(new InputStreamReader(is));
		}
		
		return config;
	}
	
	private static InputStream readFileFromZip(String fileName, File file) throws FileNotFoundException, IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		

		try(FileInputStream fis = new FileInputStream(file)){
			try(BufferedInputStream bis = new BufferedInputStream(fis)){
				try(ZipInputStream zis = new ZipInputStream(bis)){
					ZipEntry ze = null;
					
					while((ze = zis.getNextEntry()) != null){
						if(ze.getName().equals(fileName)){
							
							byte[] buffer = new byte[1024];
							
							int len;
							
							while((len = zis.read(buffer)) != -1){
								bos.write(buffer, 0, len);
							}
							
							bos.close();
							break;
							
						}
					}
				}
			}
		}
		
		return new ByteArrayInputStream(bos.toByteArray());
		
	}
	
}
