package AST_nodes;
import Visitors.Visitor;
public class Assignment_ast_node extends AST_node
{
    public AST_node left;
    public String operation;//此处必然是=
    public AST_node right;
    public Assignment_ast_node(AST_node left,String operation,AST_node right)
    {
        this.left = left;
        this.operation = operation;
        this.right = right;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
