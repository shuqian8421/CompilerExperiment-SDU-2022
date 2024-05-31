package AST_nodes;
public class If_ast_node extends AST_node
{
    public AST_node condition;
    public AST_node then_statement;
    public AST_node else_statement;
    public If_ast_node(AST_node condition,AST_node then_statement,AST_node else_statement)
    {
        this.condition = condition;
        this.then_statement = then_statement;
        this.else_statement = else_statement;
    }
}