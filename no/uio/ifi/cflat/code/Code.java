package no.uio.ifi.cflat.code;

/*
 * module Code
 */

import java.io.*;
import no.uio.ifi.cflat.cflat.Cflat;
import no.uio.ifi.cflat.error.Error;
import no.uio.ifi.cflat.log.Log;

/*
 * Code generation for the x86 processor.
 */
public class Code {
    private static PrintWriter codeFile;
    private static boolean generatingData = false;

    public static final String tmpLabel = ".tmp";

    public static void init() {
	String codeFileName;
	
	if (Cflat.sourceBaseName == null) return;
	codeFileName = Cflat.sourceBaseName + ".s";
	try {
	    codeFile = new PrintWriter(codeFileName);
	} catch (FileNotFoundException e) {
	    Error.error("Cannot create code file " + codeFileName + "!");
	}

	genVar(tmpLabel, false, 4, "Temporary storage");
    }

    public static void finish() {
	codeFile.close();
    }


    private static int numLabels = 0;

    public static String getLocalLabel() {
	return String.format(".L%04d", ++numLabels);
    }

    private static void printLabel(String lab, boolean justALabel) {
	if (lab.length() > 6) {
	    codeFile.print(lab + ":");
	    if (! justALabel) codeFile.print("\n        ");
	} else if (lab.length() > 0) {
	    codeFile.printf("%-8s", lab+":");
	} else {
	    codeFile.print("        ");
	}
    }

    public static void genInstr(String lab, String instr, 
				String arg, String comment) {
	if (generatingData) {
	    codeFile.println("        .text");
	    generatingData = false;
	}

	printLabel(lab, (instr+arg+comment).equals(""));
	codeFile.printf("%-7s %-23s ", instr, arg);
	if (comment.length() > 0) {
	    codeFile.print("# " + comment);
	}
	codeFile.println();
    }

    public static void genVar(String name, boolean global,
			      int nBytes, String comment) {
	if (! generatingData) {
	    codeFile.println("        .data");
	    generatingData = true;
	}

	if (global)
	    codeFile.println("        .globl  " + name);

	printLabel(name, false);
	codeFile.printf(".fill   %-24d", nBytes);

	if (comment.length() > 0) {
	    codeFile.print("# " + comment);
	}
	codeFile.println();
    }
}
