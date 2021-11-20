import java.util.ArrayList;

public class ArraySymbol extends NonTempSymbol{
    ArrayList<Integer> dimensions;
    public ArraySymbol(int blockName, int innerNumber, String originForm, int type, ArrayList<Integer> dimensions) {
        super(blockName,innerNumber,type,originForm);
        this.dimensions = dimensions;
    }

    public String getType(int dim) {
        StringBuilder out = new StringBuilder("i32");
        for(int i = dimensions.size()-1; i >= dim; i--) {
            out.insert(0, "[" + dimensions.get(i) + " x ");
            out.append(']');
        }
        return out.toString();
    }
}
