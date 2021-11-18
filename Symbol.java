import java.net.PortUnreachableException;

public class Symbol {
    static final int TEMP = 0, VAR = 1, CONST = 2;
    String originForm;
    int blockName;
    int innerNumber;
    int type;
    int constValue;

    public Symbol(int type, int blockName, int innerNumber,String originForm, int constValue) {
        this.originForm = originForm;
        this.blockName = blockName;
        this.innerNumber = innerNumber;
        this.type = type;
        this.constValue = constValue;
    }

    public Symbol(int type, int blockName, int innerNumber, String originForm) {
        this(VAR,blockName,innerNumber,originForm,0);
    }


    public Symbol(int type,  int blockName, int innerNumber) {
        this(TEMP,blockName,innerNumber,null,0);
    }




    @Override
    public String toString() {
        return ((blockName==0)?"@":"%")+"b" + blockName + "x" + innerNumber;
    }
}
