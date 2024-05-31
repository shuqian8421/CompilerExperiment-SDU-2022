package AST_nodes;

import Visitors.Visitor;

/*变量声明时的类型或函数定义中返回值的类型所对应的结点*/
public class BaseType_ast_node extends AST_node
{
    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
