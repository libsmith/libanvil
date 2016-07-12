package org.libsmith.anvil.collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 29.01.2016 1:08
 * @see <a href="https://en.wikipedia.org/wiki/Shunting-yard_algorithm">Shunting yard algorithm</a>
 */
public class ShuntingYardUtils {

    public static final OperatorNode OPEN_PARENTHESIS   = new ParenthesisOperator("(");
    public static final OperatorNode CLOSED_PARENTHESIS = new ParenthesisOperator(")");

    public interface Node 
    { }

    public interface ValueNode<T> extends Node {
        T getValue();
    }

    public interface OperatorNode extends Node {

        boolean LEFT_ASSOCIATIVE  = false;
        boolean RIGHT_ASSOCIATIVE = true;

        int UNARY_OP  = 1;
        int BINARY_OP = 2;

        boolean isRightAssociative();

        int comparePrecedenceTo(OperatorNode operator);

        int getOperandsCount();

        Node apply(List<Node> operands);
    }

    public static ASTNode infixToAST(List<? extends Node> infixNodeList) {

        Deque<ASTNode> out = new ArrayDeque<>();
        Deque<OperatorNode> stack = new ArrayDeque<>();

        for (Node node : infixNodeList) {
            if (node == OPEN_PARENTHESIS) {
                stack.push((OperatorNode) node);
            }
            else if (node == CLOSED_PARENTHESIS) {
                for (OperatorNode x = stack.peek(); x != null && x != OPEN_PARENTHESIS; x = stack.peek()) {
                    out.push(ASTNode.of(stack.pop(), out));
                }
                if (stack.isEmpty()) {
                    throw new ParseException("Unbalanced right parenthesis");
                }
                stack.pop();
            }
            else if (node instanceof OperatorNode) {
                OperatorNode x = (OperatorNode) node;
                for (OperatorNode y = stack.peek(); y != null && y != OPEN_PARENTHESIS; y = stack.peek()) {
                    if (!y.isRightAssociative() && x.comparePrecedenceTo(y) == 0
                        || x.comparePrecedenceTo(y) < 0) {
                        out.push(ASTNode.of(stack.pop(), out));
                    }
                    else {
                        break;
                    }
                }
                stack.push(x);
            }
            else {
                out.push(ASTNode.of(node));
            }
        }
        while (!stack.isEmpty()) {
            OperatorNode operator = stack.pop();
            if (operator == OPEN_PARENTHESIS) {
                throw new ParseException("Unbalanced left parenthesis");
            }
            out.push(ASTNode.of(operator, out));
        }
        return out.pop();
    }

    public static List<Node> infixToRPN(List<? extends Node> infixNodeList) {
        
        List<Node> out = new ArrayList<>(infixNodeList.size());
        Deque<OperatorNode> stack = new ArrayDeque<>();

        for (Node node : infixNodeList) {
            if (node == OPEN_PARENTHESIS) {
                stack.push((OperatorNode) node);
            }
            else if (node == CLOSED_PARENTHESIS) {
                for (OperatorNode x = stack.peek(); x != null && x != OPEN_PARENTHESIS; x = stack.peek()) {
                    out.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new ParseException("Unbalanced right parenthesis");
                }
                stack.pop();
            }
            else if (node instanceof OperatorNode) {
                OperatorNode x = (OperatorNode) node;
                for (OperatorNode y = stack.peek(); y != null && y != OPEN_PARENTHESIS; y = stack.peek()) {
                    if (!y.isRightAssociative() && x.comparePrecedenceTo(y) == 0
                        || x.comparePrecedenceTo(y) < 0) {
                        out.add(stack.pop());
                    }
                    else {
                        break;
                    }
                }
                stack.push(x);
            }
            else {
                out.add(node);
            }
        }
        while (!stack.isEmpty()) {
            OperatorNode operator = stack.pop();
            if (operator == OPEN_PARENTHESIS) {
                throw new ParseException("Unbalanced left parenthesis");
            }
            out.add(operator);
        }
        return out;
    }

