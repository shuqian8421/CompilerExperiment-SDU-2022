package Visitors;
import AST_nodes.*;
public abstract class Visitor
{
    public abstract void visit(NumLiteral_ast_node node);
    public abstract void visit(Assignment_ast_node node);
    public abstract void visit(Return_ast_node node);
    public abstract void visit(BinaryOperator_ast_node node);
    public abstract void visit(UnaryOperator_ast_node node);
    public abstract void visit(Variable_ast_node node);
    public abstract void visit(BaseType_ast_node node);
    public abstract void visit(Block_ast_node node);
    public abstract void visit(If_ast_node node);
    public abstract void visit(While_ast_node node);
    public abstract void visit(DoWhile_ast_node node);
    public abstract void visit(For_ast_node node);
    public abstract void visit(Break_ast_node node);
    public abstract void visit(Continue_ast_node node);
    public abstract void visit(Print_ast_node node);
    public abstract void visit(SingleVariableDeclaration_ast_node node);
    public abstract void visit(Parameter_ast_node node);
    public abstract void visit(VariableDeclaration_ast_node node);
    public abstract void visit(FunctionDeclaration_ast_node node);
    public abstract void visit(FunctionCall_ast_node node);
    public abstract void visit(Program_ast_node node);
}
