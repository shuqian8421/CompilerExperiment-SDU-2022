package AST_nodes;
public class Return_ast_node extends AST_node
{
    public AST_node expression; //返回值表达式
    public String function_name; //是哪个函数体里的return
    public Return_ast_node(AST_node expression)
    {
        this.expression = expression;
    }
}
