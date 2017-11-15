package sb_rdtrip_tester;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import org.json.simple.JSONObject;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

public class SBOLTestRunner {

	private TestCollection testCollection; 
	private SynBioHubFrontend frontend; 
	private SBOLDocument doc;
	private SynBioHubEmulator emulator; 
	private net.sf.json.JSONObject obj; 
	public SBOLTestRunner() throws SBOLValidationException, IOException, SBOLConversionException, SynBioHubException, URISyntaxException
	{
		testCollection = new TestCollection(); 
	}
	
	public File run() throws Exception
	{
		Collection<File> sbol_files = testCollection.get_Collection("sbol2");  //for the set of files, pass it into Emulator
		
		for(File f : sbol_files){
			return f; 
		}
		return null;
	}
	
	
	public void compare(String orig_file, SBOLDocument emulated, SBOLDocument retrieved)
	{
		SBOLValidate.compareDocuments(orig_file + "_Emulated", emulated, orig_file + "_Retrieved", retrieved);
	}
	
	
}
