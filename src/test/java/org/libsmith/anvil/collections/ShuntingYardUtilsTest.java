package org.libsmith.anvil.collections;

import org.junit.Test;
import org.libsmith.anvil.AbstractTest;
import org.libsmith.anvil.collections.ShuntingYardUtils.ASTNode;
import org.libsmith.anvil.collections.ShuntingYardUtils.Node;
import org.libsmith.anvil.collections.ShuntingYardUtils.NumberNode;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 29.01.2016 1:27
 */
public class ShuntingYardUtilsTest extends AbstractTest {

    @Test
    public void infixToRPNTest() {
        String infix = "( - 8 + 7 ) * ( 6 / 5 ) ** ( 4 + 3 ) ** 2 + 1";
        String rpn = "8 - 7 + 6 5 / 4 3 + 2 ** ** * 1 +";
        assertEquals(rpn, join(ShuntingYardUtils.infixToRPN(parse(infix))));
    }

    @Test
    public void infixToASTTest() {
        String infix = "( - 8 + 7 ) * ( 6 / 5 ) ** ( 4 + 3 ) ** 2 + 1";
        ASTNode astNode = ShuntingYardUtils.infixToAST(parse(infix));
        StringBuilder sb = new StringBuilder();
        dumpAST(astNode, sb);
        assertEquals("+(*(+(-(8),7),**(/(6,5),**(+(4,3),2))),1)", sb.toString());
    }

    @Test
    public void evaluateInfixTest() {
        List<ShuntingYardUtils.Node> infix = parse("( - 8 + 7 ) * ( 6 / 5 ) ** ( 4 + 3 ) ** 2 + 1");
        NumberNode numberNode = (NumberNode) ShuntingYardUtils.evaluateInfix(infix);
        assertEquals((-8d + 7d) * Math.pow(6d / 5d, Math.pow(4 + 3, 2)) + 1, numberNode.getValue());
    }

    @Test
    public void evaluateRPNTest() {
        List<Node> infix = parse("( - 8 + 7 ) * ( 6 / 5 ) ** ( 4 + 3 ) ** 2 + 1");
        List<Node> rpn = ShuntingYardUtils.infixToRPN(infix);
        NumberNode numberNode = (NumberNode) ShuntingYardUtils.evaluateRPN(rpn);
        assertEquals((-8d + 7d) * Math.pow(6d / 5d, Math.pow(4 + 3, 2)) + 1, numberNode.getValue());
    }

    @Test
    public void evaluateASTTreeTest() {
        List<Node> infix = parse("( - 8 + 7 ) * ( 6 / 5 ) ** ( 4 + 3 ) ** 2 + 1");
        ASTNode ast = ShuntingYardUtils.infixToAST(infix);
        NumberNode numberNode = (NumberNode) ShuntingYardUtils.evaluateASTTree(ast);
        assertEquals((-8d + 7d) * Math.pow(6d / 5d, Math.pow(4 + 3, 2)) + 1, numberNode.getValue());
    }

    @Test
    public void evaluateASTTreeOfBooleansTest() {
        assertEquals(ShuntingYardUtils.BooleanNode.FALSE,
                     ShuntingYardUtils.evaluateASTTree(
                             ShuntingYardUtils.infixToAST(
                                     parseBooleanExpression(
                                             "false || true && false || ( false || true ) && ! true"))));
        assertEquals(ShuntingYardUtils.BooleanNode.TRUE,
                     ShuntingYardUtils.evaluateASTTree(
                             ShuntingYardUtils.infixToAST(
                                     parseBooleanExpression(
                                             "false || true && true || ( false || true ) && ! ! true"))));
    }

