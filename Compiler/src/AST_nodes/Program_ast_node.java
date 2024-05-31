package AST_nodes;
import Visitors.Visitor;
import java.util.Vector;
public class Program_ast_node extends AST_node
{
    // 全局变量List
    public Vector<VariableDeclaration_ast_node> global_variable_declarations_list;
    // 函数定义List
    public Vector<FunctionDeclaration_ast_node> function_declaration_list;
    public Program_ast_node(Vector<VariableDeclaration_ast_node> global_variable_declarations_list,Vector<FunctionDeclaration_ast_node> function_declaration_list)
    {
        this.global_variable_declarations_list = global_variable_declarations_list;
        this.function_declaration_list = function_declaration_list;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}