package droplet;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

import com.playerrealms.droplet.util.MojangAPI;
import com.playerrealms.droplet.util.MojangAPI.NameLookupException;
import com.playerrealms.droplet.util.MojangAPI.NameNotFoundException;

public class NameLookupTest {

	@Test
	public void test() throws NameLookupException {
		UUID id = UUID.fromString("edd09768-3def-4960-b9b0-6521d71df5ac");
		
		String name = MojangAPI.getUsername(id);
		
		if(!"bowser123467".equals(name)){
			//fail(name+" is not bowser123467");
		}
		
		//UUID retrieved = MojangAPI.getUUID(name);
		
		/*if(!retrieved.equals(id)) {
			//fail(retrieved+" is not "+id);
		}*/
	}

}
