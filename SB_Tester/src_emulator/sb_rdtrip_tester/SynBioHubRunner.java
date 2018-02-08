package sb_rdtrip_tester;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.synbiohub.frontend.SynBioHubException;

public class SynBioHubRunner {

	public static void main(String[] args) {

		try {
			if (args.length != 4)
				throw new IOException();
		} catch (IOException e1) {
			System.err.println("Please provide the file to read, settings file, emulated and retrieved files' path.\n\n");
			e1.printStackTrace();
			System.exit(1);
			
		}
		String orig_file = args[0];
		File read_file = new File(orig_file);
		String settings_file = args[1];
		String emulated_file_path = args[2];
		String retrieved_file_path = args[3];
		SBOLDocument retrieved; 
		SBOLDocument emulated; 
		
		if(settings_file == null)
			System.err.println("Settings file could not be read in.\n"); 
		
		try {
			SynBioHubEmulator emulator = new SynBioHubEmulator(read_file, settings_file);
			String orig_file_name = read_file.getName().substring(0, read_file.getName().length()-4);
			try {
				retrieved = emulator.retrieveDoc(); 
				retrieved.write(retrieved_file_path + "/" + orig_file_name + "_retrieved.xml");
				emulated = emulator.retrieveEmulatedDoc();
				emulated.write(emulated_file_path + "/" + orig_file_name + "_emulated.xml");
			} catch (IOException | SBOLConversionException e) {
				e.printStackTrace();
			}
		} catch (SBOLValidationException | IOException | SBOLConversionException | SynBioHubException
				| URISyntaxException e) {
			System.err.println("SBH Emulator failed to initalize" + e.getMessage() + "\n"); 
			e.printStackTrace();
		}
		
		System.out.println("Finished."); 
	}
}
