package AST_nodes;
import Visitors.Visitor;
public class Print_ast_node extends AST_node
{
    public AST_node value;
    public Print_ast_node(AST_node value)
    {
        this.value = value;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
