package AST_nodes;
import Visitors.Visitor;
public class Continue_ast_node extends AST_node
{
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
