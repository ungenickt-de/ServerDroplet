package droplet;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.util.TextFileUploadUtil;

public class TextUploadTest {

	@Test
	public void test() throws IOException, InvalidConfigurationException {
		
		String text = "https://pastebin.com/kGS9BzGs";
		
		TextFileUploadUtil.checkLegalDownloadURL(text);
		
		String[] splits = text.split("/");
		
		if(splits.length == 0){
			throw new MalformedURLException();
		}
		
		String correctedUrl = "https://pastebin.com/raw/"+splits[splits.length-1];
		
		String content = TextFileUploadUtil.downloadText(correctedUrl);
		
		YamlConfiguration testConfig = new YamlConfiguration();
		testConfig.loadFromString(content);
		
		System.out.println(content);
		
	}

}
