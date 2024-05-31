package AST_nodes;
public class While_ast_node extends AST_node
{
    public AST_node condition;
    public AST_node statement;
    public While_ast_node(AST_node condition,AST_node statement)
    {
        this.condition = condition;
        this.statement = statement;
    }
}
