public class Symbol {
    static final int TEMP = 0, Var = 1, Const = 2, VarArray = 3, ConstArray = 4, Func = 5;
    int blockName;
    int innerNumber;
    int type;

    public Symbol(int blockName, int innerNumber, int type) {
        this.blockName = blockName;
        this.innerNumber = innerNumber;
        this.type = type;
    }

    @Override
    public String toString() {
        return ((blockName==0)?"@":"%")+"b" + blockName + "x" + innerNumber;
    }
}
