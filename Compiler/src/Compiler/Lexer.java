package Compiler;
import Tokens.Token;
import Tokens.Token_type;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
/*词法分析器*/
public class Lexer
{
    private static int pos = 0;// 字符流指针
    private static int line = 1;
    private static int column = 0;
    private static String input; // 待分析的输入字符流
    private static Vector<Token> token_list = new Vector<>(); // token流，词法分析结果存放于此变量
    // 私有化构造函数，防止实例化
    private Lexer(){}
    //对输入进行词法分析
    public static void analyse(String input)
    {
        pos = 0;
        line = 1;
        column = 0;
        Lexer.input = input;
        token_list.clear();
        token_list = Lexer.construct_token_stream();
    }
    // 读取当前字符，并将指针指向下一个字符
    private static char getChar()
    {
        try
        {
            column++;
            return input.charAt(pos++);
        }
        catch (IndexOutOfBoundsException e) {// input越界异常，说明读到文件末尾读过头了
            throw new IndexOutOfBoundsException();
        }
    }
    // 回退一个字符
    private static void rollback()
    {
        pos--;
        column--;
    }

    /**打印错误信息
     */
    private static void print_err(int line,int column,String message)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        String err_line = null;
        try
        {
            for(int i=0;i<line;i++)
                err_line = reader.readLine();
        }
        catch (Exception e)
        {
            System.err.println("报错信息读入出错！");
        }
        System.err.println("Error at "+line+":"+(column+1));
        System.err.println(err_line);
        for(int i=0;i<column;i++)
            System.err.print(" ");
        System.err.println("^ "+ message);
    }
    /** 得到下一个token
     * 读取字符流直至分析出一个token，并将该token返回
     */
    private static Token get_next_token()
    {
        char current_char;
        boolean error_when_block_annotation = false; //非法块注释，开始时默认没有
        try
        {
            current_char = getChar();
            // 忽略空白
            while(Character.isWhitespace(current_char))
            {
                if(current_char == '\n')
                {
                    line++;
                    column = 1;
                }
                current_char = getChar();
            }
            // 忽略注释
            while(current_char == '/')//'/'可能是除运算符，也可能是行注释的开头
            {
                if((current_char = getChar()) == '/')//是行注释
                {
                    do
                    {
                        current_char = getChar();
                    }
                    while(current_char != '\n' && input.length() >= pos);
                }
                else if(current_char == '*')//是块注释
                {
                    // 有别于参考代码，对块注释末尾的“*/”字符流词法分析逻辑重新写了
                    error_when_block_annotation = true;//开始分析块注释，先假设是非法的
                    //只要没到字符流末尾，就一直读
                    while(input.length() > pos)
                    {
                        current_char = getChar();
                        // 读到换行符时换行
                        if(current_char == '\n')
                        {
                            line++;
                            column = 1;
                        }
                        else if(current_char == '*')
                        {
                            if(getChar() == '/')
                            {
                                //合法了
                                error_when_block_annotation = false;
                                break;
                            }
                            // 下一次循环还要读入一个字符，所以先退还一个字符
                            rollback();
                        }
                    }
                    //注释块是有效的，继续进行以后的操作
                    current_char = getChar();
                }
                // 既不是//，也不是/*，那肯定是除法，返回除法token
                else
                {
                    rollback();// 多读了除法右边一个字符，应该退还回去
                    return new Token(Token_type.Token_divide,String.valueOf(current_char),line,column);
                }
                // 忽略注释后的空白符
                while(Character.isWhitespace(current_char))
                {
                    if(current_char == '\n' || current_char == '\r')
                    {
                        line++;
                        column = 1;
                    }
                    current_char = getChar();
                }
            }
            // 整数或浮点数
            if(Character.isDigit(current_char) || current_char == '.')
            {
                // 有可能是数值
                StringBuilder number_string = new StringBuilder();
                // 浮点数: .[0-9]+
                if(current_char == '.')
                {
                    do
                    {
                        number_string.append(current_char);
                        current_char = getChar();
                    }
                    while(Character.isDigit(current_char));
                    rollback();
                    return new Token(Token_type.Token_float_literal,number_string.toString(),line,column);
                }
                // 数值开头: [0-9]+
                else
                {
                    do
                    {
                        number_string.append(current_char);
                        current_char = getChar();
                    }
                    while(Character.isDigit(current_char));
                    // 是浮点数: [0-9]+.[0-9]+
                    if(current_char == '.')
                    {
                        do
                        {
                            number_string.append(current_char);
                            current_char = getChar();
                        }
                        while(Character.isDigit(current_char));
                        rollback();
                        return new Token(Token_type.Token_float_literal,number_string.toString(),line,column);
                    }
                    // 是整数: [0-9]+
                    else
                    {
                        rollback();
                        return new Token(Token_type.Token_int_literal,number_string.toString(),line,column);
                    }
                }
            }
            // 标识符或关键字
            if(Character.isAlphabetic(current_char) || current_char == '_')
            {
                StringBuilder identifier_string = new StringBuilder();
                do
                {
                    identifier_string.append(current_char);
                    current_char = getChar();
                }
                while(Character.isLetterOrDigit(current_char) || current_char == '_');
                rollback();
                //关键字
                return switch (identifier_string.toString()) {
                    case "int" -> new Token(Token_type.Token_int, identifier_string.toString(), line, column);
                    case "float" -> new Token(Token_type.Token_float, identifier_string.toString(), line, column);
                    case "bool" -> new Token(Token_type.Token_bool, identifier_string.toString(), line, column);
                    case "void" -> new Token(Token_type.Token_void, identifier_string.toString(), line, column);
                    case "char" -> new Token(Token_type.Token_char, identifier_string.toString(), line, column);
                    case "true", "false" ->
                            new Token(Token_type.Token_bool_literal, identifier_string.toString(), line, column);
                    case "return" -> new Token(Token_type.Token_return, identifier_string.toString(), line, column);
                    case "if" -> new Token(Token_type.Token_if, identifier_string.toString(), line, column);
                    case "else" -> new Token(Token_type.Token_else, identifier_string.toString(), line, column);
                    case "while" -> new Token(Token_type.Token_while, identifier_string.toString(), line, column);
                    case "do" -> new Token(Token_type.Token_do, identifier_string.toString(), line, column);
                    case "for" -> new Token(Token_type.Token_for, identifier_string.toString(), line, column);
                    case "break" -> new Token(Token_type.Token_break, identifier_string.toString(), line, column);
                    case "continue" -> new Token(Token_type.Token_continue, identifier_string.toString(), line, column);
                    case "print" -> new Token(Token_type.Token_print, identifier_string.toString(), line, column);
                    case "const" -> new Token(Token_type.Token_const, identifier_string.toString(), line, column);
                    default ->
                        // 普通标识符
                            new Token(Token_type.Token_id, identifier_string.toString(), line, column);
                };
            }
            // 运算符或分隔符
            switch (current_char) {
                case '+' -> {
                    return new Token(Token_type.Token_plus, String.valueOf(current_char), line, column);
                }
                case '-' -> {
                    return new Token(Token_type.Token_minus, String.valueOf(current_char), line, column);
                }
                case '*' -> {
                    return new Token(Token_type.Token_multiply_or_dereference, String.valueOf(current_char), line, column);
                }
                // 这里没有除法是因为已经在分析块注释的部分处理了
                case '(' -> {
                    return new Token(Token_type.Token_left_parenthesis, String.valueOf(current_char), line, column);
                }
                case ')' -> {
                    return new Token(Token_type.Token_right_parenthesis, String.valueOf(current_char), line, column);
                }
                case '[' -> {
                    return new Token(Token_type.Token_left_bracket, String.valueOf(current_char), line, column);
                }
                case ']' -> {
                    return new Token(Token_type.Token_right_bracket, String.valueOf(current_char), line, column);
                }
                case '{' -> {
                    return new Token(Token_type.Token_left_brace, String.valueOf(current_char), line, column);
                }
                case '}' -> {
                    return new Token(Token_type.Token_right_brace, String.valueOf(current_char), line, column);
                }
                case '&' -> {
                    String lexeme = String.valueOf(current_char);
                    current_char = getChar();
                    if (current_char == '&')
                        return new Token(Token_type.Token_and, lexeme, line, column);
                    else {
                        rollback();
                        return new Token(Token_type.Token_address, lexeme, line, column);
                    }
                }
                case '|' -> {
                    String lexeme = String.valueOf(current_char);
                    current_char = getChar();
                    if (current_char == '|')
                        return new Token(Token_type.Token_or, lexeme, line, column);
                    else
                        rollback();
                }
                case '=' -> {
                    String lexeme = String.valueOf(current_char);
                    current_char = getChar();
                    if (current_char == '=')
                        return new Token(Token_type.Token_equal, lexeme, line, column);
                    else
                        rollback();
                    return new Token(Token_type.Token_assign, lexeme, line, column);
                }
                case '!' -> {
                    String lexeme = String.valueOf(current_char);
                    current_char = getChar();
                    if (current_char == '=')
                        return new Token(Token_type.Token_not_equal, lexeme, line, column);
                    else
                        rollback();
                    return new Token(Token_type.Token_not, lexeme, line, column);
                }
                case '<' -> {
                    String lexeme = String.valueOf(current_char);
                    current_char = getChar();
                    if (current_char == '=')
                        return new Token(Token_type.Token_less_equal, lexeme, line, column);
                    else
                        rollback();
                    return new Token(Token_type.Token_less_than, lexeme, line, column);
                }
                case '>' -> {
                    String lexeme = String.valueOf(current_char);
                    current_char = getChar();
                    if (current_char == '=')
                        return new Token(Token_type.Token_greater_equal, lexeme, line, column);
                    else
                        rollback();
                    return new Token(Token_type.Token_greater_than, lexeme, line, column);
                }
                case ',' -> {
                    return new Token(Token_type.Token_comma, String.valueOf(current_char), line, column);
                }
                case ';' -> {
                    return new Token(Token_type.Token_semicolon, String.valueOf(current_char), line, column);
                }
                case '\'' -> {
                    if ((current_char = getChar()) == '\0')
                        print_err(line, column, "unclosed char literal");
                    char c;
                    if (current_char == '\\') {
                        current_char = getChar();
                        switch (current_char) {
                            case 'b' -> c = '\b';
                            case 't' -> c = '\t';
                            case 'n' -> c = '\n';
                            case 'f' -> c = '\f';
                            case 'r' -> c = '\r';
                            default -> c = current_char;
                        }
                    } else
                        c = current_char;
                    if (getChar() != '\'')
                        print_err(line, column, "unclosed char literal");
                    return new Token(Token_type.Token_char_literal, String.valueOf(c), line, column);
                }
                default -> {
                }
            }
            // 若不是，报错
            System.err.println(current_char+"is an invalid token");
            System.exit(1);
            return null;
        }
        catch (IndexOutOfBoundsException e)//读到文件末尾读过头了
        {
            column--;
            pos--;
            if(error_when_block_annotation)//这种情况是非法块注释情况
            {
                print_err(line,column,"invalid annotation block");
                System.exit(1);
            }
            return new Token(Token_type.Token_eof,"EOF");
        }
    }
    /** 构建整个token流
     */
    public static Vector<Token> construct_token_stream()
    {
        Token current_token;
        do
        {
            current_token = get_next_token();
            token_list.add(current_token);
        }
        while(current_token.type != Token_type.Token_eof);
        return token_list;
    }

    public static Vector<Token> get_token_list()
    {
        return  token_list;
    }
}