package AST_nodes;
import java.util.Vector;
/*变量结点，变量声明和每次变量引用时都对应单独的变量结点，但对应的符号表项只有一个*/
public class Variable_ast_node extends AST_node
{
    public String variable_name;
    //针对数组
    public int size = -1; // 数组长度(声明数组变量时用到)
    public Vector<String> elements = new Vector<>(); //用于记录全局变量初始化数据
    public AST_node index_expression = null;// 数组下标表达式
    public Variable_ast_node(String variable_name)
    {
        this.variable_name = variable_name;
    }
}