    public static Node evaluateRPN(List<? extends Node> nodes) {
        
        Deque<Node> deque = new ArrayDeque<>();
        for (Node node : nodes) {
            if (node instanceof OperatorNode) {
                OperatorNode operator = (OperatorNode) node;
                Node[] operands = new Node[operator.getOperandsCount()];
                for (int i = operator.getOperandsCount() - 1; i >= 0; i--) {
                    operands[i] = deque.pop();
                }
                node = operator.apply(Arrays.asList(operands));
            }
            deque.push(node);
        }
        if (deque.size() != 1) {
            throw new ParseException("RPN stack has tail size of " + deque.size() + " items");
        }
        return deque.pop();
    }

    public static Node evaluateInfix(List<? extends Node> nodes) {
        return evaluateASTTree(infixToAST(nodes));
    }

    public static Node evaluateASTTree(final ASTNode rootNode) {
        
        Node node = rootNode.getNode();
        if (node instanceof OperatorNode) {
            return ((OperatorNode) node).apply(new AbstractList<Node>() {
                @Override
                public Node get(int index) {
                    return evaluateASTTree(rootNode.getChildes().get(index));
                }

                @Override
                public int size() {
                    return rootNode.getChildes().size();
                }
            });
        }
        return node;
    }

    public static List<ValueNode<Boolean>> evaluateASTPathOfTruth(ASTNode rootNode) {
        
        final List<ValueNode<Boolean>> trueNodes = new ArrayList<>();
        Node node = rootNode.getNode();
        if (node instanceof OperatorNode) {
            final List<ASTNode> childes = rootNode.getChildes();
            final List<ValueNode<Boolean>> trueChildes = new ArrayList<>();
            Node result = ((OperatorNode) node).apply(new AbstractList<Node>() {
                @Override
                public Node get(int index) {
                    ASTNode astNode = childes.get(index);
                    List<ValueNode<Boolean>> result = evaluateASTPathOfTruth(astNode);
                    if (!result.isEmpty()) {
                        trueChildes.addAll(result);
                    }
                    return BooleanNode.valueOf(!result.isEmpty());
                }

                @Override
                public int size() {
                    return childes.size();
                }
            });
            if (BooleanNode.TRUE.equals(result)) {
                if (!trueChildes.isEmpty()) {
                    trueNodes.addAll(trueChildes);
                }
                else {
                    @SuppressWarnings("unchecked")
                    ValueNode<Boolean> booleanResult = (ValueNode<Boolean>) result;
                    trueNodes.add(booleanResult);
                }
            }
        }
        else if (BooleanNode.TRUE.equals(node)) {
            @SuppressWarnings("unchecked")
            ValueNode<Boolean> booleanNode = (ValueNode<Boolean>) node;
            trueNodes.add(booleanNode);
        }
        return trueNodes;
    }

    public static class ParseException extends IllegalArgumentException {
        
        private static final long serialVersionUID = 440617564004078047L;

        public ParseException(String message) {
            super(message);
        }
    }

    @ThreadSafe
    public static class SimpleValueNode<T> implements ValueNode<T> {

        private final T value;

        public SimpleValueNode(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || obj instanceof ValueNode && Objects.equals(((ValueNode) obj).getValue(), value);
        }
    }

    @ThreadSafe
    public static class BooleanNode extends SimpleValueNode<Boolean> {

        public static final BooleanNode TRUE  = new BooleanNode(true);
        public static final BooleanNode FALSE = new BooleanNode(false);
        public static final BooleanNode NULL  = new BooleanNode(null);

        private BooleanNode(Boolean value) {
            super(value);
        }

        public static BooleanNode valueOf(boolean value) {
            return value ? TRUE : FALSE;
        }

        public static BooleanNode valueOf(Boolean value) {
            return value == null ? NULL : value ? TRUE : FALSE;
        }

        public static BooleanNode negate(ValueNode<Boolean> value) {
            return value == null || value.getValue() == null ? null : valueOf(!value.getValue());
        }
    }

    public static class NumberNode<T extends Number> extends ShuntingYardUtils.SimpleValueNode<T> {

        public NumberNode(T value) {
            super(value);
        }

        @Override
        public String toString() {
            return NumberFormat.getInstance().format(getValue());
        }
    }

    @ThreadSafe
    public static class ASTNode {
        private final Node          node;
        private final List<ASTNode> childes;

        private ASTNode(@Nonnull Node node, @Nullable ASTNode[] childes) {
            this.node = node;
            this.childes = childes == null ? Collections.emptyList()
                                           : Collections.unmodifiableList(Arrays.asList(childes));
        }

