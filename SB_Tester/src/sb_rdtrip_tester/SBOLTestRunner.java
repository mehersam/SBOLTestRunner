//package sb_rdtrip_tester;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.HashSet;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//import org.sbolstandard.core2.SBOLConversionException;
//import org.sbolstandard.core2.SBOLValidationException;
//import org.synbiohub.frontend.SynBioHubException;
//
//@RunWith(Parameterized.class)
//public class SBOLTestRunner {
//
//	private File file; 
//	
//	public SBOLTestRunner(File file){
//		this.file = file; 
//	}
//	
//	/**
//	 * @return a set of files to test
//	 */
//	@Parameterized.Parameters
//	public static java.util.Collection<File> files() {
//		File file_base = null ;
//		java.util.Collection<File> col = new HashSet<File>();
//		try {
//			file_base = new File(SBOLTestRunner.class.getResource("/SBOL2/").toURI());
//		}
//		catch (URISyntaxException e1) {
//			e1.printStackTrace();
//		}
//		for (File f : file_base.listFiles()) {
//			col.add(f);
//		}
//		return col;
//	}
//	
//	
//	
//	@Test
//	public void test_TestRunner() throws SBOLValidationException, IOException, SBOLConversionException, SynBioHubException, URISyntaxException {
//		String prefix = "https://synbiohub.utah.edu";
//		String email = "mehersam251@gmail.com";
//		String pass = "S@ipav12";
//		
//		String id = "Tester_1";
//		String version = "1";
//		String name = "Round_Trip_Tester";
//		String description = "This is a simple roundtrip test"; 
//		
//		String topLevel = "https://synbiohub.utah.edu/user/mehersam/Tester_1/Tester_1_collection/1"; 
//		URI TP_collection = URI.create(topLevel); 
//		System.out.println("Working on: " + file.getName());
//		new  TestRunner (prefix, email, pass, file, id, version, name, description, TP_collection, true, true);
//	
//	}
//
//}
