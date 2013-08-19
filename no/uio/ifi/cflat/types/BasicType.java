package no.uio.ifi.cflat.types;

import no.uio.ifi.cflat.error.Error;

abstract public class BasicType extends Type {
    @Override public void checkSameType(int lineNum, Type otherType, String what) {
	if (this != otherType)
	    Error.error(lineNum, 
			what + " should have the same type, not " + typeName() +
			" and " + otherType.typeName() + ".");
    }

    @Override public void checkType(int lineNum, Type correctType, String what) {
	if (this != correctType)
	    Error.error(lineNum, 
			what + " is " + typeName() +
			", not " + correctType.typeName() + ".");
    }

}
