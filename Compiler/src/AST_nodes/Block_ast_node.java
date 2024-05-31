package AST_nodes;
import java.util.Vector;
public class Block_ast_node extends AST_node
{
    public Vector<AST_node> statements;
    public Block_ast_node(Vector<AST_node> statements)
    {
        this.statements = statements;
    }
}
