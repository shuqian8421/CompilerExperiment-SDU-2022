package Symbols;
import AST_nodes.Node_type;
public class Symbol
{
    public String name;
    public Node_type type;
    public Symbol(String symbol_name,Node_type symbol_type)
    {
        this.name = symbol_name;
        this.type = symbol_type;
    }
}