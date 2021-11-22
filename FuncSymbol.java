import java.util.ArrayList;

public class FuncSymbol extends NonTempSymbol{
    ArrayList<Symbol> param;
    boolean isInt;
    public FuncSymbol(int blockName, int innerNumber, String originForm, ArrayList<Symbol> param, boolean isInt) {
        super(blockName,innerNumber,Symbol.Func,originForm);
        this.param = param;
        this.isInt = isInt;
    }
}
