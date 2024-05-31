package AST_nodes;
import Tokens.Token_type;
import Visitors.Visitor;
public class BinaryOperator_ast_node extends AST_node
{
    public AST_node left;
    public Token_type operator;
    public AST_node right;
    public BinaryOperator_ast_node(AST_node left, Token_type operator, AST_node right)
    {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}