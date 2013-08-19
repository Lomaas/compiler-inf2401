package no.uio.ifi.cflat.types;

/*
 * module Types
 */

import no.uio.ifi.cflat.code.Code;
import no.uio.ifi.cflat.error.Error;
import no.uio.ifi.cflat.scanner.Token;
import static no.uio.ifi.cflat.scanner.Token.*;

/*
 * Handle Cb types.
 */

public class Types {
    public static BasicType doubleType, intType;

    public static void init() {
	doubleType = new BasicType() {
		@Override public int size() {
		    return 8;
		}

		@Override public String typeName() {
		    return "double";
		}

		@Override public void genJumpIfZero(String jumpLabel) {
		    Code.genInstr("", "fstps", Code.tmpLabel, "");
		    Code.genInstr("", "cmpl", "$0,"+Code.tmpLabel, "");
		    Code.genInstr("", "je", jumpLabel, "");
		}
	    };
	//-- Must be changed in part 2:
    }

    public static void finish() {
	//-- Must be changed in part 2:
    }

    public static Type getType(Token t) {
	switch (t) {
	case doubleToken: return doubleType;
	case intToken:    return intType;
        default:          Error.panic(t+" is no type name!");
	}
	return null;  // Just to keep the Java compiler happy. :-)
    }
}
