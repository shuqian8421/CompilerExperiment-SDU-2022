package AST_nodes;
import java.util.Vector;
public class FunctionDeclaration_ast_node extends AST_node
{
    public AST_node function_type;// 函数返回值类型结点
    public String function_name;// 函数名
    public Vector<AST_node> formal_parameters; // 形参列表
    public AST_node function_block;// 函数体
    public int offset = -1;// 偏移量
    public FunctionDeclaration_ast_node(AST_node function_type,String function_name,Vector<AST_node> formal_parameters,AST_node function_block)
    {
        this.function_type = function_type;
        this.function_name = function_name;
        this.formal_parameters = formal_parameters;
        this.function_block = function_block;
    }
}