import java.net.PortUnreachableException;

public class Symbol {
    static final int TEMP = 0, VAR = 1, CONST = 2;
    String originForm;
    int blockName;
    int innerNumber;
    int type;
    int constValue;

    public Symbol(int type, int blockLayer, int innerNumber,String originForm, int constValue) {
        this.originForm = originForm;
        this.blockName = blockLayer;
        this.innerNumber = innerNumber;
        this.type = type;
        this.constValue = constValue;
    }

    public Symbol(int type, int blockLayer, int innerNumber, String originForm) {
        this(type,blockLayer,innerNumber,originForm,0);
    }


    public Symbol(int type,  int blockLayer, int innerNumber) {
        this(type,blockLayer,innerNumber,null);
    }




    @Override
    public String toString() {
        return "%b" + blockName + "x" + innerNumber;
    }
}
