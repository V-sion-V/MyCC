public class ExpReturnMsg {
    final int value;
    final Symbol symbol;

    public ExpReturnMsg(int value) {
        this.value = value;
        symbol = null;
    }

    public ExpReturnMsg(Symbol symbol) {
        this.symbol = symbol;
        value = 0;
    }

    @Override
    public String toString() {
        if(symbol == null) return ""+value;
        else return symbol.toString();
    }

    public boolean isNumber(){
        return symbol==null;
    }
}