    @Test
    public void evaluateASTPathOfTruthTest() {
        assertEquals("e-true f-true true", join(ShuntingYardUtils.evaluateASTPathOfTruth(
                ShuntingYardUtils.infixToAST(
                        parseBooleanExpression(
                                "a-false || b-false && c-true || ( d-false || e-true ) && f-true && ! g-false")))));
        assertEquals("", join(ShuntingYardUtils.evaluateASTPathOfTruth(
                ShuntingYardUtils.infixToAST(
                        parseBooleanExpression(
                                "a-false || b-false && c-true || ( d-false || e-true ) && ! f-true")))));
        assertEquals("b-true c-true d-true e-true", join(ShuntingYardUtils.evaluateASTPathOfTruth(
                ShuntingYardUtils.infixToAST(
                        parseBooleanExpression(
                                "a-false || b-true && c-true && d-true && e-true")))));
    }

    @Test
    public void evaluateGreedyASTPathOfTruthTest() {
        assertEquals("b-true d-true h-true j-true", join(ShuntingYardUtils.evaluateASTPathOfTruth(
                ShuntingYardUtils.infixToAST(
                        parseBooleanExpression(
                                "a-false | b-true | c-false | d-true | e-true && f-false | ( g-false | h-true && j-true )")))));
    }


    @Test(expected = ShuntingYardUtils.ParseException.class)
    public void unbalancedRightTest() {
        ShuntingYardUtils.infixToRPN(parse("( 1 + 2 ) + 3 )"));
    }

    @Test(expected = ShuntingYardUtils.ParseException.class)
    public void unbalancedLeftTest() {
        ShuntingYardUtils.infixToRPN(parse("( 1 + 2 + 3"));
    }

    private static List<Node> parse(String expression) {
        LinkedList<Node> nodes = new LinkedList<>();

        token:
        for (String token : expression.split(" +")) {
            token = token.trim();
            for (OperatorNode operator : OperatorNode.values()) {
                if (token.equals(operator.stringValue)) {
                    if (operator.equals(OperatorNode.SUB) && nodes.peekLast() instanceof ShuntingYardUtils.OperatorNode) {
                        operator = OperatorNode.NEG;
                    }
                    nodes.add(operator);
                    continue token;
                }
            }
            switch (token) {
                case "(":
                    nodes.add(ShuntingYardUtils.OPEN_PARENTHESIS);
                    break;
                case ")":
                    nodes.add(ShuntingYardUtils.CLOSED_PARENTHESIS);
                    break;
                default:
                    nodes.add(new NumberNode<>(Double.parseDouble(token)));
                    break;
            }
        }
        return nodes;
    }

    private static List<Node> parseBooleanExpression(String expression) {
        LinkedList<Node> nodes = new LinkedList<>();
        for (final String token : expression.split(" +")) {
            switch (token) {
                case "&":
                    nodes.add(ShuntingYardUtils.BooleanOperatorNode.AND_GREEDY);
                    break;
                case "&&":
                    nodes.add(ShuntingYardUtils.BooleanOperatorNode.AND);
                    break;
                case "|":
                    nodes.add(ShuntingYardUtils.BooleanOperatorNode.OR_GREEDY);
                    break;
                case "||":
                    nodes.add(ShuntingYardUtils.BooleanOperatorNode.OR);
                    break;
                case "!":
                    nodes.add(ShuntingYardUtils.BooleanOperatorNode.NOT);
                    break;
                case "(":
                    nodes.add(ShuntingYardUtils.OPEN_PARENTHESIS);
                    break;
                case ")":
                    nodes.add(ShuntingYardUtils.CLOSED_PARENTHESIS);
                    break;
                default:
                    nodes.add(new ShuntingYardUtils.ValueNode<Boolean>() {
                        @Override
                        public Boolean getValue() {
                            return token.contains("true");
                        }

                        @Override
                        public String toString() {
                            return token;
                        }
                    });
                    break;
            }
        }
        return nodes;
    }

