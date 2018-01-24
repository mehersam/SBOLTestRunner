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
		
		SBOLTestRunner wrapper = new SBOLTestRunner("sbol2");
		int sizeOfTestSuite = wrapper.getSizeOfTestSuite();
		SynBioHubEmulator emulator; 
		FileOutputStream fs = null; 
		BufferedOutputStream bs = null;
		PrintStream printStream = null; 
		
		if(!new File("Compared/").exists() && !new File("Compared/").isDirectory())
		{
			new File("Compared/").mkdir();
		}
			
		int i = 0;
		int success = 0;
		int fail = 0;
		for(File f : wrapper.getTestFiles())
		{
			//if (!f.getAbsoluteFile().toString().startsWith("/Users/myers/git/SBOLTestRunner/SB_Tester/target/classes/sb_rdtrip_tester/SBOLTestSuite/SBOL2/CreateAndRemoveComponentDefinition")) continue;
			//if (!f.getAbsoluteFile().toString().endsWith("_orig.xml")) continue;
			//if (!f.getAbsoluteFile().toString().endsWith("Final_TASBE.xml")) continue;
			i++;
			fs = new FileOutputStream("Compared/" + f.getName().substring(0, f.getName().length()-4) + "_file_comparisonErrors.txt"); 
			bs = new BufferedOutputStream(fs);
			printStream = new PrintStream(bs, true);
			System.setErr(printStream);

			try{
				emulator = new SynBioHubEmulator(f, args[0]); 

				//JSONObject output = emulator.retrieve(); 
				String orig_file = emulator.retreiveInputFile(); //output.getString("orig_file_name");
				SBOLDocument retrieved = emulator.retrieveDoc(); //(SBOLDocument) output.get("Retrieved");
				SBOLDocument emulated = emulator.retrieveEmulated(); //(SBOLDocument) output.get("Emulated");

				retrieved.write("Retrieved/" + orig_file + "_Retrieved.xml");
				emulated.write("Emulated/" + orig_file + "_Emulated.xml");

				//wrapper.writeRetrieved(orig_file + "_Retrieved.xml", retrieved);
				//wrapper.writeEmulated(orig_file + "_Emulated.xml", emulated); 

				wrapper.compare(orig_file, emulated, retrieved); 
				if (SBOLValidate.getNumErrors()!=0) {
					int errorCnt = 0;
					for (String error : SBOLValidate.getErrors()) {
						if (error.startsWith("Namespace")) continue;
						System.err.println(error);
						errorCnt++;
					}
					if (errorCnt > 0) {
						fail++;
						System.out.println(i+" of "+sizeOfTestSuite+": "+ f.getName()+" Fail "+fail);
						System.err.println("Fail");
					} else {
						success++;
						System.out.println(i+" of "+sizeOfTestSuite+": "+ f.getName()+" Success "+success);
						System.err.println("Success");
					}
				} else {
					success++;
					System.out.println(i+" of "+sizeOfTestSuite+": "+ f.getName()+" Success "+success);
					System.err.println("Success");
				}
			}
			catch (SBOLValidationException e) {
				fail++;
				System.out.println(i+" of "+sizeOfTestSuite+": "+ f.getName()+" Fail "+fail);
				e.printStackTrace(System.err);
				System.err.println("Fail");
			}
			catch (SBOLConversionException e) {
				fail++;
				System.out.println(i+" of "+sizeOfTestSuite+": "+ f.getName()+" Fail "+fail);
				e.printStackTrace(System.err);
				System.err.println("Fail");
			}
			catch (SynBioHubException e) {
				fail++;
				System.out.println(i+" of "+sizeOfTestSuite+": "+ f.getName()+" Fail "+fail);
				e.printStackTrace(System.err);
				System.err.println("Fail");
			}
			catch (URISyntaxException e) {
				fail++;
				System.out.println(i+" of "+sizeOfTestSuite+": "+ f.getName()+" Fail "+fail);
				e.printStackTrace(System.err);
				System.err.println("Fail");
			}
			fs.close();
			bs.close();
			printStream.close();
		}
		System.setErr(null);
	}

}
