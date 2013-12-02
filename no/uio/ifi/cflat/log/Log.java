package no.uio.ifi.cflat.log;

/*
 * module Log
 */

import java.io.*;
import no.uio.ifi.cflat.cflat.Cflat;
import no.uio.ifi.cflat.error.Error;
import no.uio.ifi.cflat.scanner.Scanner;
import static no.uio.ifi.cflat.scanner.Token.*;

/*
 * Produce logging information.
 */
public class Log {
    public static boolean doLogBinding = false, doLogParser = false,
            doLogScanner = false, doLogTree = true;

    private static String logName, curTreeLine = "";
    private static int nLogLines = 0, parseLevel = 0, treeLevel = 0;

    public static void init() {
        logName = Cflat.sourceBaseName + ".log";
    }

    public static void finish() {

    }

    private static void writeLogLine(String data) {
        try {
            PrintWriter log = (nLogLines==0 ? new PrintWriter(logName) :
                    new PrintWriter(new FileOutputStream(logName,true)));
            log.println(data);  ++nLogLines;
            log.close();
        } catch (FileNotFoundException e) {
            Error.error("Cannot open log file " + logName + "!");
        }
    }

    /*
     * Make a note in the log file that an error has occured.
     *
     * @param message  The error message
     */
    public static void noteError(String message) {
        if (nLogLines > 0)
            writeLogLine(message);
    }


    public static void enterParser(String symbol) {
        if (! doLogParser) return;
        String tmp = "Parser: ";

        for (int i = 1;  i <= parseLevel;  ++i)
            tmp += "  ";

        parseLevel++;

        writeLogLine(tmp + symbol);
    }

    public static void leaveParser(String symbol) {
        if (! doLogParser) return;
        String tmp = "Parser: ";

        if(parseLevel != 0){
            parseLevel--;
            for (int i = 1;  i <= parseLevel;  ++i)
                tmp += "  ";
        }
        writeLogLine(tmp + symbol);
    }

    /**
     * Make a note in the log file that another source line has been read.
     * This note is only made if the user has requested it.
     *
     * @param lineNum  The line number
     * @param line     The actual line
     */
    public static void noteSourceLine(int lineNum, String line) {
        if (! doLogParser && ! doLogScanner) return;

        writeLogLine(Integer.toString(lineNum) + " " + line);
    }

    /**
     * Make a note in the log file that another token has been read 
     * by the Scanner module into Scanner.nextNextToken.
     * This note will only be made if the user has requested it.
     */
    public static void noteToken() {
        if (! doLogScanner) return;

        if(Scanner.nextNextNum == 0)
            writeLogLine("Scanner: " + Scanner.nextNextToken.name() + " " + Scanner.nextNextName);
        else
            writeLogLine("Scanner: " + Scanner.nextNextToken.name() + " " + Scanner.nextNextNum);

    }

    public static void noteBinding(String name, int lineNum, int useLineNum) {
        if (! doLogBinding) return;

        if(lineNum < 0)
            writeLogLine("Binding: Library reference " + name);
        else
            writeLogLine("Binding: Line " + Integer.toString(lineNum) + " " + name +
                " refers to a declaration in line " + Integer.toString(useLineNum));
    }


    public static void wTree(String s) {
        if (curTreeLine.length() == 0) {
            for (int i = 1;  i <= treeLevel;  ++i) curTreeLine += "  ";
        }
        curTreeLine += s;

    }

    public static void wTreeLn() {
        writeLogLine("Tree:     " + curTreeLine);
        curTreeLine = "";
    }

    public static void wTreeLn(String s) {
        wTree(s);  wTreeLn();
    }

    public static void indentTree() {
        curTreeLine = "";
        treeLevel++;
        for (int i = 1;  i <= treeLevel;  ++i) curTreeLine += "  ";
    }

    public static void outdentTree() {
        curTreeLine = "";
        treeLevel--;
        for (int i = 1;  i <= treeLevel;  ++i) curTreeLine += "  ";
    }
}