    private static String join(List<? extends Node> nodes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Node node : nodes) {
            stringBuilder.append(node.toString()).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    private void dumpAST(ASTNode node, StringBuilder sb) {
        sb.append(node.getNode());
        List<ASTNode> childes = node.getChildes();
        if (!childes.isEmpty()) {
            sb.append("(");
            for (int i = 0; i < childes.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                dumpAST(childes.get(i), sb);
            }
            sb.append(")");
        }
    }

    /**
     * @see <a href="http://introcs.cs.princeton.edu/java/11precedence/">Java operator precedence</a>
     */
    @SuppressWarnings("unused")
    enum OperatorNode implements ShuntingYardUtils.OperatorNode {
        ADD("+", 1, BINARY_OP, LEFT_ASSOCIATIVE) {
            @Override
            public double apply(double ... op) {
                return op[0] + op[1];
            }
        },
        SUB("-", 1, BINARY_OP, LEFT_ASSOCIATIVE) {
            @Override
            public double apply(double ... op) {
                return op[0] - op[1];
            }
        },
        MUL("*", 2, BINARY_OP, LEFT_ASSOCIATIVE) {
            @Override
            public double apply(double ... op) {
                return op[0] * op[1];
            }
        },
        DIV("/", 2, BINARY_OP, LEFT_ASSOCIATIVE) {
            @Override
            public double apply(double ... op) {
                return op[0] / op[1];
            }
        },
        REM("%", 2, BINARY_OP, LEFT_ASSOCIATIVE) {
            @Override
            public double apply(double ... op) {
                return op[0] / op[1];
            }
        },
        POW("**", 3, BINARY_OP, RIGHT_ASSOCIATIVE) {
            @Override
            public double apply(double ... op) {
                return Math.pow(op[0], op[1]);
            }
        },
        NEG("-", 3, UNARY_OP, RIGHT_ASSOCIATIVE) {
            @Override
            public double apply(double ... op) {
                return -op[0];
            }
        };

        private final String stringValue;
        private final int operands;
        private final float precedence;
        private final boolean rightAssociativity;

        OperatorNode(String stringValue, float precedence, int operands, boolean rightAssociativity) {
            this.precedence = precedence;
            this.operands = operands;
            this.rightAssociativity = rightAssociativity;
            this.stringValue = stringValue;
        }

        @Override
        public Node apply(List<Node> operands) {
            double[] ops = new double[operands.size()];
            for (int i = 0; i < operands.size(); i++) {
                @SuppressWarnings("unchecked")
                NumberNode<Number> numberNode = (NumberNode<Number>) operands.get(i);
                ops[i] = numberNode.getValue().doubleValue();
            }
            return new NumberNode<>(apply(ops));
        }

        public abstract double apply(double ... operands);

        @Override
        public int comparePrecedenceTo(ShuntingYardUtils.OperatorNode operator) {
            return Float.compare(this.precedence, ((OperatorNode) operator).precedence);
        }

        @Override
        public boolean isRightAssociative() {
            return rightAssociativity;
        }

        @Override
        public int getOperandsCount() {
            return operands;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }
}

@SuppressWarnings("unused")
class BiNode<T> {
    private final T value;
    private final BiNode<T> left;
    private final BiNode<T> right;

    BiNode(T value, BiNode<T> left, BiNode<T> right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public static BiNode<Node> of(ASTNode astNode) {
        List<ASTNode> childes = astNode.getChildes();
        BiNode<Node> left = childes.size() > 0 ? of(childes.get(0)) : null;
        BiNode<Node> right = childes.size() > 1 ? of(childes.get(1)) : null;
        return new BiNode<>(astNode.getNode(), left, right);
    }

    public void printTree(PrintStream out) {
        if (right != null) {
            right.printTree(out, true, "");
        }
        printNodeValue(out);
        if (left != null) {
            left.printTree(out, false, "");
        }
    }
    private void printNodeValue(PrintStream out) {
        if (value == null) {
            out.print("<null>");
        } else {
            out.print(value.toString());
        }
        out.print('\n');
    }

    private void printTree(PrintStream out, boolean isRight, String indent) {
        if (right != null) {
            right.printTree(out, true, indent + (isRight ? "        " : " |      "));
        }
        out.print(indent);
        if (isRight) {
            out.print(" ┌");
        } else {
            out.print(" └");
        }
        out.print("----- ");
        printNodeValue(out);
        if (left != null) {
            left.printTree(out, false, indent + (isRight ? " |      " : "        "));
        }
    }
}
