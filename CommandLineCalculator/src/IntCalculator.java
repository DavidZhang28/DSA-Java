import java.util.Arrays;
import java.util.HashMap;

/**
 * Command line calculator that works with only integers.
 * The command line argument must be put in quotes, as in:
 * java IntCalculator "(2 x 3) ^ 2"
 * @author Brian S. Borowski, Palash Sharma, Ken Kaurov
 * @version 1.2   October 3, 2025 Fix parsing bug
 *          1.1.1 October 1, 2024 Added support for sqrt, abs, and lg functions
 *          1.0   October 5, 2022 Original release
 */
public class IntCalculator {
    public static final HashMap<Character, Integer>
            binaryOperatorPrecedenceMap = new HashMap<>(),
            unaryOperatorPrecedenceMap = new HashMap<>(),
            functionPrecedenceMap = new HashMap<>();
    public static final HashMap<Character, String> functionStringMap = new HashMap<>();
    public static final double LOG2 = Math.log(2);

    /*
     * userExpression is the infix expression entered by the user.
     * infixExpression is similar to the user expression, except that each
     * negation minus sign has been replaced with a ~ to avoid ambiguity in
     * the conversion and evaluation algorithms.
     */
    private String infixExpression, userExpression, postfixExpression;
    private Error error;

    /*
     * Operators and their precedences. Operators with higher precedences are
     * evaluated first.
     */
    static {
        functionPrecedenceMap.put('s', 5);
        functionPrecedenceMap.put('a', 5);
        functionPrecedenceMap.put('l', 5);

        unaryOperatorPrecedenceMap.put('~', 4);
        binaryOperatorPrecedenceMap.put('^', 3);
        binaryOperatorPrecedenceMap.put('x', 2);
        binaryOperatorPrecedenceMap.put('/', 2);
        binaryOperatorPrecedenceMap.put('%', 2);
        binaryOperatorPrecedenceMap.put('+', 1);
        binaryOperatorPrecedenceMap.put('-', 1);

        functionStringMap.put('s', "sqrt");
        functionStringMap.put('a', "abs");
        functionStringMap.put('l', "lg");
    }

    /**
     * Creates an instance of an integer calculator.
     * @param expression the infix expression supplied by the user
     */
    public IntCalculator(String expression) {
        setExpression(expression);
    }

    /**
     * Sets the expression instance variables after parsing the characters in
     * input expression. If an invalid symbol is discovered, the internal
     * Error object is set accordingly.
     * @param expression the infix expression supplied by the user
     */
    public void setExpression(String expression) {
        error = null;
        boolean leading = true;
        StringBuilder internalBuilder = new StringBuilder(),
                externalBuilder = new StringBuilder();
        for (int i = 0, len = expression.length(); i < len; i++) {
            char symbol = expression.charAt(i);
            boolean validSymbol = true;
            if (isWhiteSpace(symbol)) {
                internalBuilder.append(symbol);
                externalBuilder.append(symbol);
                continue;
            }
            if (!isValid(symbol)) {
                validSymbol = false;
                if (error == null) {
                    error = new Error(getErrorHeader(i) + "Unexpected symbol '"
                            + symbol + "' found at position " + (i + 1) + ".",
                            0, i);
                }
            } else if (functionStringMap.containsKey(symbol)) {
                String functionString = functionStringMap.get(symbol);
                if (expression.startsWith(functionString, i)) {
                    i += functionString.length() - 1;
                } else {
                    validSymbol = false;
                    if (error == null) {
                        error = new Error(getErrorHeader(i) + "Unexpected symbol starting with '"
                                + symbol + "' found at position " + (i + 1) + ".",
                                0, i);
                    }
                }
            }
            char newSymbol = symbol;
            if (leading && symbol == '-') {
                newSymbol = '~';
            }
            leading =  (symbol == '(' || isUnencodedUnaryOperator(symbol)
                    || isBinaryOperator(symbol));
            internalBuilder.append(newSymbol);
            if (validSymbol && functionStringMap.containsKey(symbol)) {
                externalBuilder.append(functionStringMap.get(symbol));
            } else {
                externalBuilder.append(symbol);
            }
        }
        this.infixExpression = internalBuilder.toString();
        this.userExpression = externalBuilder.toString();
    }

    /**
     * Returns the precedence of the given operator.
     * @param operator the operator of which to find the precedence
     * @return the precedence of the given operator
     */
    public static int precedence(char operator) {
        Integer val = binaryOperatorPrecedenceMap.get(operator);
        if (val == null) {
            val = unaryOperatorPrecedenceMap.get(operator);
        }
        if (val == null) {
            val = functionPrecedenceMap.get(operator);
        }
        return val != null ? val : -1;
    }

