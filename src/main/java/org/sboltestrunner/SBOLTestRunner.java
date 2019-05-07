package org.sboltestrunner;

import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.synbiohub.frontend.SynBioHubException;
import net.sf.json.JSONObject;

public class SBOLTestRunner {

	// required variables
	private static String tester_cmd = "";
	private static String compared_file_path = "";
	private static String retrieved_file_path = "";

	// optional variables
	private static String emulated_file_path = "";
	private static String timing_file_path = "";

	private static boolean emulate = false;
	private static boolean timing = false;
	private static String collection_type = "default";

	public static void main(String[] args) throws Exception {
		int arg_count = 0;

		if (args.length < 3) { // must provide the main three args
			usage();
			System.exit(1);
		} else {
			tester_cmd = args[0];
			compared_file_path = args[1];
			retrieved_file_path = args[2];
			while (arg_count < args.length) {
				if (args[arg_count].equals("-f")) {
					collection_type = args[arg_count + 1];
				} else if (args[arg_count].equals("-e")) {
					emulate = true;
					emulated_file_path = args[arg_count + 1];
				} else if (args[arg_count].equals("-t")) {
					timing = true;
					timing_file_path = args[arg_count + 1];
				} else if (args[arg_count].equals("-h")) {
					usage();
				}
				arg_count++;
			}
		}

		createCleanDir(compared_file_path);
		createCleanDir(retrieved_file_path);

		if (emulate)
			createCleanDir(emulated_file_path);
		if (timing)
			createCleanDir(timing_file_path);

		SBOLTestBuilder wrapper = new SBOLTestBuilder(collection_type);
		int sizeOfTestSuite = wrapper.getSizeOfTestSuite();
		FileOutputStream fs = null;
		BufferedOutputStream bs = null;
		PrintStream printStream = null;

		int i = 0;
		int success = 0;
		int fail = 0;
		for (File f : wrapper.getTestFiles()) {
			i++;
			fs = new FileOutputStream(compared_file_path + f.getName().substring(0, f.getName().length() - 4)
					+ "_file_comparisonErrors.txt");
			bs = new BufferedOutputStream(fs);
			printStream = new PrintStream(bs, true);
			System.setErr(printStream);

			try {
				String filename = f.getName().substring(0, f.getName().length() - 4);
				String retrieved_full_fp = retrieved_file_path + filename + "_retrieved.xml";
				File retrieved_File = null;
				SBOLDocument retrieved = null;
				Process test_runner = null;

                System.out.println(String.format("Running %s", filename));

				if (emulate) {
					String emulated_full_fp = emulated_file_path + filename + "_emulated.xml";
					String timing_full_fp = timing_file_path + filename + "_timing.txt";

					try {

						/* FOR DEBUG */
						//System.out.println(String.format("%s %s %s %s %s", tester_cmd,
						//			f.getAbsolutePath(), emulated_full_fp, retrieved_full_fp, timing_full_fp));

						if (timing) {
							test_runner = Runtime.getRuntime().exec(String.format("%s %s %s %s %s", tester_cmd,
									f.getAbsolutePath(), emulated_full_fp, retrieved_full_fp, timing_full_fp));
						} else {
							test_runner = Runtime.getRuntime().exec(String.format("%s %s %s %s", tester_cmd,
									f.getAbsolutePath(), emulated_full_fp, retrieved_full_fp));
						}

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
					retrieved_File = new File(retrieved_full_fp);
					SBOLDocument emulated = SBOLReader.read(emulated_File);
					retrieved = SBOLReader.read(retrieved_File);

					InputStream err = test_runner.getErrorStream();
					byte c[] = new byte[err.available()];
					err.read(c, 0, c.length);
					int emulatorErrorCnt = 0;
					System.err.println(new String(c));
					emulatorErrorCnt++;
					System.err.println(emulatorErrorCnt);

					wrapper.compare(f.getName(), emulated, retrieved);
				} else { // no emulator given for application
					try {
						// System.out.println(String.format("%s %s %s", tester_cmd, f.getAbsolutePath(),
						// retrieved_full_fp));

						if (timing) {
							test_runner = Runtime.getRuntime().exec(String.format("%s %s %s %s", tester_cmd,
									f.getAbsolutePath(), retrieved_full_fp, timing_file_path));
						} else {
							test_runner = Runtime.getRuntime().exec(String.format("%s %s %s", tester_cmd,
									f.getAbsolutePath(), retrieved_full_fp));

						}
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

					retrieved_File = new File(retrieved_full_fp);
					retrieved = SBOLReader.read(retrieved_File);
					SBOLDocument inputFile = SBOLReader.read(f);

					InputStream err = test_runner.getErrorStream();
					byte c[] = new byte[err.available()];
					err.read(c, 0, c.length);
					int emulatorErrorCnt = 0;
					System.err.println(new String(c));
					emulatorErrorCnt++;
					System.err.println(emulatorErrorCnt);

					wrapper.compare(f.getName(), inputFile, retrieved);
				}

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
			} catch (Exception e) {
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

        if(fail > 0) {
            System.exit(1); // Indicate a case didn't pass
        }
		System.setErr(null);
	}

	private static void usage() {
		System.err.println("SBOLTestRunner ");
		System.err.println("Description: performs round-trip testing of an SBOL application\n"
				+ "and can test with SBOL 1, SBOL2, and GenBank files.");
		System.err.println();
		System.err.println("Usage:");
		System.err.println(
				"\tjava -jar SBOLTestRunner.jar <testAppCommand> <comparedFilePath> <retrievedFilePath> [options] [-e <emulatedFilePath> -t <timingFilePath> -f <fileType>]");
		System.err.println();
		System.err.println("Options:");
		System.err.println("\t-e  <emulate> provide file path to print emulated files from test application");
		System.err.println("\t-t  <timing> provide file path to print timing statistics");
		System.err
				.println("\t-f  <collectionType> specifies file types (SBOL1/SBOL2/GenBank) for input (default=SBOL2)");
	}

	private static void createCleanDir(String dirPath) throws IOException {
		if (!new File(dirPath).exists() && !new File(dirPath).isDirectory()) {
			new File(dirPath).mkdir();
		} else {
			FileUtils.cleanDirectory(new File(dirPath));

		}
	}

}
