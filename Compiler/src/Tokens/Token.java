package Tokens;
public class Token
{
    public Token_type type;
    public String lexeme;
    // 用于定位编译错误在源程序中的位置
    public int line = -1;
    public int column = -1;
    public Token(Token_type type, String lexeme, int line, int column)
    {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }
    public Token(Token_type type, String lexeme)
    {
        this.type = type;
        this.lexeme = lexeme;
    }
}