    /**
     * Returns true if the symbol is valid; false otherwise.
     * @param symbol the symbol to check
     * @return true if the symbol is valid
     */
    public static boolean isValid(char symbol) {
        return isBinaryOperator(symbol) ||
                (symbol != '~' && isUnaryOperator(symbol)) ||
                isFunction(symbol) ||
                isDigit(symbol) ||
                isParenthesis(symbol);
    }

    /**
     * Returns true if the symbol is a binary operator; false otherwise.
     * @param symbol the symbol to check
     * @return true if the symbol is a binary operator
     */
    public static boolean isBinaryOperator(char symbol) {
        return binaryOperatorPrecedenceMap.containsKey(symbol);
    }

    /**
     * Returns true if the symbol is an unencoded unary operator -; false
     * otherwise.
     * @param symbol the symbol to check
     * @return true if the symbol is an unencoded unary operator
     */
    public static boolean isUnencodedUnaryOperator(char symbol) {
        return symbol == '-';
    }

    /**
     * Returns true if the symbol is a unary operator; false otherwise. ~ is
     * the only unary operator.
     * @param symbol the character to evaluate
     * @return true if the symbol is a unary operator
     */
    public static boolean isUnaryOperator(char symbol) {
        return unaryOperatorPrecedenceMap.containsKey(symbol);
    }

    /**
     * Returns true if the symbol is a function; false otherwise. s and a are the
     * only functions.
     * @param symbol the character to evaluate
     * @return true if the symbol is a function
     */
    public static boolean isFunction(char symbol) {
        return functionPrecedenceMap.containsKey(symbol);
    }

    /**
     * Returns true if the symbol is a digit 0 through 9; false otherwise.
     * @param symbol the character to evaluate
     * @return true if the symbol is a digit
     */
    public static boolean isDigit(char symbol) {
        return symbol >= 48 && symbol <= 57;
    }

    /**
     * Returns true if the symbol is whitespace; false otherwise. Whitespace
     * characters include space, tab, and new line characters.
     * @param symbol the character to evaluate
     * @return true if the symbol is a whitespace character
     */
    public static boolean isWhiteSpace(char symbol) {
        return symbol == ' ' || symbol == '\t' || symbol == '\n';
    }

    /**
     * Returns true if the symbol is an opening or closing parenthesis; false
     * otherwise.
     * @param symbol the character to evaluate
     * @return true if the symbol is an opening or closing parenthesis
     */
    public static boolean isParenthesis(char symbol) {
        return symbol == '(' || symbol == ')';
    }

    /**
     * Returns true if the symbol is left associative; false otherwise.
     * @param symbol the character to evaluate
     * @return true if the symbol is left associative
     */
    public static boolean isLeftAssociative(char symbol) {
        // Only ^ is right associative. All other symbols are left associative.
        return symbol != '^';
    }

    /**
     * Returns a String of spaces followed by a caret and a space, so that the
     * caret points to the first erroneous character in the
     * expression.
     * @param numSpaces the number of spaces before the caret
     * @return a String of spaces followed by a caret and a space
     */
    public static String getErrorHeader(int numSpaces) {
        char[] charArray = new char[numSpaces];
        Arrays.fill(charArray, ' ');
        return new String(charArray) + "^ ";
    }

