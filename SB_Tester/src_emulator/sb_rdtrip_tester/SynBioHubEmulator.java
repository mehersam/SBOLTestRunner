package sb_rdtrip_tester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.commons.io.IOUtils;
import net.sf.json.JSONObject; 
import net.sf.json.JSONSerializer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.xml.namespace.QName;

import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.Model;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

public class SynBioHubEmulator {

	private SynBioHubFrontend hub;
	private SBOLDocument doc;
	private JSONObject value; 
	private Config config; //this needs an empty constructor with some default values for prefix?
	
	public SynBioHubEmulator(File read_file, String settings_file) throws SBOLValidationException, IOException, SBOLConversionException,
			SynBioHubException, URISyntaxException {
		
		config = parse_JSON(settings_file); //read in settings file
		
		hub = new SynBioHubFrontend(config.get_url(), config.get_prefix());
		initialize_results();
		login(config.get_email(), config.get_pass());
		
		create_design(config.get_prefix(), read_file, config.get_complete(), config.get_defaults());

		// submit document to SB
		hub.submit(config.get_id(), config.get_version(), config.get_name(), config.get_desc(), "", "", "1", doc);

		retrieve_compare(config.get_TP_col(), read_file.getName());

	}
	
	public void initialize_results()
	{
		//TODO: check if directory doesn't exist
		if(!new File("Retrieved/").exists() && !new File("Retrieved/").isDirectory())
			{
				new File("Retrieved/").mkdir();
			}
		//TODO: check if directory doesn't exist
		if(!new File("Emulated/").exists() && !new File("Emulated/").isDirectory())
			{
				new File("Emulated/").mkdir();
			}
	}

	private void retrieve_compare(URI topLevelURI, String orig_file)
			throws SynBioHubException, IOException, SBOLConversionException, URISyntaxException, SBOLValidationException {
		
		String retrieved_doc_file_name = (String) orig_file.subSequence(0, orig_file.length()-4);
		
		SBOLDocument retrievedDoc = new SBOLDocument();
		retrievedDoc = hub.getSBOL(topLevelURI); //get the SBOLDocument back
		
		retrievedDoc.write("Retrieved/" + retrieved_doc_file_name + "_Retrieved.xml"); //write to new file

		String newPrefix = "https://synbiohub.utah.edu/user/mehersam/Tester_1/";
		
		//attempt to emulate the changes 
		doc = emulator(doc, newPrefix, topLevelURI);
		doc = ack_changes(doc, retrievedDoc, newPrefix, topLevelURI);
		doc.write("Emulated/" + retrieved_doc_file_name + "_Emulated.xml");

		//SBOLValidate.compareDocuments(orig_file + "_Emulated", doc, orig_file + "_Retrieved", retrievedDoc);
		JsonObject value = Json.createObjectBuilder()
			     .add("Retrieved", (JsonValue) retrievedDoc)
			     .add("Emulated", (JsonValue) doc)
			     .add("orig_file_name", orig_file)
			     .build();
		}
	
	public JSONObject send_docs()
	{
		return value;
		
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

		//CHANGE 4: add source file name of uploaded document from Model obj
		for(Model m : retrievedDoc.getModels())
		{
			URI source = m.getSource();
			Model retrieved_Model = doc.getModel(m.getDisplayId(), m.getVersion());
			retrieved_Model.setSource(source);
		}
		
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
	
	private SBOLDocument create_design(File f) throws SBOLValidationException, IOException, SBOLConversionException
	{
		doc = new SBOLDocument();
		doc.setComplete(true);
		doc = SBOLReader.read(f); 
		return doc; 
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
	InputStream is = 
             SynBioHubEmulator.class.getResourceAsStream(settings_file);
     String jsonTxt = IOUtils.toString( is );

     JSONObject json = (JSONObject) JSONSerializer.toJSON( jsonTxt );        
     String url = json.getString( "url" );
     String prefix = json.getString( "prefix" );
     String email = json.getString( "email" );
     String pass = json.getString( "pass" );
     String id = json.getString( "id" );
     String version = json.getString( "version" );
     String name = json.getString( "name" );
     String desc = json.getString( "desc" );
     URI TP_collection = new URI(json.getString("topLevel")); 
     boolean complete = json.getBoolean("complete"); 
     boolean create_defaults = json.getBoolean("create_defaults"); 

 	 return new Config(url, prefix, email, pass, id, version, name, desc, TP_collection, complete, create_defaults); 
  
	}
 }
	

