package sb_rdtrip_tester;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.synbiohub.frontend.SynBioHubException;
import net.sf.json.JSONObject;

public class Wrapper {

	public static void main(String[] args) throws Exception {
		
		SBOLTestRunner wrapper = new SBOLTestRunner();
		SynBioHubEmulator emulator; 
		FileOutputStream fs = null; 
		BufferedOutputStream bs = null;
		PrintStream printStream = null; 
		
		if(!new File("Compared/").exists() && !new File("Compared/").isDirectory())
		{
			new File("Compared/").mkdir();
		}
	
		for(File f : wrapper.run("all"))
		{
			fs = new FileOutputStream("Compared/" + f.getName().substring(0, f.getName().length()-4) + "_file_comparisonErrors.txt"); 
			bs = new BufferedOutputStream(fs);
			printStream = new PrintStream(bs, true);
			System.setErr(printStream);
			
			try{
			emulator = new SynBioHubEmulator(f, args[0]); 
			
			JSONObject output = emulator.retrieve(); 
			String orig_file = output.getString("orig_file_name");
			SBOLDocument emulated = (SBOLDocument) output.get("Emulated");
			SBOLDocument retrieved = (SBOLDocument) output.get("Retrieved");
			
			wrapper.writeRetrieved(orig_file + "_Retrieved.xml", retrieved);
			wrapper.writeEmulated(orig_file + "_Emulated.xml", emulated); 

			wrapper.compare(orig_file, emulated, retrieved); 
			}
			catch (SBOLValidationException e) {
			e.printStackTrace();
			System.err.println("Fail");
		}
		catch (SBOLConversionException e) {
			e.printStackTrace();
			System.err.println("Fail");
		}
		catch (SynBioHubException e) {
			e.printStackTrace();
			System.err.println("Fail");
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			System.err.println("Fail");
		}
		if (SBOLValidate.getNumErrors()!=0) {
			for (String error : SBOLValidate.getErrors()) {
				System.err.println(error);
			}
			System.err.println("Fail");
		}
		}
		System.setErr(null);
		fs.close();
		bs.close();
		printStream.close();
		
	}

}
