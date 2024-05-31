package AST_nodes;
import Visitors.Visitor;
public class DoWhile_ast_node extends AST_node
{
    public AST_node statement;
    public AST_node condition;
    public DoWhile_ast_node(AST_node statement,AST_node condition)
    {
        this.statement = statement;
        this.condition = condition;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
