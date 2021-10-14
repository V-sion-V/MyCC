import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    private final Scanner sc = new Scanner(System.in);
    private String in = sc.next();

    private final Pattern ident = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
    private final Pattern number = Pattern.compile("0[xX][0-9a-fA-F]+|[1-9][0-9]*|0[0-7]*");
    private final Pattern eq = Pattern.compile("==");
    private final Pattern other = Pattern.compile("=|;|\\(|\\)|(\\{)|}|\\+|\\*|/|<|>");
    private final Pattern inLineComment = Pattern.compile("//");
    private final Pattern commentL = Pattern.compile("/\\*");
    private final Pattern commentR = Pattern.compile("\\*/");

    private final HashMap<String,String> keyword = getKeyword();
    private final HashMap<String,String> separator = getSeparator();

    private HashMap<String,String> getKeyword() {
        HashMap<String,String> keyword = new HashMap<>();
        keyword.put("int","Int");
        keyword.put("if","If");
        keyword.put("else","Else");
        keyword.put("while","While");
        keyword.put("break","Break");
        keyword.put("continue","Continue");
        keyword.put("return","Return");
        return keyword;
    }

    private HashMap<String,String> getSeparator() {
        HashMap<String,String> separator = new HashMap<>();
        separator.put("=","Assign");
        separator.put(";","Semicolon");
        separator.put("(","LPar");
        separator.put(")","RPar");
        separator.put("{","LBrace");
        separator.put("}","RBrace");
        separator.put("+","Plus");
        separator.put("*","Mult");
        separator.put("/","Div");
        separator.put("<","Lt");
        separator.put(">","Gt");
        return separator;
    }

    public String getToken() {
        String ret;
        if (in.length() > 0) {
            Matcher identMatcher = ident.matcher(in);
            Matcher numberMatcher = number.matcher(in);
            Matcher eqMatcher = eq.matcher(in);
            Matcher otherMatcher = other.matcher(in);
            if(inLineComment.matcher(in).lookingAt()) {
                in = sc.nextLine();
                in = sc.next();
                ret = getToken();
            } else if(commentL.matcher(in).lookingAt()){
                while(sc.hasNext()) {
                    in = sc.next();
                    Matcher temp = commentR.matcher(in);
                    if(temp.find()){
                        in = in.substring(temp.end());
                        return getToken();
                    }
                }
                ret = "EOF";
            } else if (identMatcher.lookingAt()) {
                if (keyword.containsKey(identMatcher.group()))
                    ret = keyword.get(identMatcher.group());
                else
                    ret = "Ident(" + identMatcher.group() + ')';
                in = in.substring(identMatcher.end());
            } else if (numberMatcher.lookingAt()) {
                int number;
                if(numberMatcher.group().startsWith("0x"))
                    number = Integer.parseInt(numberMatcher.group().substring(2),16);
                else if(numberMatcher.group().startsWith("0"))
                    number = Integer.parseInt(numberMatcher.group(),8);
                else
                    number = Integer.parseInt(numberMatcher.group(),10);
                ret = "Number(" + number + ')';
                in = in.substring(numberMatcher.end());
            } else if (eqMatcher.lookingAt()) {
                ret = "Eq";
                in = in.substring(eqMatcher.end());
            } else if (otherMatcher.lookingAt()) {
                ret = separator.get(otherMatcher.group());
                in = in.substring(otherMatcher.end());
            } else {
                ret = "Err";
                in = "";
            }
            return ret;
        } else if (sc.hasNext()) {
            in = sc.next();
            return getToken();
        } else {
            return "EOF";
        }
    }
}
