package AST_nodes;
public class Print_ast_node extends AST_node
{
    public AST_node value;
    public Print_ast_node(AST_node value)
    {
        this.value = value;
    }
}
