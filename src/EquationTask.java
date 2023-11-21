import java.util.Stack;
import java.util.concurrent.Callable;

public class EquationTask implements Callable<Double> {
    private final String equation;

    public EquationTask(String equation) {
        this.equation = equation;
    }

    @Override
    public Double call() {
        try {
            return evaluateExpression(equation);
        } catch (Exception e) {
            e.printStackTrace();

            return Double.NaN;
        }
    }

    private static double evaluateExpression(String expression) {
        String[] tokens = expression.split(" ");
        Stack<Double> values = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (isNumeric(token)) {
                values.push(Double.parseDouble(token));
            } else if ("(".equals(token)) {
                operators.push(token);
            } else if (")".equals(token)) {
                while (!operators.isEmpty() && !"(".equals(operators.peek())) {
                    applyOperator(values, operators);
                }
                operators.pop();
            } else if (isOperator(token)) {
                while (!operators.isEmpty() && hasPrecedence(token, operators.peek())) {
                    applyOperator(values, operators);
                }
                operators.push(token);
            }
        }

        while (!operators.isEmpty()) {
            applyOperator(values, operators);
        }

        if (values.size() != 1 || !operators.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression: " + expression);
        }

        return values.pop();
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isOperator(String str) {
        return "+".equals(str) || "-".equals(str) || "*".equals(str) || "/".equals(str) || "^".equals(str);
    }

    private static boolean hasPrecedence(String op1, String op2) {
        return (op1.equals("^") && !op2.equals("^"))
                || ((op1.equals("*") || op1.equals("/")) && (op2.equals("+") || op2.equals("-")));
    }

    private static void applyOperator(Stack<Double> values, Stack<String> operators) {
        String operator = operators.pop();
        double operand2 = values.pop();
        double operand1 = values.pop();
        double result = performOperation(operand1, operand2, operator);
        values.push(result);
    }

    private static double performOperation(double operand1, double operand2, String operator) {
        switch (operator) {
            case "+":
                return operand1 + operand2;
            case "-":
                return operand1 - operand2;
            case "*":
                return operand1 * operand2;
            case "/":
                if (operand2 != 0) {
                    return operand1 / operand2;
                } else {
                    throw new ArithmeticException("Division by zero");
                }
            case "^":
                return Math.pow(operand1, operand2);
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }
}