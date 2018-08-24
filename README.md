SBOL TestRunner
=============

SBOLTestRunner is an automated tool to determine the compliance of SBOL applications through a round-trip testing methodology.
There are two types of round-trip testing methods, simple and extended. Applications that simply import SBOL data and export the data
without an internal data modifications are tested through the simple round-trip process. 

For applications that process imported data and make data modifications e.g. change URI prefix, these applications are tested 
using an extended round-trip test. For this, an emulator application should be provided. The emulator should take an SBOL file and the location for where the
exported file is written as arguments and performs the same steps the application tested does to the imported data. Once these emulation steps are performed on 
the input file, this file should be written to the argument filepath provided.

Running SBOL TestRunner stand-alone
=================================

SIMPLE ROUND-TRIP
=================================

The SBOL TestRunner can be run through the command-line by using the following command: 

  java -jar <jarFileName> -<collection_type> -e <application_cmd> <retrieved_file_path> <compared_file_path>

a. <jarFileName> is the name of the SBOL TestRunner jar file downloaded
b. -<collection_type> specifies the type of SBOL test files to test the application with

The different collection types include: 

	-s2 : the SBOL2 example files
	-s1 : the SBOL1 example files
	-gb : the GenBank example files
	-i  : the invalid example files 
	-s  : the examples files encoding structural SBOL data types
	-f  : the examples files encoding functional SBOL data types
	-a  : the examples files encoding auxillary SBOL data types
	-sf  : the examples files encoding structural and functional SBOL data types
	
c. The command to run the application being tested
d. The file path for where exported files will be retrieved from.
e. The file path for where the imported and exported files will be compared.

Extended ROUND-TRIP
=================================

The SBOL TestRunner can be run through the command-line by using the following command: 

  java -jar <jarFileName> -<collection_type> <application_cmd> <emulated_file_path> <retrieved_file_path> <compared_file_path>

a. <jarFileName> is the name of the SBOL TestRunner jar file downloaded
b. -<collection_type> specifies the type of SBOL test files to test the application with

The different collection types include: 

	-s2 : the SBOL2 example files
	-s1 : the SBOL1 example files
	-gb : the GenBank example files
	-i  : the invalid example files 
	-s  : the examples files encoding structural SBOL data types
	-f  : the examples files encoding functional SBOL data types
	-a  : the examples files encoding auxillary SBOL data types
	-sf  : the examples files encoding structural and functional SBOL data types
	
c. The command to run the application being tested
d. The file path for where the emulated files exist.
e. The file path for where exported files will be retrieved from.
f. The file path for where the imported and exported files will be compared.

Compilation
=================================

To run the SBOLTestRunner from source, clone the repository and build with the following command: 

 mvn clean install
 
The jar file should be found within the 'target' directory. The same steps to run the SBOLTestRunner should be followed as described above.