        public @Nonnull Node getNode() {
            return node;
        }

        public @Nonnull List<ASTNode> getChildes() {
            return childes;
        }

        private static ASTNode of(Node node) {
            return new ASTNode(node, null);
        }

        private static ASTNode of(OperatorNode op, Deque<ASTNode> operandStack) {
            ASTNode[] childes = new ASTNode[op.getOperandsCount()];
            for (int i = op.getOperandsCount() - 1; i >= 0; i--) {
                childes[i] = operandStack.pop();
            }
            return new ASTNode(op, childes);
        }
    }

    public enum BooleanOperatorNode implements ShuntingYardUtils.OperatorNode {
        AND(BINARY_OP, 2, LEFT_ASSOCIATIVE) {
            @Override
            @SuppressWarnings("unchecked")
            public ShuntingYardUtils.Node apply(List<ShuntingYardUtils.Node> operands) {
                boolean a = ((ValueNode<Boolean>) operands.get(0)).getValue();
                if (!a) {
                    return BooleanNode.FALSE;
                }
                boolean b = ((ValueNode<Boolean>) operands.get(1)).getValue();
                return BooleanNode.valueOf(b);
            }
        },
        OR(BINARY_OP, 1, LEFT_ASSOCIATIVE) {
            @Override
            @SuppressWarnings("unchecked")
            public ShuntingYardUtils.Node apply(List<ShuntingYardUtils.Node> operands) {
                boolean a = ((ValueNode<Boolean>) operands.get(0)).getValue();
                if (a) {
                    return BooleanNode.TRUE;
                }
                boolean b = ((ValueNode<Boolean>) operands.get(1)).getValue();
                return BooleanNode.valueOf(b);
            }
        },
        AND_GREEDY(BINARY_OP, 2, LEFT_ASSOCIATIVE) {
            @Override
            @SuppressWarnings("unchecked")
            public ShuntingYardUtils.Node apply(List<ShuntingYardUtils.Node> operands) {
                boolean a = ((ValueNode<Boolean>) operands.get(0)).getValue();
                boolean b = ((ValueNode<Boolean>) operands.get(1)).getValue();
                return BooleanNode.valueOf(a && b);
            }
        },
        OR_GREEDY(BINARY_OP, 1, LEFT_ASSOCIATIVE) {
            @Override
            @SuppressWarnings("unchecked")
            public ShuntingYardUtils.Node apply(List<ShuntingYardUtils.Node> operands) {
                boolean a = ((ValueNode<Boolean>) operands.get(0)).getValue();
                boolean b = ((ValueNode<Boolean>) operands.get(1)).getValue();
                return BooleanNode.valueOf(a || b);
            }
        },
        NOT(UNARY_OP, 3, RIGHT_ASSOCIATIVE) {
            @Override
            @SuppressWarnings("unchecked")
            public ShuntingYardUtils.Node apply(List<ShuntingYardUtils.Node> operands) {
                return BooleanNode.negate(((ValueNode<Boolean>) operands.get(0)));
            }
        };

        private static final float PRECEDENCE_CLASS = 10000;

        private final int     operands;
        private final float   precedence;
        private final boolean rightAssociativity;

        BooleanOperatorNode(int operands, float precedence, boolean rightAssociativity) {
            this.operands = operands;
            this.precedence = PRECEDENCE_CLASS + precedence;
            this.rightAssociativity = rightAssociativity;
        }

        @Override
        public int getOperandsCount() {
            return operands;
        }

        @Override
        public int comparePrecedenceTo(ShuntingYardUtils.OperatorNode other) {
            return other instanceof BooleanOperatorNode
                   ? Float.compare(this.precedence, ((BooleanOperatorNode) other).precedence)
                   : -other.comparePrecedenceTo(this);
        }

        @Override
        public boolean isRightAssociative() {
            return rightAssociativity;
        }
    }

    private static class ParenthesisOperator implements OperatorNode {
        private final String value;

        private ParenthesisOperator(String value) {
            this.value = value;
        }

        @Override
        public int comparePrecedenceTo(OperatorNode operator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRightAssociative() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getOperandsCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Node apply(List<Node> operands) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

