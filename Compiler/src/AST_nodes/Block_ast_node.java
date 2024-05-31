package AST_nodes;
import Visitors.Visitor;
import java.util.Vector;
public class Block_ast_node extends AST_node
{
    public Vector<AST_node> statements;
    public Block_ast_node(Vector<AST_node> statements)
    {
        this.statements = statements;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
