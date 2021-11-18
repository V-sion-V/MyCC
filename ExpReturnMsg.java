public class ExpReturnMsg {
    final int type;
    final int iVal;
    final Symbol symbol;


    public ExpReturnMsg(int iVal) {
        this.iVal = iVal;
        this.type = 1;
        symbol = null;
    }

    public ExpReturnMsg(Symbol symbol) {
        this.symbol = symbol;
        this.type = 2;
        this.iVal = 0;
    }

    public ExpReturnMsg(Symbol symbol,boolean isBoolean) {
        if(isBoolean)this.type = 3;
        else type = 2;
        this.symbol = symbol;
        this.iVal = 0;
    }

    @Override
    public String toString() {
        if(symbol == null) return ""+ iVal;
        else return symbol.toString();
    }

    public boolean isNumber() {
        return type==1;
    }

    public  boolean isSymbol() {
        return type==2;
    }

    public  boolean isBooleanSymbol() {
        return type==3;
    }

}
