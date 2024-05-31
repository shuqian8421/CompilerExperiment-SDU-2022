package AST_nodes;
import Symbols.Variable_symbol;
import Visitors.Visitor;
public class Parameter_ast_node extends AST_node
{
    public AST_node parameter_type; //参数类型结点
    public AST_node parameter_variable; //参数名称结点
    public Variable_symbol symbol;
    public Parameter_ast_node(AST_node parameter_type,AST_node parameter_variable)
    {
        this.parameter_type = parameter_type;
        this.parameter_variable = parameter_variable;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
