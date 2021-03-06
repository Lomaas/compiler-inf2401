package no.uio.ifi.cflat.chargenerator;

/*
 * module CharGenerator
 */

import java.io.*;
import no.uio.ifi.cflat.cflat.Cflat;
import no.uio.ifi.cflat.error.Error;
import no.uio.ifi.cflat.log.Log;

/*
 * Module for reading single characters.
 */
public class CharGenerator {
    public static char curC, nextC;
    public static boolean bug = false;
    private static LineNumberReader sourceFile = null;
    private static String sourceLine;
    private static int sourcePos;

    public static void init() {
        try {
            sourceFile = new LineNumberReader(new FileReader(Cflat.sourceName));
        } catch (FileNotFoundException e) {
            Error.error("Cannot read " + Cflat.sourceName + "!");
        }
        sourceLine = "";  sourcePos = 0;  curC = nextC = ' ';

        readNext();
    }

    public static void finish() {
        if (sourceFile != null) {
            try {
                sourceFile.close();
            } catch (IOException e) {}
        }
    }

    public static boolean isMoreToRead() {
        if(sourceLine == null){
            return false;
        }
        return true;
    }

    public static int curLineNum() {
        return (sourceFile == null ? 0 : sourceFile.getLineNumber());
    }

    public static void readNext() {
        curC = nextC;

        if (! isMoreToRead()) return;

        while(sourceLine.length() < 1 || sourceLine.charAt(0) == '#' || sourcePos >= sourceLine.length()) {
            // Read next line
            readNextLine();
            if(sourceLine == null){
                return;
            }
        }

        if(isMoreToRead()) {
            nextC = sourceLine.charAt(sourcePos);
            sourcePos ++;
        }
    }

    private static void readNextLine(){
        try {
            sourceLine = sourceFile.readLine();

            if(sourceLine != null)
                Log.noteSourceLine(CharGenerator.curLineNum(), sourceLine);

            sourcePos = 0;
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
