package sb_rdtrip_tester;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLReaderTest;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.synbiohub.frontend.SynBioHubException;
import net.sf.json.JSONObject;

//TODO SBOLTestRunner
public class Wrapper {

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			System.err.println(
					"Please provide the test runner program command, path location of the emulated and retrieved files.");
			System.exit(1);
		}

		String test_runner_cmd = args[0];
		String emulated_file_path = args[1];
		String retrieved_file_path = args[2];

		if (!new File(emulated_file_path).exists() && !new File(emulated_file_path).isDirectory()) {
			new File(emulated_file_path).mkdir();
		}

		if (!new File(retrieved_file_path).exists() && !new File(retrieved_file_path).isDirectory()) {
			new File(retrieved_file_path).mkdir();
		}

		if (!new File("Compared/").exists() && !new File("Compared/").isDirectory()) {
			new File("Compared/").mkdir();
		}

		SBOLTestRunner wrapper = new SBOLTestRunner("sbol2");
		int sizeOfTestSuite = wrapper.getSizeOfTestSuite();
		FileOutputStream fs = null;
		BufferedOutputStream bs = null;
		PrintStream printStream = null;

		int i = 0;
		int success = 0;
		int fail = 0;
		for (File f : wrapper.getTestFiles()) {
			i++;
			fs = new FileOutputStream(
					"Compared/" + f.getName().substring(0, f.getName().length() - 4) + "_file_comparisonErrors.txt");
			bs = new BufferedOutputStream(fs);
			printStream = new PrintStream(bs, true);
			System.setErr(printStream);

			try {	
				String filename = f.getName().substring(0, f.getName().length()-4); 
				String emulated_full_fp = emulated_file_path + filename + "_emulated.xml";
				String retrieved_full_fp = retrieved_file_path + filename + "_retrieved.xml";

				Process test_runner = null;
				try {
					test_runner = Runtime.getRuntime().exec(String.format("%s %s %s %s", test_runner_cmd,
							f.getAbsolutePath(), emulated_full_fp, retrieved_full_fp));
					test_runner.waitFor(); // wait for the runner to finish
											// running

					if (test_runner.exitValue() != 0) {
						InputStream err = test_runner.getErrorStream();
						byte c[] = new byte[err.available()];
						err.read(c, 0, c.length);
						int emulatorErrorCnt = 0;
						System.err.println(new String(c));
					}

				} catch (Exception e) {
					System.err.println("TestRunner failed to execute properly\n\n" + e.getMessage());
					System.err.println(e.getStackTrace());

				}
				
				File emulated_File = new File(emulated_full_fp); 
				File retrieved_File = new File(retrieved_full_fp); 
				
				SBOLDocument emulated = SBOLReader.read(emulated_File);
				SBOLDocument retrieved = SBOLReader.read(retrieved_File);

				InputStream err = test_runner.getErrorStream();
				byte c[] = new byte[err.available()];
				err.read(c, 0, c.length);
				int emulatorErrorCnt = 0;
				System.err.println(new String(c));
				emulatorErrorCnt++;
				System.err.println(emulatorErrorCnt);


				wrapper.compare(f.getName(), emulated, retrieved);
				if (SBOLValidate.getNumErrors() != 0) {
					int errorCnt = 0;
					for (String error : SBOLValidate.getErrors()) {
						if (error.startsWith("Namespace"))
							continue;
						System.err.println(error);
						errorCnt++;
					}
					if (errorCnt > 0) {
						fail++;
						System.out.println(i + " of " + sizeOfTestSuite + ": " + f.getName() + " Fail " + fail);
						System.err.println("Fail");
					} else {
						success++;
						System.out.println(i + " of " + sizeOfTestSuite + ": " + f.getName() + " Success " + success);
						System.err.println("Success");
					}
				} else {
					success++;
					System.out.println(i + " of " + sizeOfTestSuite + ": " + f.getName() + " Success " + success);
					System.err.println("Success");
				}
			} catch (SBOLValidationException e) {
				fail++;
				System.out.println(i + " of " + sizeOfTestSuite + ": " + f.getName() + " Fail " + fail);
				e.printStackTrace(System.err);
				System.err.println("Fail");
			} catch (SBOLConversionException e) {
				fail++;
				System.out.println(i + " of " + sizeOfTestSuite + ": " + f.getName() + " Fail " + fail);
				e.printStackTrace(System.err);
				System.err.println("Fail");
			}
			// catch (SynBioHubException e) {
			// fail++;
			// System.out.println(i+" of "+sizeOfTestSuite+": "+ f.getName()+"
			// Fail "+fail);
			// e.printStackTrace(System.err);
			// System.err.println("Fail");
			// }
			fs.close();
			bs.close();
			printStream.close();
		}
		System.setErr(null);
	}

}
