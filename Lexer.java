import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final Scanner sc = new Scanner(System.in);
    private String in = sc.next();

    private final Pattern ident = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
    private final Pattern number = Pattern.compile("0[xX][0-9a-fA-F]+|[1-9][0-9]*|0[0-7]*");
    private final Pattern separator = Pattern.compile("<=|>=|==|!=|&&|\\|\\||=|;|,|\\(|\\)|\\[|]|\\{|}|\\+|-|\\*|/|%|!|<|>");

    private final Pattern inLineComment = Pattern.compile("//");
    private final Pattern commentL = Pattern.compile("/\\*");
    private final Pattern commentR = Pattern.compile("\\*/");

    private final HashMap<String, Integer> keyword = getKeyword();
    private final HashMap<String, Integer> sep = getSep();

    private final LinkedList<Token> tokenBuffer = new LinkedList<>();

    private HashMap<String, Integer> getKeyword() {
        HashMap<String, Integer> keyword = new HashMap<>();
        keyword.put("const", Token.CONST);
        keyword.put("void", Token.VOID);
        keyword.put("int", Token.INT);
        keyword.put("if", Token.IF);
        keyword.put("else", Token.ELSE);
        keyword.put("while", Token.WHILE);
        keyword.put("break", Token.BREAK);
        keyword.put("continue", Token.CONTINUE);
        keyword.put("return", Token.RETURN);
        return keyword;
    }

    private HashMap<String, Integer> getSep() {
        HashMap<String, Integer> separator = new HashMap<>();
        separator.put("<=", Token.LE);
        separator.put(">=", Token.GE);
        separator.put("==", Token.EQ);
        separator.put("!=", Token.NE);
        separator.put("&&", Token.AND);
        separator.put("||", Token.OR);
        separator.put("=", Token.ASSIGN);
        separator.put(";", Token.SEMI);
        separator.put(",", Token.COMMA);
        separator.put("(", Token.LP);
        separator.put(")", Token.RP);
        separator.put("[", Token.LC);
        separator.put("]", Token.RC);
        separator.put("{", Token.LB);
        separator.put("}", Token.RB);
        separator.put("+", Token.PLUS);
        separator.put("-", Token.MINUS);
        separator.put("*", Token.MULT);
        separator.put("/", Token.DIV);
        separator.put("%", Token.MODE);
        separator.put("!", Token.NOT);
        separator.put("<", Token.LT);
        separator.put(">", Token.GT);
        return separator;
    }

    public Token nextToken() {
        while(tokenBuffer.size()<11) {
            tokenBuffer.add(nextTokenInner());
        }
        return tokenBuffer.poll();
    }

    public Token getFurtherToken(int n) {
        if(n>=0&&n<10) {
            return tokenBuffer.get(n);
        } else {
            return null;
        }
    }

    private Token nextTokenInner() {
        Token ret;
        if (!in.isEmpty()) {
            Matcher identMatcher = ident.matcher(in);
            Matcher numberMatcher = number.matcher(in);
            Matcher separatorMatcher = separator.matcher(in);
            if (inLineComment.matcher(in).lookingAt()) {
                in = sc.nextLine();
                in = sc.next();
                ret = nextTokenInner();
            } else if (commentL.matcher(in).lookingAt()) {
                in = in.substring(2);
                Matcher temp = commentR.matcher(in);
                if (temp.find()) {
                    in = in.substring(temp.end());
                    return nextTokenInner();
                }
                while (sc.hasNext()) {
                    in = sc.next();
                    temp = commentR.matcher(in);
                    if (temp.find()) {
                        in = in.substring(temp.end());
                        return nextTokenInner();
                    }
                }
                ret = new Token(Token.EOF);
            } else if (identMatcher.lookingAt()) { //
                if (keyword.containsKey(identMatcher.group()))
                    ret = new Token(keyword.get(identMatcher.group()));
                else
                    ret = new Token(Token.IDT, identMatcher.group());
                in = in.substring(identMatcher.end());
            } else if (numberMatcher.lookingAt()) {
                int number;
                if (numberMatcher.group().startsWith("0x")||numberMatcher.group().startsWith("0X"))
                    number = Integer.parseInt(numberMatcher.group().substring(2), 16);
                else if (numberMatcher.group().startsWith("0"))
                    number = Integer.parseInt(numberMatcher.group(), 8);
                else
                    number = Integer.parseInt(numberMatcher.group(), 10);
                ret = new Token(Token.NUM, String.valueOf(number));
                in = in.substring(numberMatcher.end());
            } else if (separatorMatcher.lookingAt()) {
                ret = new Token(sep.get(separatorMatcher.group()));
                in = in.substring(separatorMatcher.end());
            } else {
                ret = new Token(Token.ERR);
                in = "";
            }
            return ret;
        } else if (sc.hasNext()) {
            in = sc.next();
            return nextTokenInner();
        } else {
            return new Token(Token.EOF);
        }
    }
}
