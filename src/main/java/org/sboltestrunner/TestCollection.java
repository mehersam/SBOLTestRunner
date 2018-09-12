package org.sboltestrunner;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.sbolstandard.core2.Activity;
import org.sbolstandard.core2.Association;
import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Cut;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.GenericLocation;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.TopLevel;

public class TestCollection {

	public TestCollection() {

	}

	public Collection<File> get_Collection(String id) throws Exception {
		if (id.equals("sbol2"))
			return sbol2();
		else if (id.equals("sbol1"))
			return sbol1();
		else if (id.equals("genbank"))
			return gb();
		else if (id.equals("invalid"))
			return invalidFiles();
		else if (id.equals("rdf"))
			return rdf();
		else if (id.equals("all"))
			return all();
		else if(id.equals("structural"))
			return structural();
		else if(id.equals("functional"))
			return functional();
		else if(id.equals("auxiliary"))
			return auxillary();
		else if(id.equals("structfunc"))
			return structAndFunc();
		else
			throw new Exception("Invalid id passed, cannot find Collection");

	}
	
	
	private Collection<File> all() throws IOException {
		java.util.Collection<File> sbol_files = new HashSet<File>();
		sbol_files.addAll(sbol2());
		sbol_files.addAll(sbol1());
		sbol_files.addAll(gb());
		sbol_files.addAll(invalidFiles());
		sbol_files.addAll(rdf());
		return sbol_files;
	}

