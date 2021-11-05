import java.util.ArrayList;
import java.util.HashMap;

public class SymbolList {
    HashMap<String,Symbol> symbolMap = new HashMap<>();
    ArrayList<Symbol> symbolList = new ArrayList<>();
    final int blockName;
    final SymbolList parent;

    public SymbolList(int blockName) {
        this(blockName,null);
    }

    public SymbolList(int blockName, SymbolList parent) {
        this.blockName = blockName;
        this.parent = parent;
    }

    Symbol declareNewTemp() {
        Symbol temp = new Symbol(null,blockName,symbolList.size(),Symbol.TEMP,0);
        symbolList.add(temp);
        return temp;
    }

    Symbol declareNewVar(String ident) {
        if(symbolMap.containsKey(ident)){
            return null;
        } else {
            Symbol temp = new Symbol(ident,blockName,symbolList.size(),Symbol.VAR,0);
            symbolMap.put(ident,temp);
            symbolList.add(temp);
            return temp;
        }
    }

    void declareNewConst(String ident, int value) {
        if(!symbolMap.containsKey(ident)){
            Symbol temp = new Symbol(ident,blockName,symbolList.size(),Symbol.CONST,value);
            symbolMap.put(ident,temp);
            symbolList.add(temp);
        }
    }

    Symbol getSymbol(String ident) {
        return symbolMap.getOrDefault(ident, null);
    }
}
