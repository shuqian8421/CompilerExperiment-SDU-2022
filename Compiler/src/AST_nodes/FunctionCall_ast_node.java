package AST_nodes;
import java.util.Vector;
public class FunctionCall_ast_node extends AST_node
{
    public String function_name;//函数名
    public Vector<AST_node> arguments;// 实参列表
    public FunctionCall_ast_node(String function_name,Vector<AST_node> arguments)
    {
        this.function_name = function_name;
        this.arguments = arguments;
    }
}