	private Collection<File> sbol2() throws IOException {

		File file_base = null;
		java.util.Collection<File> sbol_files = new HashSet<File>();

//		InputStream resourceAsStream = TestCollection.class.getResourceAsStream("/SBOLTestSuite/SBOL2/");
//
//		File tempFile = File.createTempFile(String.valueOf(resourceAsStream.hashCode()), ".tmp");
//	    /tempFile.deleteOnExit();

		try {
			file_base = new File(TestCollection.class.getResource("/SBOLTestSuite/SBOL2/").toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}
		return sbol_files;
	}

	private Collection<File> sbol1() {

		File file_base = null;
		java.util.Collection<File> sbol_files = new HashSet<File>();
		try {
			file_base = new File(TestCollection.class.getResource("/SBOLTestSuite/SBOL1/").toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}

		return sbol_files;
	}

	private Collection<File> gb() {
		File file_base = null;
		java.util.Collection<File> sbol_files = new HashSet<File>();
		try {
			file_base = new File(TestCollection.class.getResource("/SBOLTestSuite/Genbank/").toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}

		return sbol_files;
	}

	private Collection<File> invalidFiles() {
		File file_base = null;
		java.util.Collection<File> sbol_files = new HashSet<File>();
		try {
			file_base = new File(TestCollection.class.getResource("/SBOLTestSuite/InvalidFiles/").toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}

		return sbol_files;
	}

	private Collection<File> rdf() {

		File file_base = null;
		java.util.Collection<File> sbol_files = new HashSet<File>();
		try {
			file_base = new File(TestCollection.class.getResource("/SBOLTestSuite/RDF/").toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}

		return sbol_files;
	}

	private Collection<File> structural() throws SBOLValidationException, IOException, SBOLConversionException {

		java.util.Collection<File> sbol_files = new HashSet<File>();
		sbol_files.addAll(sbol1());
		sbol_files.addAll(sbol2());

		Collection<File> structuralFiles = new HashSet<File>();
		for (File f : sbol_files) {
			SBOLDocument doc = SBOLReader.read(f);
			HashMap<String, Integer> types = count_classes(doc);

			// structural
			if ((!types.containsKey("ModuleDefinition") && !types.containsKey("Model"))
					&& !types.containsKey("Collection") && !types.containsKey("GenericTopLevel")
					&& !types.containsKey("Activity") && !types.containsKey("Attachment") && !types.containsKey("Plan")
					&& !types.containsKey("Agent")) {
				structuralFiles.add(f);
			}

		}

		return structuralFiles;

	}

	private Collection<File> functional() throws SBOLValidationException, IOException, SBOLConversionException {

		java.util.Collection<File> sbol_files = new HashSet<File>();
		sbol_files.addAll(sbol1());
		sbol_files.addAll(sbol2());

		Collection<File> functionalFiles = new HashSet<File>();
		for (File f : sbol_files) {
			SBOLDocument doc = SBOLReader.read(f);
			HashMap<String, Integer> types = count_classes(doc);

			// functional
			if ((!types.containsKey("Sequence") && !types.containsKey("ComponentDefinition"))
					&& !types.containsKey("Collection") && !types.containsKey("GenericTopLevel")
					&& !types.containsKey("Activity") && !types.containsKey("Attachment") && !types.containsKey("Plan")
					&& !types.containsKey("Agent") && !types.containsKey("Implementation")
					&& !types.containsKey("CombinatorialDerivation")) {
				functionalFiles.add(f);
			}

		}

		return functionalFiles;

	}

	private Collection<File> auxillary() throws SBOLValidationException, IOException, SBOLConversionException {

		java.util.Collection<File> sbol_files = new HashSet<File>();
		sbol_files.addAll(sbol1());
		sbol_files.addAll(sbol2());

		Collection<File> auxFiles = new HashSet<File>();
		for (File f : sbol_files) {
			SBOLDocument doc = SBOLReader.read(f);
			HashMap<String, Integer> types = count_classes(doc);

			// auxiliary
			if ((types.containsKey("Collection") || types.containsKey("GenericTopLevel")
					|| types.containsKey("Activity") || types.containsKey("Attachment") || types.containsKey("Plan")
					|| types.containsKey("Agent"))
					&& (!types.containsKey("Sequence") && !types.containsKey("ComponentDefinition")
							&& !types.containsKey("Implementation") && !types.containsKey("CombinatorialDerivation")
							&& !types.containsKey("ModuleDefinition") && !types.containsKey("Model"))) {
				auxFiles.add(f);
			}

		}

		return auxFiles;

	}

	private Collection<File> structAndFunc() throws SBOLValidationException, IOException, SBOLConversionException {

		java.util.Collection<File> sbol_files = new HashSet<File>();
		sbol_files.addAll(sbol1());
		sbol_files.addAll(sbol2());

		Collection<File> sAndfFiles = new HashSet<File>();
		for (File f : sbol_files) {
			SBOLDocument doc = SBOLReader.read(f);
			HashMap<String, Integer> types = count_classes(doc);

			// structural && functional
			if ((types.containsKey("ModuleDefinition") || types.containsKey("Model"))
					&& (types.containsKey("Sequence") || types.containsKey("ComponentDefinition")
							|| types.containsKey("Implementation") || types.containsKey("CombinatorialDerivation"))
					&& (!types.containsKey("Collection") && !types.containsKey("GenericTopLevel")
							&& !types.containsKey("Activity") && !types.containsKey("Attachment")
							&& !types.containsKey("Plan") && !types.containsKey("Agent"))) {
				sAndfFiles.add(f);
			}

		}

		return sAndfFiles;

	}
	
	

	private HashMap<String, Integer> count_classes(SBOLDocument doc)
			throws SBOLValidationException, IOException, SBOLConversionException {

		HashMap<String, Integer> class_counts = new HashMap<String, Integer>();

		// place toplevel objects
		// class_counts.put("TopLevel", doc.getTopLevels().size());
		class_counts.put("Collection", doc.getCollections().size());
		class_counts.put("ComponentDefinition", doc.getComponentDefinitions().size());
		class_counts.put("Model", doc.getModels().size());
		class_counts.put("ModuleDefinition", doc.getModuleDefinitions().size());
		class_counts.put("Sequence", doc.getSequences().size());
		class_counts.put("GenericTopLevel", doc.getGenericTopLevels().size());
		class_counts.put("Attachment", doc.getAttachments().size());
		class_counts.put("CombinatorialDerivation", doc.getCombinatorialDerivations().size());
		class_counts.put("Implementation", doc.getImplementations().size());
		class_counts.put("Activity", doc.getActivities().size());
		class_counts.put("Plan", doc.getPlans().size());
		class_counts.put("Agent", doc.getAgents().size());

		for (Activity act : doc.getActivities()) {
			class_counts.put("Association", act.getAssociations().size());
			class_counts.put("Usage", act.getUsages().size());

			for (Association assoc : act.getAssociations()) {
				if (assoc.getPlan() != null)
					class_counts.put("Plan", class_counts.get("Plan") + 1);
				if (assoc.getAgent() != null)
					class_counts.put("Agent", class_counts.get("Agent") + 1);

			}
		}
		
		for(CombinatorialDerivation combDer : doc.getCombinatorialDerivations())
		{
			int count = 0; 
			if(combDer.getVariableComponents() != null) {
				count = combDer.getVariableComponents().size();
				
			}
			class_counts.put("VariableComponent", class_counts.get("VariableComponent") + count );

		}

		for (TopLevel TL : doc.getTopLevels()) {
			class_counts.put("Annotation", class_counts.get("Annotation") + TL.getAnnotations().size());
			// for (Annotation a : TL.getAnnotations())
			// put_annotations(a);
		}

		// for (Collection c : doc.getCollections()) {
		// for (Annotation a : c.getAnnotations())
		// put_annotations(a);
		// }

		for (ComponentDefinition cd : doc.getComponentDefinitions()) {
			// for (Annotation a : cd.getAnnotations()) {
			// put_annotations(a);
			// }

			class_counts.put("Component", class_counts.get("Component") + cd.getComponents().size());
			for (Component c : cd.getComponents()) {
				// for (Annotation a : c.getAnnotations())
				// put_annotations(a);
				class_counts.put("MapsTo", class_counts.get("MapsTo") + c.getMapsTos().size());

				// class_counts.put("Component-MapsTo", class_counts.get("Component-MapsTo") +
				// c.getMapsTos().size());

				// for (MapsTo mp : c.getMapsTos())
				// for (Annotation a : mp.getAnnotations())
				// put_annotations(a);
			}

			class_counts.put("SequenceAnnotation",
					class_counts.get("SequenceAnnotation") + cd.getSequenceAnnotations().size());
			for (SequenceAnnotation sa : cd.getSequenceAnnotations()) {
				// for (Annotation a : sa.getAnnotations())
				// put_annotations(a);
				// class_counts.put("Location", class_counts.get("Location") +
				// sa.getLocations().size());

				for (Location l : sa.getLocations()) {
					// for (Annotation a : l.getAnnotations())
					// put_annotations(a);
					if (l instanceof Cut)
						class_counts.put("Cut", class_counts.get("Cut") + 1);
					if (l instanceof Range)
						class_counts.put("Range", class_counts.get("Range") + 1);
					if (l instanceof GenericLocation)
						class_counts.put("GenericLocation", class_counts.get("GenericLocation") + 1);
				}
			}
			class_counts.put("SequenceConstraint",
					class_counts.get("SequenceConstraint") + cd.getSequenceConstraints().size());
			// for (SequenceConstraint sc : cd.getSequenceConstraints())
			// for (Annotation a : sc.getAnnotations())
			// put_annotations(a);
		}

		// for (Model m : doc.getModels()) {
		// for (Annotation a : m.getAnnotations())
		// put_annotations(a);
		// }

		for (ModuleDefinition md : doc.getModuleDefinitions()) {
			class_counts.put("FunctionalComponent",
					class_counts.get("FunctionalComponent") + md.getFunctionalComponents().size());
			for (FunctionalComponent fc : md.getFunctionalComponents()) {
				//// for (Annotation a : fc.getAnnotations())
				//// put_annotations(a);
				class_counts.put("MapsTo", class_counts.get("MapsTo") + fc.getMapsTos().size());
				// class_counts.put("FC-MapsTo", class_counts.get("FC-MapsTo") +
				// fc.getMapsTos().size());
				//
				// for (MapsTo mp : fc.getMapsTos())
				// for (Annotation a : mp.getAnnotations())
				// put_annotations(a);
				//
			}

			class_counts.put("Interaction", class_counts.get("Interaction") + md.getInteractions().size());
			for (Interaction i : md.getInteractions()) {
				class_counts.put("Participation", class_counts.get("Participation") + i.getParticipations().size());
				// for (Annotation a : i.getAnnotations())
				// put_annotations(a);
			}

			class_counts.put("Model", class_counts.get("Model") + md.getModels().size());

			class_counts.put("Module", class_counts.get("Module") + md.getModules().size());
			for (Module m : md.getModules()) {
				// for (Annotation a : m.getAnnotations())
				// put_annotations(a);
				// class_counts.put("Module-MapsTo", class_counts.get("Module-MapsTo") +
				// m.getMapsTos().size());
				class_counts.put("MapsTo", class_counts.get("MapsTo") + m.getMapsTos().size());

				// for (MapsTo mp : m.getMapsTos())
				// for (Annotation a : mp.getAnnotations())
				// put_annotations(a);

			}
		}

		// GenericTopLevel's have nothing but annotations
		for(String key : class_counts.keySet())
			if(class_counts.get(key) == null)
				if(class_counts.get(key) == 0)
					class_counts.remove(key);
		
		return class_counts; 
	}



}