    /**
     * Returns true if instance variable 'infixExpression' is valid; false
     * otherwise. At this point, all characters in the expression are known to
     * be valid, but the expression itself may not be well-formed.
     * @return true if the infix expression is valid
     * @throws StackException if an error occurs when calling a method on the
     * stack. This should not happen. The throws clause is there so that you
     * don't need any try-catch blocks in the body of this method.
     */
    public boolean containsValidExpression() throws StackException {
        if (error != null) {
            return false;
        }
        MyStack<Symbol> stack = new MyArrayList<>();
        boolean leading = true,
                operandFound = false,
                digitsFound = false,
                functionFound = false;
        int len = infixExpression.length(), originalI;
        for (int i = originalI = 0; i < len; i++, originalI++) {
            char symbol = infixExpression.charAt(i);
            if (isWhiteSpace(symbol)) {
                if (digitsFound) {
                    operandFound = true;
                    digitsFound = false;
                }
                continue;
            }
            boolean isBinaryOperator = isBinaryOperator(symbol),
                    isFunction = isFunction(symbol),
                    isOperand = isDigit(symbol);
            if (isOperand) {
                if (operandFound) {
                    error = new Error(getErrorHeader(originalI - 1)
                            + "Expected operator at position " + originalI + ".",
                            0, originalI - 1);
                    return false;
                }
                digitsFound = true;
            } else if (isBinaryOperator) {
                operandFound = false;
                digitsFound = false;
            }
            if (functionFound && symbol != '(') {
                error = new Error(getErrorHeader(originalI)
                        + "Expected '(', but found '" + symbol
                        + "' at position " + (originalI + 1) + ".", 0, originalI);
                return false;
            }
            else if (symbol == ')' || isBinaryOperator) {
                if (leading) {
                    error = new Error(getErrorHeader(originalI)
                            + "Expected operand, but found '" + symbol
                            + "' at position " + (originalI + 1) + ".", 0, originalI);
                    return false;
                }
            } else if (!isOperand && !leading) {
                error = new Error(getErrorHeader(originalI)
                        + "Expected operator, but found '" + symbol
                        + "' at position " + (originalI + 1) + ".", 0, originalI);
                return false;
            }
            if (isFunction) {
                functionFound = true;
            }
            if (symbol == '(') {
                stack.push(new Symbol(symbol, 0, originalI));
                functionFound = false;
            } else if (symbol == ')') {
                if (stack.isEmpty()) {
                    error = new Error(getErrorHeader(originalI)
                            + "Unmatched ')' found at position "
                            + (originalI + 1) + ".", 0, originalI);
                    return false;
                }
                stack.pop();
                operandFound = true;
            }
            leading = isBinaryOperator || isUnaryOperator(symbol) ||
                    isFunction(symbol) || symbol == '(';
            if (functionStringMap.containsKey(symbol)) {
                originalI += functionStringMap.get(symbol).length() - 1;
            }
        }
        if (functionFound) {
            error = new Error(getErrorHeader(originalI)
                    + "Missing '(' at position " + (originalI + 1) + ".", 0, originalI);
            return false;
        }
        if (leading) {
            error = new Error(getErrorHeader(originalI)
                    + "Missing operand at position " + (originalI + 1) + ".", 0,
                    originalI);
            return false;
        }
        if (!stack.isEmpty()) {
            Symbol stackTop = stack.pop();
            error = new Error(getErrorHeader(stackTop.position)
                    + "Unmatched '(' found at position "
                    + (stackTop.position + 1) + ".", 0,stackTop.position);
            return false;
        }
        return true;
    }

