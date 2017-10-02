package sb_rdtrip_tester;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;
import org.synbiohub.frontend.SynBioHubException;

@RunWith(Parameterized.class)
public class SBOLTestRunner {

	private File file; 
	private FileOutputStream fs; 
	private BufferedOutputStream bs;
	private PrintStream printStream; 
	
	
	public SBOLTestRunner(File file){
		this.file = file; 
	}

	/**
	 * @return a set of files to test
	 */
	@Parameterized.Parameters
	public static java.util.Collection<File> files() {
		File file_base = null ;
		java.util.Collection<File> col = new HashSet<File>();
		try {
			file_base = new File(SBOLTestRunner.class.getResource("/SBOL2/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			col.add(f);
		}
		return col;
	}
	
	 @Before
	 public void setUp() throws FileNotFoundException {
		 	fs = new FileOutputStream(file.getName().substring(0, file.getName().length()-4) + " file_comparisonErrors.txt"); 
		 	bs = new BufferedOutputStream(fs);
			printStream = new PrintStream(bs, true);
	//
//			// set output stream to bos to capture output
			//System.setOut(printStream);
			System.setErr(printStream);
	   }
	 
	 @After
	 public void tearDown() throws IOException {
			//System.setOut(null);
			System.setErr(null);
			fs.close();
			bs.close();
			printStream.close();
	    }
	
	
	@Test
	public void test_TestRunner() throws SBOLValidationException, IOException, SBOLConversionException, SynBioHubException, URISyntaxException {
		String prefix = "https://synbiohub.utah.edu";
		String email = "mehersam251@gmail.com";
		String pass = "S@ipav12";
		
		String id = "Tester_1";
		String version = "1";
		String name = "Round_Trip_Tester";
		String description = "This is a simple roundtrip test"; 
		
		String topLevel = "https://synbiohub.utah.edu/user/mehersam/Tester_1/Tester_1_collection/1"; 
		URI TP_collection = URI.create(topLevel); 
		System.err.println("Working on: " + file.getName());
		new  TestRunner (prefix, email, pass, file, id, version, name, description, TP_collection, true, true);
	
	}

}
