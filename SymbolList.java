import java.util.ArrayList;
import java.util.HashMap;

public class SymbolList {
    HashMap<String,Symbol> symbolMap = new HashMap<>();
    public int innerNumber = 0;
    final int blockName;
    final SymbolList parent;

    public SymbolList(int blockName) {
        this(blockName,null);
    }

    public SymbolList(int blockName, SymbolList parent) {
        this.blockName = blockName;
        this.parent = parent;
    }

    TempSymbol declareNewTemp() {
        return new TempSymbol(blockName, innerNumber++);
    }

    VarSymbol declareNewVar(String ident) {
        if(symbolMap.containsKey(ident)){
            return null;
        } else {
            VarSymbol temp = new VarSymbol(blockName, innerNumber++, ident);
            symbolMap.put(ident,temp);
            return temp;
        }
    }

    ConstSymbol declareNewConst(String ident, int value) {
        if (symbolMap.containsKey(ident)) {
            return null;
        } else {
            ConstSymbol temp = new ConstSymbol(blockName, innerNumber++, ident, value);
            symbolMap.put(ident,temp);
            return temp;
        }
    }

    ArraySymbol declareNewConstArray(String ident, ArrayList<Integer> dimensions) {
        if(symbolMap.containsKey(ident)){
            return null;
        } else {
            ArraySymbol temp = new ArraySymbol(blockName, innerNumber++, ident, Symbol.ConstArray, dimensions);
            symbolMap.put(ident,temp);
            return temp;
        }
    }

    ArraySymbol declareNewVarArray(String ident, ArrayList<Integer> dimensions) {
        if(symbolMap.containsKey(ident)){
            return null;
        } else {
            ArraySymbol temp = new ArraySymbol(blockName, innerNumber++, ident, Symbol.VarArray, dimensions);
            symbolMap.put(ident,temp);
            return temp;
        }
    }

    Symbol getSymbol(String ident) {
        Symbol x =  symbolMap.getOrDefault(ident, null);
        if(x!=null) {
            return x;
        } else if(parent!=null) {
            return parent.getSymbol(ident);
        } else return null;
    }
}
