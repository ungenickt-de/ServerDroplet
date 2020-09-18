package droplet;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import org.junit.Test;

import com.playerrealms.droplet.lang.Language;

public class LanguageTest {

	@Test
	public void test() throws FileNotFoundException {
		
		Language.registerLanguage(getTextResource("lang/en_US.yml"), "en_us");
		Language.registerLanguage(getTextResource("lang/ja_JP.yml"), "ja_jp");
		
		
		Language english = Language.getLanguage("en_us");
		Language japanese = Language.getLanguage("ja_jp");
		
		int missing = 0;
		
		for(String key : english.getKeys()){
			
			if(!japanese.hasKey(key)){
				missing++;
				System.err.println(key);
			}
			
		}
		if(missing > 0){
			//fail("Missing "+missing+" text for "+japanese.getLanguageName());
		}
		
	}

	private Reader getTextResource(String string) throws FileNotFoundException {
		File file = new File("src/main/resources/"+string);
		return new FileReader(file);
	}

}
