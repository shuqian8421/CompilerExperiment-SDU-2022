package Symbols;
import AST_nodes.Node_type;
public class Variable_symbol extends Symbol
{
    public int offset;
    public Node_type base_type;
    public int scope_level;
    public Variable_symbol(String symbol_name, Node_type symbol_type,Node_type base_type,int offset,int scope_level)
    {
        super(symbol_name, symbol_type);
        this.base_type = base_type;
        this.offset = offset;
        this.scope_level = scope_level;
    }
}
