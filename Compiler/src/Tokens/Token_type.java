package Tokens;
/*词法分析部分*/
public enum Token_type
{
    Token_int_literal,              // [0-9]+
    Token_float_literal,            // [0-9]+.[0-9]+
    Token_bool_literal,             // true or false
    Token_char_literal,             // 'a'
    Token_id,                       // identifier

    //关键字
    Token_int,                      // int
    Token_float,                    // float
    Token_bool,                     // bool
    Token_void,                     // void
    Token_char,                     // char
    Token_return,                   // return
    Token_if,                       // if
    Token_else,                     // else
    Token_while,                    // while
    Token_do,                       // do
    Token_for,                      // for
    Token_break,                    // break
    Token_continue,                 // continue
    Token_print,                    // print
    Token_const,                    // const

    Token_plus,                     // '+'
    Token_minus,                    // '-'
    Token_multiply_or_dereference,  // '*'
    Token_divide,                   // '/'
    Token_address,                  // '&'
    Token_not,                      // '!'
    Token_left_parenthesis,         // '('
    Token_right_parenthesis,        // ')'
    Token_left_brace,               // '{'
    Token_right_brace,              // '}'
    Token_left_bracket,             // '['
    Token_right_bracket,            // ']'
    Token_assign,                   // '='
    Token_comma,                    // ','
    Token_semicolon,                // ';'

    //比较运算符
    Token_and,                      // '&&'
    Token_or,                       // '||'
    Token_equal,                    // '=='
    Token_not_equal,                // '!='
    Token_less_than,                // '<'
    Token_less_equal,               // '<='
    Token_greater_than,             // '>'
    Token_greater_equal,            // '>='

    Token_eof(-1);                // 用于标识字符流的结尾
    public int value;
    Token_type(){}
    Token_type(int value)
    {
        this.value = value;
    }
}