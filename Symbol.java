public class Symbol {
    static final int TEMP = 0, VAR = 1, CONST = 2;
    String originForm;
    int blockName;
    int innerNumber;
    int type;
    int constValue;

    public Symbol(String originForm, int blockLayer, int innerNumber, int type, int constValue) {
        this.originForm = originForm;
        this.blockName = blockLayer;
        this.innerNumber = innerNumber;
        this.type = type;
        this.constValue = constValue;
    }

    @Override
    public String toString() {
        return "%b" + blockName + "x" + innerNumber;
    }
}
