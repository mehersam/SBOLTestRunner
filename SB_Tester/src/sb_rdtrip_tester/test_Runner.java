package sb_rdtrip_tester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;
import org.synbiohub.frontend.SynBioHubException;

public class test_Runner {

	public static void main(String[] args) throws FileNotFoundException //throws SBOLValidationException, IOException, SBOLConversionException, SynBioHubException, URISyntaxException {
	{
		String prefix = "https://synbiohub.utah.edu";
		String email = "mehersam251@gmail.com";
		String pass = "S@ipav12";
		
		String id = "Tester_1";
		String version = "1";
		String name = "Round_Trip_Tester";
		String description = "This is a simple roundtrip test"; 
		
		String topLevel = "https://synbiohub.utah.edu/user/mehersam/Tester_1/Tester_1_collection/1"; 
		URI TP_collection = URI.create(topLevel); 
		
		File[] files = new File("./SBOL2/").listFiles(); 

		for (int i = 0; i < files.length; i++)
		{
			try {		
				System.out.println("*************************************Working on: " + files[i].getName() + "*************************************");
				new  TestRunner (prefix, email, pass, files[i], id, version, name, description, TP_collection, true, true);
				System.out.println("\n\n");
			} catch (SBOLValidationException | IOException | SBOLConversionException | SynBioHubException
					| URISyntaxException e) {
				
				
				e.printStackTrace();
			}
		
			//System.out.println("**************************************************************************");


		}
		
		

	}

}
