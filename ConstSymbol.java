public class ConstSymbol extends NonTempSymbol{
    int constValue;
    public ConstSymbol(int blockName, int innerNumber,String originForm, int constValue) {
        super(blockName, innerNumber, Const, originForm);
        this.constValue = constValue;
    }
}
