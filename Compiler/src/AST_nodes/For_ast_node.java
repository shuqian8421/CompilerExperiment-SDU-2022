package AST_nodes;
import Visitors.Visitor;
public class For_ast_node extends AST_node
{
    public AST_node initialization;
    public AST_node condition;
    public AST_node increment;
    public AST_node statement;
    public For_ast_node(AST_node initialization,AST_node condition,AST_node increment,AST_node statement)
    {
        this.initialization = initialization;
        this.condition = condition;
        this.increment = increment;
        this.statement = statement;
    }
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
