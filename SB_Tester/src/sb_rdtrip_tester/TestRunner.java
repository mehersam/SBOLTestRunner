package sb_rdtrip_tester;

import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.namespace.QName;

import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

public class TestRunner {

	private SynBioHubFrontend hub;
	private SBOLDocument doc;

	public TestRunner(String prefix, String email, String pass, File read_file, String id, String version, String name,
			String desc, URI TP_collection, boolean complete, boolean create_defaults) throws SBOLValidationException, IOException, SBOLConversionException,
			SynBioHubException, URISyntaxException {

		hub = new SynBioHubFrontend(prefix);
		
		login(email, pass);

		create_design(prefix, read_file, complete, create_defaults);

		// submit document to SB
		hub.submit(id, version, name, desc, "", "", "1", doc);

		retrieve_compare(TP_collection, read_file.getName());

	}

	private void retrieve_compare(URI topLevelURI, String orig_file)
			throws SynBioHubException, IOException, SBOLConversionException, URISyntaxException, SBOLValidationException {
		
		String retrieved_doc_file_name = (String) orig_file.subSequence(0, orig_file.length()-4);
		
		SBOLDocument retrievedDoc = new SBOLDocument();
		retrievedDoc = hub.getSBOL(topLevelURI); //get the SBOLDocument back
		retrievedDoc.write("Retrieved_Files/" + retrieved_doc_file_name + "_Retrieved.xml"); //write to new file

		String newPrefix = "https://synbiohub.utah.edu/user/mehersam/Tester_1/";
		
		//attempt to emulate the changes 
		doc = emulator(doc, newPrefix, topLevelURI);
		doc = ack_changes(doc, retrievedDoc, newPrefix, topLevelURI);
		doc.write("Emulated_Files/" + retrieved_doc_file_name + "_Emulated.xml");

		SBOLValidate.compareDocuments(orig_file + "_Emulated", doc, orig_file + "_Retrieved", retrievedDoc);
		
	}

	private SBOLDocument ack_changes(SBOLDocument doc, SBOLDocument retrievedDoc, String newPrefix, URI topLevelURI) throws SBOLValidationException {
		
		// CHANGE 3: add timestamp of uploaded document
		Collection timeCollection = retrievedDoc.getCollection(topLevelURI);
		String time = "";

		for (Annotation a : timeCollection.getAnnotations()) {
			if (a.getQName().getLocalPart().equals("created"))
				time = a.getStringValue();
		}
		
		Collection c = doc.getCollection("Tester_1_collection", "1");
		c.createAnnotation(new QName("http://purl.org/dc/terms/", "created", "dcterms"), time);

		return retrievedDoc;
	}

	private SBOLDocument emulator(SBOLDocument doc, String newPrefix, URI topLevelURI)
			throws SBOLValidationException, URISyntaxException {

		// CHANGE 1: change URI prefix
		doc = doc.changeURIPrefixVersion(newPrefix, "1");
		doc.setDefaultURIprefix(newPrefix);

		// CHANGE 2: add owned by annotation to Collection
		String ownedByURI = "https://synbiohub.utah.edu/user/mehersam";

		// CHANGE 4: add top level collection
		Collection c = doc.createCollection("Tester_1_collection", "1");

		String name = "Round_Trip_Tester";
		String description = "This is a simple roundtrip test";
		c.setName(name);
		c.setDescription(description);
		c.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), "Meher");
	
		for (TopLevel tp : doc.getTopLevels()) {
			if (!tp.getIdentity().equals(c.getIdentity()))
				c.addMember(tp.getIdentity());

		}

		(new IdentifiedVisitor() {

			@Override
			public void visit(Identified identified, TopLevel topLevel) {

				try {

					addTopLevelToNestedAnnotations(topLevel, identified.getAnnotations());

					identified.createAnnotation(
							new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "ownedBy", "sbh"),
							new URI(ownedByURI));

					identified.createAnnotation(
							new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "topLevel", "sbh"),
							topLevel.getIdentity());

					String class_type = identified.getClass().toString().replace("class org.sbolstandard.core2.", "");
					identified.createAnnotation(new QName("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type", "rdf"),
							new URI("http://sbols.org/v2#" + class_type));

					// System.out.println(tp.getClass());

				} catch (SBOLValidationException e) {

				} catch (URISyntaxException e) {
				}

			}

		}).visitDocument(doc);

		return doc;

	}

	private void addTopLevelToNestedAnnotations(TopLevel topLevel, List<Annotation> annotations) {
		for (Annotation annotation : annotations) {
			if (annotation.isNestedAnnotations()) {
				List<Annotation> nestedAnnotations = annotation.getAnnotations();
				addTopLevelToNestedAnnotations(topLevel, nestedAnnotations);
				nestedAnnotations.add(
						new Annotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "topLevel", "sbh"),
								topLevel.getIdentity()));
				annotation.setAnnotations(nestedAnnotations);
			}
		}
	}

	/**
	 * Login function to login into SynBioHub
	 * 
	 * @param email
	 *            - of account to login into
	 * @param pass
	 *            - pass of account to login into
	 * @return
	 */
	private boolean login(String email, String pass) {
		// login info
		try {
			hub.login(email, pass);
		} catch (SynBioHubException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private SBOLDocument create_design(String prefix, File file_to_read, boolean complete, boolean create_default)
			throws SBOLValidationException, IOException, SBOLConversionException {
		
		doc = new SBOLDocument();
		doc.setDefaultURIprefix(prefix);
		doc.setComplete(complete);
		doc.setCreateDefaults(create_default);
		
		SBOLReader.setKeepGoing(true);
		doc = SBOLReader.read(file_to_read.getAbsolutePath());
		//doc.read(file_to_read.getAbsolutePath());
	
		return doc;

	}
}
