//package sb_rdtrip_tester;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.List;
//
//import javax.xml.namespace.QName;
//
//import org.sbolstandard.core2.Annotation;
//import org.sbolstandard.core2.Collection;
//import org.sbolstandard.core2.Identified;
//import org.sbolstandard.core2.ModuleDefinition;
//import org.sbolstandard.core2.SBOLConversionException;
//import org.sbolstandard.core2.SBOLDocument;
//import org.sbolstandard.core2.SBOLValidate;
//import org.sbolstandard.core2.SBOLValidationException;
//import org.sbolstandard.core2.TopLevel;
//import org.synbiohub.frontend.SynBioHubException;
//import org.synbiohub.frontend.SynBioHubFrontend;
//
//public class tester {
//
//	private static  SynBioHubFrontend hub; 
//	private static SBOLDocument doc; 
//	public static void main(String[] args) throws SBOLValidationException, IOException, SBOLConversionException, SynBioHubException, URISyntaxException {
//
//		String prefix = "https://synbiohub.utah.edu";
//		String email = "mehersam251@gmail.com";
//		String pass = "S@ipav12";
//		
//		doc = new SBOLDocument(); 
//		doc.setDefaultURIprefix(prefix);
//		doc.setComplete(true);
//		doc.setCreateDefaults(true);	
//		
//		doc.read("RepressionModel.xml");  //create_design(prefix); //create the design to upload
//		
//		//Synbiohub work
//		hub = new SynBioHubFrontend(prefix);
//		
//		//login with user credentials
//		if(!login(prefix, email, pass))
//			System.out.println("login failed");
//
//		//upload_design	
//		String id = "Tester_1";
//		String version = "1";
//		String name = "Round_Trip_Tester";
//		String description = "This is a simple roundtrip test"; 
//		
//		hub.submit(id, version, name, description, "", "", "1", doc);
//		
//		
//		//retrieve design and validate
//		String topLevel = "https://synbiohub.utah.edu/user/mehersam/Tester_1/Tester_1_collection/1"; 
//		URI URI_retrieved = URI.create(topLevel); 
//		
//		retrieve_validate(URI_retrieved); 
//		
//	}
//
//	
//	private static void retrieve_validate(URI topLevelURI) throws SBOLValidationException, SynBioHubException, IOException, SBOLConversionException, URISyntaxException 
//	{
//		SBOLDocument retrievedDoc = new SBOLDocument(); 
//		
//		retrievedDoc = hub.getSBOL(topLevelURI);
//		retrievedDoc.write("RetrievedDoc.xml");
//		
//		String newPrefix = "https://synbiohub.utah.edu/user/mehersam/Tester_1/";
//		//doc.setDefaultURIprefix(topLevelURI.toString());
//			
//		System.out.println("SynBioHub uri: " + retrievedDoc.getDefaultURIprefix()+ "\n");  
//		System.out.println("Original Doc uri: " + doc.getDefaultURIprefix() + "\n");  
//
//
//		//SBOLValidate.validateSBOL(retrievedDoc, true, true, true);
//		doc = emulator(doc,retrievedDoc, newPrefix, topLevelURI);
//		doc.write("EmulatedDoc.xml");
//
//		SBOLValidate.compareDocuments("Original", doc, "Retrieved", retrievedDoc);
//		//doc.addNamespace(new URI(""), prefix);
//	}
//	
//	private static  SBOLDocument emulator(SBOLDocument doc, SBOLDocument retrievedDoc, String newPrefix, URI topLevelURI) throws SBOLValidationException, URISyntaxException
//	{
//		
//		doc = doc.changeURIPrefixVersion(newPrefix, "1");
//		doc.setDefaultURIprefix(newPrefix);
//		String ownedByURI = "https://synbiohub.utah.edu/user/mehersam";
//	
//		
//		Collection timeCollection = retrievedDoc.getCollection(topLevelURI);
//		String time = ""; 
//		for(Annotation a : timeCollection.getAnnotations())
//		{
//			if(a.getQName().getLocalPart().equals("created"))
//				time = a.getStringValue();
//			
//		}
//		
//		Collection c = doc.createCollection("Tester_1_collection", "1");
//		
//		String name = "Round_Trip_Tester";
//		String description = "This is a simple roundtrip test"; 
//		c.setName(name);
//		c.setDescription(description);
//		c.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), "Meher");
//		c.createAnnotation(new QName("http://purl.org/dc/terms/", "created", "dcterms"), time);
//		for(TopLevel tp : doc.getTopLevels())
//		{
//			if(!tp.getIdentity().equals(c.getIdentity()))
//				c.addMember(tp.getIdentity());
//			
//		}
//		
//		 (new IdentifiedVisitor() {
//
//             @Override
//             public void visit(Identified identified,TopLevel topLevel) {
//
//                     try {
//                    	
//
//                             addTopLevelToNestedAnnotations(topLevel, identified.getAnnotations());
//
//                             identified.createAnnotation(
//                                             new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "ownedBy", "sbh"),
//                                             new URI(ownedByURI));
//
//                             identified.createAnnotation(
//                                             new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "topLevel", "sbh"),
//                                             topLevel.getIdentity());
//                             
//                              String class_type = identified.getClass().toString().replace("class org.sbolstandard.core2.", ""); 
//               				  identified.createAnnotation(
//                                         new QName("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type", "rdf"),
//                                         new URI("http://sbols.org/v2#" + class_type));
//               			
//               			//System.out.println(tp.getClass());
//                            
//
//                     } catch (SBOLValidationException e) {
//
//
//                     }
//                     catch(URISyntaxException e) {}
//
//             }
//
//     }).visitDocument(doc);
//			
//
//		return doc;
//		
//	}
//	
//
//	public static  void addTopLevelToNestedAnnotations(TopLevel topLevel, List<Annotation> annotations) {
//		for (Annotation annotation : annotations) {
//		if (annotation.isNestedAnnotations()) {
//		List<Annotation> nestedAnnotations = annotation.getAnnotations();
//		addTopLevelToNestedAnnotations(topLevel, nestedAnnotations);
//		nestedAnnotations.add(new Annotation(
//		new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#", "topLevel", "sbh"),
//		topLevel.getIdentity()));
//		annotation.setAnnotations(nestedAnnotations);
//		}
//		}
//		}
//	
//	
//	//TODO log an issue to have login function return a boolean for success or not
//	private static  boolean login(String prefix, String email, String pass) {
//		// login info
//		try {
//			hub.login(email, pass);
//		} catch (SynBioHubException e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
//	
//	private SBOLDocument create_design(String prefix) throws SBOLValidationException, IOException, SBOLConversionException{
//		SBOLDocument document = new SBOLDocument();
//		document.setDefaultURIprefix(prefix);
//		document.setComplete(false);
//		document.setCreateDefaults(true);	
//		
//		document.read("RepressionModel.xml");
//			
//	
//		return document;
//
//	}
//
//}
