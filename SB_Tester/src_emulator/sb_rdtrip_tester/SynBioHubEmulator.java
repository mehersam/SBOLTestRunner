package sb_rdtrip_tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import net.sf.json.JSONObject; 
import javax.xml.namespace.QName;

import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.IdentifiedVisitor;
import org.sbolstandard.core2.Model;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

public class SynBioHubEmulator {

	private SynBioHubFrontend hub;
	private SBOLDocument doc;
	private SBOLDocument retrievedDoc;
	private Config config; //this needs an empty constructor with some default values i.e prefix, etc..?
	private File input_file; 
	public SynBioHubEmulator(File read_file, String settings_file) throws SBOLValidationException, IOException, SBOLConversionException,
			SynBioHubException, URISyntaxException {
		
		input_file = read_file; 
		config = parse_JSON(settings_file); //read in settings file
		
		hub = new SynBioHubFrontend(config.get_url(), config.get_prefix());
		login(config.get_email(), config.get_pass());
		
		create_design(config.get_prefix(), input_file, config.get_complete(), config.get_defaults());

		// submit document to SB
		hub.createCollection(config.get_id(), config.get_version(), config.get_name(), config.get_desc(), "", true, doc);

		retrievedDoc = hub.getSBOL(config.get_TP_col());

	}

	public JSONObject retrieve()
			throws SynBioHubException, IOException, SBOLConversionException, URISyntaxException, SBOLValidationException {
				
		SBOLDocument retrievedDoc = new SBOLDocument();
		retrievedDoc = hub.getSBOL(config.get_TP_col()); //get the SBOLDocument back
		
		//retrievedDoc.write("Retrieved/" + retrieved_doc_file_name + "_Retrieved.xml"); //write to new file

		String newPrefix = config.get_prefix() + "/user/" + config.get_user() + "/" + config.get_id() + "/";
		
		//attempt to emulate the changes 
		doc = emulator(doc, newPrefix, config.get_TP_col());
		doc = ack_changes(doc, retrievedDoc, newPrefix, config.get_TP_col());
		//doc.write("Emulated/" + retrieved_doc_file_name + "_Emulated.xml");

		//SBOLValidate.compareDocuments(orig_file + "_Emulated", doc, orig_file + "_Retrieved", retrievedDoc);
		 JSONObject value = new JSONObject();
	        value.put("Retrieved", retrievedDoc);
	        value.put("Emulated", doc);
	        value.put("orig_file_name", (String)input_file.getName()); 

		
		return value; 
	}
	
	public SBOLDocument retrieveDoc() throws SynBioHubException {
		return retrievedDoc; 
	}
	
	public SBOLDocument retrieveEmulated() throws SynBioHubException, SBOLValidationException, URISyntaxException {
		String newPrefix = config.get_prefix() + "/user/" + config.get_user() + "/" + config.get_id() + "/";
		
		//attempt to emulate the changes 
		doc = emulator(doc, newPrefix, config.get_TP_col());
		doc = ack_changes(doc, retrieveDoc(), newPrefix, config.get_TP_col());
		return doc;
	}
	
	public String retreiveInputFile() {
		return (String)input_file.getName();
	}

	private SBOLDocument ack_changes(SBOLDocument doc, SBOLDocument retrievedDoc, String newPrefix, URI topLevelURI) throws SBOLValidationException {
		
		// CHANGE 3: add timestamp of uploaded document
		Collection timeCollection = retrievedDoc.getCollection(topLevelURI);
		String time = "";

		for (Annotation a : timeCollection.getAnnotations()) {
			if (a.getQName().getLocalPart().equals("created"))
				time = a.getStringValue();
		}
		
		Collection c = doc.getCollection(config.get_id() + "_collection", config.get_version());
		c.createAnnotation(new QName("http://purl.org/dc/terms/", "created", "dcterms"), time);

		//CHANGE 4: add source file name of uploaded document from Model obj
		for(Model m : retrievedDoc.getModels())
		{
			URI source = m.getSource();
			Model doc_Model = doc.getModel(m.getDisplayId(), m.getVersion());
			doc_Model.setSource(source);
		}
		
		return doc;
	}

	private SBOLDocument emulator(SBOLDocument doc, String newPrefix, URI topLevelURI)
			throws SBOLValidationException, URISyntaxException {

		// CHANGE 0: remove objects found in WebOfRegistries
		for (TopLevel tp : doc.getTopLevels()) {
			// TODO: should replace with webOfRegistries
			if (tp.getIdentity().toString().startsWith("https://synbiohub.utah.edu/") ||
					tp.getIdentity().toString().startsWith("https://synbiohub.org/")) {
				doc.removeTopLevel(tp);
			}
		}
		
		// CHANGE 1: change URI prefix	
		doc.setDefaultURIprefix("http://dummy.org");
		doc = doc.changeURIPrefixVersion(newPrefix, "1");
		doc.setDefaultURIprefix(newPrefix);

		// CHANGE 2: add owned by annotation to Collection
		String ownedByURI = config.get_prefix() + "/user/" + config.get_user();

		// CHANGE 4: add top level collection
		Collection c = doc.createCollection("Tester_1_collection", "1");

		String name = config.get_name();
		String description = config.get_desc();
		c.setName(name);
		c.setDescription(description);
		c.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), config.get_creator());
	
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
					if (class_type.equals("Activity") || class_type.equals("Usage") || class_type.equals("Association") ||
							class_type.equals("Agent") || class_type.equals("Plan")) {
						identified.createAnnotation(new QName("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type", "rdf"),
								new URI("http://www.w3.org/ns/prov#" + class_type));
					} else {
						identified.createAnnotation(new QName("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type", "rdf"),
								new URI("http://sbols.org/v2#" + class_type));
					}
					// System.out.println(tp.getClass());

				} catch (SBOLValidationException e) {

				} catch (URISyntaxException e) {
				}

			}

		}).visitDocument(doc);
		
		return doc;

	}

	private void addTopLevelToNestedAnnotations(TopLevel topLevel, List<Annotation> annotations) throws SBOLValidationException {
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
		
		//SBOLReader.setKeepGoing(true);
		doc = SBOLReader.read(file_to_read.getAbsolutePath());
		//doc.read(file_to_read.getAbsolutePath());
	
		return doc;

	}
	
	private Config parse_JSON(String settings_file) throws IOException, URISyntaxException
	{		

		File f = new File(SynBioHubEmulator.class.getResource(settings_file).toURI());
		InputStream is = new FileInputStream(f);
		String jsonTxt = IOUtils.toString(is, "UTF-8");
		JSONObject json = JSONObject.fromObject(jsonTxt);       

		String url = json.getString( "url" );
		String prefix = json.getString( "prefix" );
		String email = json.getString( "email" );
		String pass = json.getString( "pass" );
		String user = json.getString("user");
		String creator = json.getString("creator");
		String id = json.getString( "id" );
		String version = json.getString( "version" );
		String name = json.getString( "name" );
		String desc = json.getString( "desc" );
		URI TP_collection = new URI(json.getString("topLevel")); 
		boolean complete = json.getBoolean("complete"); 
		boolean create_defaults = json.getBoolean("create_defaults"); 

		return new Config(url, prefix, email, pass, user, creator, id, version, name, desc, TP_collection, complete, create_defaults); 

	}
}


