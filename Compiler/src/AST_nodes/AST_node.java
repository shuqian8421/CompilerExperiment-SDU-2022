package AST_nodes;

import Visitors.Visitor;

/*各种节点类的基类*/
public abstract class AST_node
{
    public Node_type type = Node_type.Node_void;
    public Node_type baseType = Node_type.Node_void;
    public abstract void accept(Visitor v);
}