    /**
     * Converts the infix expression stored in the instance variable
     * 'infixExpression' into postfix, storing the result in instance variable
     * 'postfixExpression'. Each symbol in the postfix expression is
     * separated by a space.
     * @return a string containing the postfix expression
     * @throws StackException if an error occurs when calling a method on the
     * stack. This should not happen. The throws clause is there so that you
     * don't need any try-catch blocks in the body of this method.
     */
    public String infixToPostfix() throws StackException {
        MyStack<Character> stack = new MyArrayList<>();
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < infixExpression.length(); i++)
        {
            char c = infixExpression.charAt(i);
            if (isDigit(c))
            {
                output.append(c);
                while (i + 1 < infixExpression.length() && (isDigit(infixExpression.charAt(i+1)) || infixExpression.charAt(i+1) == ' '))
                {
                    if (infixExpression.charAt(i+1) == ' ')
                    {
                        i++;
                        continue;
                    }
                    output.append(infixExpression.charAt(i+1));
                    i++;
                }
                output.append(' ');
            }
            else if (c == '(')
                stack.push(c);
            else if (c == ')')
            {
                while (!stack.isEmpty() && stack.peek() != '(')
                {
                    output.append(stack.pop()).append(' ');
                }
                stack.pop();
            }
            else if (isUnaryOperator(c))
            {
                stack.push(c);
            }
            else if (isBinaryOperator(c))
            {
                if (c == '^')
                {
                    while (!stack.isEmpty() && precedence(c) < precedence(stack.peek()))
                    {
                        output.append(stack.pop()).append(' ');
                    }
                    stack.push(c);
                    continue;
                }
                while (!stack.isEmpty() && precedence(c) <= precedence(stack.peek()))
                {
                    output.append(stack.pop()).append(' ');
                }
                stack.push(c);
            }
            else if (isFunction(c))
            {
                while (!stack.isEmpty() && precedence(c) <= precedence(stack.peek()))
                {
                    output.append(stack.pop()).append(' ');
                }
                stack.push(c);
                while (infixExpression.charAt(i+1) != '(')
                {
                    i++;
                }
            }
        }
        while (!stack.isEmpty())
        {
            output.append(stack.pop()).append(' ');
        }
        this.postfixExpression = output.toString().trim();
        return this.postfixExpression;
    }

    /**
     * Evaluates the postfix expression and returns the integer value of the
     * expression. All operations are performed with integers.
     * @return the integer value that results after evaluating the postfix
     * expression
     * @throws StackException if an error occurs when calling a method on the
     * stack. This should not happen. The throws clause is there so that you
     * don't need any try-catch blocks in the body of this method.
     * @throws IllegalArgumentException if an attempt to divide or mod by zero
     * is encountered. The message of the exception reads:
     * "Cannot evaluate expression, division by zero."
     * An IllegalArgumentException is thrown if the user attempts to compute
     * 0^0. The message of the exception reads:
     * "Cannot evaluate expression, 0^0 is undefined."
     * An IllegalArgumentException is thrown if the user attempts to compute the
     * lg (log base 2) of a number <= 0. The message of the exception reads:
     * "Cannot evaluate lg of a non-positive number."
     * An IllegalArgumentException is thrown if the user attempts to compute the
     * sqrt (square root) of a number < 0. The message of the exception reads:
     * "Cannot evaluate sqrt of a negative number."
     */
    public int evaluatePostfix() throws StackException, IllegalArgumentException 
    {
        MyStack<Integer> stack = new MyArrayList<>();
        for (int i = 0; i < postfixExpression.length(); i++)
        {
            char c = postfixExpression.charAt(i);
            if (isDigit(c))
            {
                //run a while loop here to get the full number. You can do this by saying
                int num = c - '0';
                while (i + 1 < postfixExpression.length() &&isDigit(postfixExpression.charAt(i+1)))
                {
                    num = num * 10 + (postfixExpression.charAt(i+1) - '0');
                    i++;
                }
                stack.push(num);
            }
            else if (isUnaryOperator(c))
            {
                stack.push(stack.pop() * -1);
            }
            else if (isBinaryOperator(c))
            {
                int second = stack.pop();
                int first = stack.pop();
                if (c == '+')
                    stack.push(first + second);
                else if (c == '-')
                    stack.push(first - second);
                else if (c == 'x')
                    stack.push(first * second);
                else if (c == '/')
                {
                    if (second == 0)
                        throw new IllegalArgumentException("Cannot evaluate expression, division by zero.");
                    stack.push(first / second);
                }
                else if (c == '%')
                {
                    if (second == 0)
                        throw new IllegalArgumentException("Cannot evaluate expression, division by zero.");
                    stack.push(first % second);
                }
                else if (c == '^')
                {
                    if (first ==0 && second == 0)
                        throw new IllegalArgumentException("Cannot evaluate expression, 0^0 is undefined.");
                    stack.push((int)(Math.pow(first, second)));
                }
            }
            else if (isFunction(c))
            {
                int val = stack.pop();
                if (c == 's')
                {
                    stack.push((int)(SquareRoot.sqrt(val, SquareRoot.EPSILON)));
                }
                else if (c == 'a')
                {
                    stack.push(Math.abs(val));
                }
                else if (c == 'l')
                {
                    if (val <= 0)
                        throw new IllegalArgumentException("Cannot evaluate lg of a non-positive number.");
                    stack.push((int)(Math.log(val) / LOG2));
                }
            }
        }
        if (!stack.isEmpty())
            return stack.pop();
        return 1;
    }

    /**
     * Returns the internal error message, if one exists.
     * @return the internal error message
     */
    public String getErrorMessage() {
        return error == null ? "No errors found." : error.message;
    }

    /**
     * Returns the infix expression supplied by the user.
     * @return the infix expression supplied by the user
     */
    public String getExpression() {
        return userExpression;
    }

    public static void main(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg);
        }
        String input = builder.toString().trim();
        if (input.isEmpty()) {
            System.err.println("Usage: java IntCalculator <expression>");
            System.exit(1);
        }
        IntCalculator calc = new IntCalculator(input);

        try {
            if (calc.containsValidExpression()) {
                String postfix = calc.infixToPostfix();
                System.out.println("Postfix expression: " + postfix);
                System.out.println("Evaluation:         "
                        + calc.evaluatePostfix());
            } else {
                System.err.println(calc.getExpression());
                System.err.println(calc.getErrorMessage());
            }
        } catch (Exception e) {
            System.err.println("Error:              " + e.getMessage());
        }
    }
}

class Symbol {
    char character;
    int lineNumber, position;

    Symbol(char character, int lineNumber, int position) {
        this.character = character;
        this.lineNumber = lineNumber;
        this.position = position;
    }
}

class Error {
    int lineNumber, position;
    String message;

    Error(String message, int lineNumber, int position) {
        this.message = message;
        this.lineNumber = lineNumber;
        this.position = position;
    }
}