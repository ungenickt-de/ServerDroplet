package droplet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

import com.playerrealms.droplet.util.UploadUtil;
import com.playerrealms.droplet.util.UploadUtil.UploadResult;

public class UploadTest {

	@Test
	public void test() throws IOException {
		
		File test = new File("test.txt");
		if(!test.exists()){
			test.createNewFile();
		}
		
		try(BufferedWriter out = new BufferedWriter(new FileWriter(test))){
			out.write("Hello World!");
			out.flush();
		}
		
		UploadResult result = UploadUtil.uploadFile(test);
		
		if(result.isSuccess()){
			System.out.println(result.getUrl());
		}else{
			System.out.println(result.getError()+" "+result.getMessage());
		}
		
	}

}
