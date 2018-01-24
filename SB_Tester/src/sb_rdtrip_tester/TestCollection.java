package sb_rdtrip_tester;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;

public class TestCollection {
	
	public TestCollection()
	{
		
	}
	
	public Collection<File> get_Collection(String id) throws Exception
	{
		if(id.equals("sbol2"))
			return sbol2(); 
		else if(id.equals("sbol1"))
			return sbol1(); 
		else if(id.equals("gb"))
			return gb(); 
		else if(id.equals("invalid"))
			return invalidFiles(); 
		else if(id.equals("rdf"))
			return rdf(); 
		else if(id.equals("all"))
			return all(); 
		else throw new Exception("Invalid id passed, cannot find Collection"); 
		
	}
	
	private Collection<File> all()
	{
		java.util.Collection<File> sbol_files = new HashSet<File>();
		sbol_files.addAll(sbol2()); 
		sbol_files.addAll(sbol1()); 
		sbol_files.addAll(gb()); 
		sbol_files.addAll(invalidFiles()); 
		sbol_files.addAll(rdf()); 
		return sbol_files; 
	}
	
	private Collection<File> sbol2()
	{
		//what I need is to pass into the emulator each test file. for each subdirectory. 
		//load in all the testfiles and ask for each one separately? 
		
		File file_base = null ;
		java.util.Collection<File> sbol_files = new HashSet<File>();
		try {
			file_base = new File(TestCollection.class.getResource("SBOLTestSuite/SBOL2/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}
		
		return sbol_files;
	}
	
	private Collection<File> sbol1()
	{
		//what I need is to pass into the emulator each test file. for each subdirectory. 
		//load in all the testfiles and ask for each one separately? 
		
		File file_base = null ;
		java.util.Collection<File> sbol_files = new HashSet<File>();
		try {
			file_base = new File(TestCollection.class.getResource("SBOLTestSuite/SBOL1/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}
		
		return sbol_files;
	}
	
	private Collection<File> gb()
	{
		//what I need is to pass into the emulator each test file. for each subdirectory. 
		//load in all the testfiles and ask for each one separately? 
		
		File file_base = null ;
		java.util.Collection<File> sbol_files = new HashSet<File>();
		try {
			file_base = new File(TestCollection.class.getResource("SBOLTestSuite/GenBank/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}
		
		return sbol_files;
	}
	
	private Collection<File> invalidFiles()
	{
		//what I need is to pass into the emulator each test file. for each subdirectory. 
		//load in all the testfiles and ask for each one separately? 
		
		File file_base = null ;
		java.util.Collection<File> sbol_files = new HashSet<File>();
		try {
			file_base = new File(TestCollection.class.getResource("SBOLTestSuite/InvalidFiles/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}
		
		return sbol_files;
	}
	
	private Collection<File> rdf()
	{
		//what I need is to pass into the emulator each test file. for each subdirectory. 
		//load in all the testfiles and ask for each one separately? 
		
		File file_base = null ;
		java.util.Collection<File> sbol_files = new HashSet<File>();
		try {
			file_base = new File(TestCollection.class.getResource("SBOLTestSuite/RDF/").toURI());
		}
		catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		for (File f : file_base.listFiles()) {
			sbol_files.add(f);
		}
		
		return sbol_files;
	}


}
