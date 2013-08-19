package no.uio.ifi.cflat.types;

import no.uio.ifi.cflat.error.Error;

public class ArrayType extends Type {
    public int nElems;
    public Type elemType;

    public ArrayType(int n, Type t) {
	nElems = n;  elemType = t;
    }

    @Override public int size() {
	return nElems*elemType.size();
    }

    @Override public String typeName() {
	return elemType.typeName() + " array";
    }

    @Override public void checkSameType(int lineNum, Type otherType, String what) {
	if (otherType instanceof ArrayType &&
	    elemType == ((ArrayType)otherType).elemType) return;

	Error.error(lineNum, 
		    what + " should have the same type, not " + typeName() +
		    " and " + otherType.typeName() + ".");
    }

    @Override public void checkType(int lineNum, Type correctType, String what) {
	if (correctType instanceof ArrayType &&
	    elemType == ((ArrayType)correctType).elemType) return;

	Error.error(lineNum, 
		    what + " is " + typeName() +
		    ", not " + correctType.typeName() + ".");
    }
}
