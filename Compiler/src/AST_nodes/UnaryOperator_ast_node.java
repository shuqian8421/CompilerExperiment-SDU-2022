package AST_nodes;
import Tokens.Token_type;
import Visitors.Visitor;
public class UnaryOperator_ast_node extends AST_node
{
    public Token_type op;
    public AST_node right;
    public UnaryOperator_ast_node(Token_type op, AST_node right)
    {
        this.op = op;
        this.right = right;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
