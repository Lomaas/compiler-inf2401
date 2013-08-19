package no.uio.ifi.cflat.cflat;

/*
 * module Cflat
 *
 * (c) 2013 dag@ifi.uio.no
 */

import java.io.*;
import no.uio.ifi.cflat.chargenerator.CharGenerator;
import no.uio.ifi.cflat.code.Code;
import no.uio.ifi.cflat.error.Error;
import no.uio.ifi.cflat.log.Log;
import no.uio.ifi.cflat.scanner.Scanner;
import static no.uio.ifi.cflat.scanner.Token.*;
import no.uio.ifi.cflat.syntax.Syntax;
import no.uio.ifi.cflat.types.Types;

/*
 * The main program of the Cb compiler.
 */
public class Cflat {
    public static final String version = "2013-07-01";

    public static String sourceName = null,  // Source file name
	sourceBaseName = null;               // Source file name without extension
    public static boolean noLink = false;    // Should we drop linking?
    public static final String myOS =        // The current operating system
	System.getProperty("os.name");

    private static boolean testParser = false, testScanner = false;

    private static boolean checkParams(String[] args) {
	for (String opt: args) {
	    if (opt.equals("-c")) {
		noLink = true;
	    } else if (opt.equals("-logB")) {
		Log.doLogBinding = true;
	    } else if (opt.equals("-logP")) {
		Log.doLogParser = true;
	    } else if (opt.equals("-logS")) {
		Log.doLogScanner = true;
	    } else if (opt.equals("-logT")) {
		Log.doLogTree = true;
	    } else if (opt.equals("-testparser")) {
		testParser = true;
		Log.doLogParser = Log.doLogTree = true;
	    } else if (opt.equals("-testscanner")) {
		testScanner = true;  
		Log.doLogScanner = true;
	    } else if (opt.startsWith("-")) {
		return false;
	    } else {
		if (sourceName != null) return false;
		sourceName = sourceBaseName = opt;
		if (opt.length()>3 && opt.endsWith(".cb")) 
		    sourceBaseName = opt.substring(0,opt.length()-3);
		else if (opt.length()>6 && opt.endsWith(".cflat")) 
		    sourceBaseName = opt.substring(0,opt.length()-6);
	    }
	}
	return true;
    }

    /**
     * The actual main program of the Cb compiler.
     * It will initialize the various modules and start the
     * compilation (or module testing, if requested); finally,
     * it will terminate the modules.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
	int exitStatus = 0;

	if (checkParams(args) && sourceName!=null) {
	    System.out.println("This is the Cb compiler (version " + version +
			       " on " + myOS + ")");

	    try {
		Error.init();  Log.init();  Code.init();  Types.init();
		CharGenerator.init();  Scanner.init();  Syntax.init();
		
		if (testScanner) {
		    System.out.print("Scanning...");
		    while (Scanner.nextNextToken != eofToken) 
			Scanner.readNext();
		} else {
		    System.out.print("Parsing...");  Syntax.parseProgram();
		    if (Log.doLogTree) {
			System.out.print(" printing...");  Syntax.printProgram();
		    }
		    if (! testParser) {
			System.out.print(" checking...");  Syntax.checkProgram();
			System.out.print(" generating code...");  Syntax.genCode();  
		    }
		}
		System.out.println(" OK");
	    }
	    catch (RuntimeException e) {
		System.out.println();
		System.err.println(e.getMessage());
		exitStatus = 1;
	    }
	    finally {
		Syntax.finish();  Scanner.finish();  CharGenerator.finish();  
		Types.finish();  Code.finish();  Log.finish();  Error.finish();
	    }

	    if (exitStatus==0 && ! testScanner && ! testParser) 
		assembleCode();
	} else {
	    System.err.println("Usage: cflat [-c] [-log{B|P|S|T}] " +
			       "[-test{scanner|parser}] file");
	    exitStatus = 2;
	}

	System.exit(exitStatus);
    }


    private static void assembleCode() {
	String pName = sourceBaseName;
	if (pName.equals(sourceName)) pName += ".exe";
	String sName = sourceBaseName + ".s";
	String cLib = (underscoredGlobals() ? "cflatus" : "cflat");

	String arg[];
	if (noLink) {
	    arg = new String[4];
	    arg[0] = "gcc";  arg[1] = "-m32";
	    arg[2] = "-c";   arg[3] = sName;
	} else {
	    arg = new String[8];
	    arg[0] = "gcc";      arg[1] = "-m32";
	    arg[2] = "-o";       arg[3] = pName;
	    arg[4] = sName;  
	    arg[5] = "-L.";      arg[6] = "-L/local/share/inf2100";
	    arg[7] = "-l"+cLib;  
	}
	System.out.print("Running");
	for (String s: arg) 
	    System.out.print(" "+s);
	System.out.println();

	ProcessBuilder pb = new ProcessBuilder(arg);
	try {
	    pb.redirectErrorStream(true);
	    final Process proc = pb.start();

	    new Thread () {  // Show any error messages:
		public void run () {
		    BufferedReader output =
			new BufferedReader(new InputStreamReader(proc.getInputStream()));
		    String line;
		    try {
			while ((line=output.readLine()) != null) 
			    System.out.println(line);
		    } catch (IOException e) {}
		}
	    }.start();
	    
	    int status = proc.waitFor();
	    if (status != 0) 
		Error.error("Running gcc produced errors!");
	} 
	catch (InterruptedException e) {}
	catch (IOException e) {
	    Error.error("Cannot run gcc!");	
	}
    }


    public static boolean underscoredGlobals() {
	/* Only Unix and Linux do not use underscored global names. */

	return ! myOS.toLowerCase().matches(".+n.x.*");
    }
}
