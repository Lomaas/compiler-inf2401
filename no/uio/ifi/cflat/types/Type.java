package no.uio.ifi.cflat.types;

abstract public class Type {
    abstract public int size();
    abstract public String typeName();

    abstract public void checkSameType(int lineNum, Type otherType, String what);
    abstract public void checkType(int lineNum, Type correctType, String what);
    public void genJumpIfZero(String jumpLabel) {}
}
