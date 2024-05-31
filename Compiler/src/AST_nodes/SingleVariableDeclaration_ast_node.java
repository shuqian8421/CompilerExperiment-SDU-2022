package AST_nodes;
import Visitors.Visitor;
import java.util.Vector;
/*单变量声明对应节点*/
public class SingleVariableDeclaration_ast_node extends AST_node
{
    public AST_node type_node;
    public AST_node variable_node;
    public Vector<AST_node> inits;
    public SingleVariableDeclaration_ast_node(AST_node type_node,AST_node variable_node,Vector<AST_node> inits)
    {
        this.type_node = type_node;
        this.variable_node = variable_node;
        this.inits = inits;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
