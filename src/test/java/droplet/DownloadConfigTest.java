package droplet;

import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import com.playerrealms.droplet.ServerDroplet;

public class DownloadConfigTest {

	@Test
	public void test() throws IOException, InvalidConfigurationException {
		ServerDroplet.downloadConfig();
	}

}
