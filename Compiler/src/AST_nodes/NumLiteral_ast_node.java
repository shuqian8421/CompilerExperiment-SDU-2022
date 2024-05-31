package AST_nodes;
import Tokens.Token;
import Tokens.Token_type;
public class NumLiteral_ast_node extends AST_node
{
    public String literal;
    public NumLiteral_ast_node(Token token)
    {
        literal = token.lexeme;
        if(token.type==Token_type.Token_int_literal)
            type=Node_type.Node_int;
        else if (token.type==Token_type.Token_float_literal)
            type = Node_type.Node_float;
        else if (token.type==Token_type.Token_bool_literal)
            type=Node_type.Node_bool;
        else if (token.type==Token_type.Token_char_literal)
            type=Node_type.Node_char;
    }
}
