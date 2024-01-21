import java.io.InputStream;
import java.io.IOException;


class TernaryEvaluator {
    private final InputStream in;

    private int lookahead;

    public TernaryEvaluator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private int evalDigit(int c) {
        return c - '0';
    }

    public int eval() throws IOException, ParseError {
        int value = exp();

        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();

        return value;
    }

    private int exp() throws IOException, ParseError {

        return exptail(term());

    }

    private int term() throws IOException, ParseError {
        return termtail(factor());
    }


    private int factor() throws IOException, ParseError {
        int cond=0;
        if (isDigit(lookahead)) {
            //while(isDigit(lookahead)){
                cond = 10*cond+evalDigit(lookahead);
                consume(lookahead);
            //}
            return cond;
        }
        else if(lookahead =='('){
            consume('(');
            cond=exp();
            consume(')');
            return cond;
        }

        throw new ParseError();
    }


    private int exptail(int condition) throws IOException, ParseError {
        int left,right;
        switch (lookahead) {
            case '^':
                consume('^');
                left = condition;
                right = exptail(term());
                return left^right;
            case -1:
            case '\n':
            case ':':
            case ')':
                return condition;
        }

        throw new ParseError();
    }


private int termtail(int condition) throws IOException, ParseError {
    int left,right;
    switch (lookahead) {
        case '&':
            consume('&');
            left = condition;
            right = termtail(factor());
            return left&right;
        case -1:
        case '\n':
        case ':':
        case ')':
        case '^':
            return condition;
    }

    throw new ParseError();
}
}
