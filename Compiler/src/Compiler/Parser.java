package Compiler;

import Tokens.*;
import AST_nodes.*;

import java.util.Vector;

public class Parser
{

    private static Token current_token;
    private static Vector<Token> token_list;
    private static int pos =0;//Token流指针
    private static Program_ast_node entry_node = null;
    //防止实例化
    private Parser(){}
    private static void eat_current_token()
    {
        current_token = token_list.elementAt(pos++);
    }
    private static void rollback()
    {
        current_token = token_list.elementAt((--pos)-1);
    }
    // primary := num_literal
    //              | "(" expression ")"
    //              | identifier func_args?
    //              | identifier "[" expression "]"
    // func_args = "(" (expression ("," expression)*)? ")"
    private static AST_node primary()
    {
        // num_literal
        if(current_token.type == Token_type.Token_int_literal ||
                current_token.type == Token_type.Token_float_literal ||
                current_token.type == Token_type.Token_bool_literal ||
                current_token.type == Token_type.Token_char_literal)
        {
            var node = new NumLiteral_ast_node(current_token);
            eat_current_token();
            return node;
        }

        // "(" expression ")"
        if(current_token.type == Token_type.Token_left_parenthesis)
        {
            eat_current_token();//吃"("
            var node = expression();
            if(current_token.type == Token_type.Token_right_parenthesis)
                eat_current_token();//吃")"
            return node;
        }

        // identifier func_args?
        //            | "[" expression "]"
        // func_args = "(" (expression ("," expression)*)? ")"
        if(current_token.type == Token_type.Token_id)
        {
            String name = current_token.lexeme; // 函数或变量名称
            eat_current_token();
            // 函数调用
            if(current_token.type == Token_type.Token_left_parenthesis)
            {
                eat_current_token();//吃"("
                Vector<AST_node> actual_parameter_nodes = new Vector<>();
                while (current_token.type != Token_type.Token_right_parenthesis)
                {
                    var parameter_node = expression();
                    actual_parameter_nodes.add(parameter_node);
                    if(current_token.type == Token_type.Token_comma)
                        eat_current_token();//吃","
                }
                eat_current_token();//吃")"
                return new FunctionCall_ast_node(name,actual_parameter_nodes);
            }
            // 变量
            var variable_node = new Variable_ast_node(name);
            // 变量1：数组元素
            if(current_token.type == Token_type.Token_left_bracket)
            {
                eat_current_token(); //吃"["
                variable_node.index_expression = expression();
                variable_node.type = Node_type.Node_array;
                eat_current_token(); //吃"]"
            }
            // 变量2：基本变量
            return variable_node;
        }
        //错误！
        System.err.println("error!:"+current_token.lexeme);
        System.exit(1);
        return null;
    }
    // unary :=	(“+” | “-” | “!” | “*” | “&”) unary | primary
    private static AST_node unary()
    {
        if(current_token.type == Token_type.Token_plus ||
                current_token.type == Token_type.Token_minus ||
                current_token.type == Token_type.Token_not ||
                current_token.type == Token_type.Token_multiply_or_dereference ||
                current_token.type == Token_type.Token_address)
        {
            Token token = current_token;
            eat_current_token();
            return new UnaryOperator_ast_node(token.type,unary());
        }
        else
            return primary();
    }
    // mul_div := unary ("*" unary | "/" unary)*
    private static AST_node mul_div()
    {
        AST_node node = unary();
        while(true)
        {
            if(current_token.type == Token_type.Token_multiply_or_dereference || current_token.type == Token_type.Token_divide)
            {
                Token operator_token = current_token;
                eat_current_token();
                var left = node;
                node = new BinaryOperator_ast_node(left,operator_token.type,unary());
                continue;
            }
            return node;
        }
    }
    // add_sub  :=  mul_div ("+" mul_div | "-" mul_div)*
    private static AST_node add_sub()
    {
        var node = mul_div();
        while(true)
        {
            if(current_token.type == Token_type.Token_plus || current_token.type == Token_type.Token_minus)
            {
                Token operator_token = current_token;
                eat_current_token();
                var left = node;
                node = new BinaryOperator_ast_node(left,operator_token.type,mul_div());
                continue;
            }
            return node;
        }
    }
    // relational  :=  add_sub ("<" add_sub | "<=" add_sub | ">" add_sub | ">=" add_sub)*
    private static AST_node relational()
    {
        var node = add_sub();
        while(true)
        {
            if(current_token.type == Token_type.Token_less_than ||
                    current_token.type == Token_type.Token_less_equal ||
                    current_token.type == Token_type.Token_greater_than ||
                    current_token.type == Token_type.Token_greater_equal)
            {
                Token operator_token = current_token;
                eat_current_token();
                var left = node;
                node = new BinaryOperator_ast_node(left,operator_token.type,add_sub());
                continue;
            }
            return node;
        }
    }
    // equality  :=  relational ("==" relational | "!=" relational)*
    private static AST_node equality()
    {
        var node = relational();
        while(true)
        {
            if(current_token.type == Token_type.Token_equal || current_token.type == Token_type.Token_not_equal)
            {
                Token operator_token = current_token;
                eat_current_token();
                var left = node;
                node = new BinaryOperator_ast_node(left,operator_token.type,relational());
                continue;
            }
            return node;
        }
    }
    // logic  :=  equality ("&&" equality | "||" equality)*
    private static AST_node logic()
    {
        var node = equality();
        while(true)
        {
            if(current_token.type == Token_type.Token_and || current_token.type == Token_type.Token_or)
            {
                Token operator_token = current_token;
                eat_current_token();
                var left = node;
                node = new BinaryOperator_ast_node(left,operator_token.type,equality());
                continue;
            }
            return node;
        }
    }
    // expression  :=  logic ("=" expression)?
    private static AST_node expression()
    {
        var node = logic();
        if(current_token.type == Token_type.Token_assign)
        {
            Token assign_token = current_token;
            eat_current_token();
            var left = node;
            node = new Assignment_ast_node(left,assign_token.lexeme,expression());
        }
        return node;
    }
    // expression_statement  :=  expression? ";"
    private static AST_node expression_statement()
    {
        AST_node node = null;
        if(current_token.type == Token_type.Token_semicolon)
            eat_current_token();
        else
        {
            node = expression();
            if(current_token.type == Token_type.Token_semicolon)
                eat_current_token();
            else
            {
                System.err.println("error:"+current_token.lexeme);
                System.exit(1);
            }
        }
        return node;
    }
    // declarator  := identifier type-suffix
    // type-suffix  :=  ϵ | ("[" num_literal "]")?
    private static AST_node declarator()
    {
        if(current_token.type == Token_type.Token_id)
        {
            var node = new Variable_ast_node(current_token.lexeme);
            eat_current_token();
            if(current_token.type == Token_type.Token_left_bracket)
            {
                eat_current_token();//吃"["
                node.size = Integer.parseInt(current_token.lexeme);
                node.type = Node_type.Node_array;
                eat_current_token();//吃size
                eat_current_token();//吃"]"
            }
            return node;
        }
        return null;
    }
    // type_specification := "int" | "float" | "bool" | "void" | "char"
    private static AST_node type_specification()
    {
        var node = new BaseType_ast_node();
        switch (current_token.type) {
            case Token_int -> node.type = Node_type.Node_int;
            case Token_float -> node.type = Node_type.Node_float;
            case Token_char -> node.type = Node_type.Node_char;
            case Token_void -> node.type = Node_type.Node_void;
            case Token_bool -> node.type = Node_type.Node_bool;
            default -> {}
        }
        eat_current_token();
        return node;
    }
    // variable_declaration	 :=  type_specification declarator ("=" expression)? ("," declarator ("=" expression)?)* ";"
    //                         | type_specification declarator ("=" "{" (expression)? ("," expression)* "}")? ("," declarator ("=" expression)?)* ";"
    private static AST_node variable_declaration()
    {
        Vector<AST_node> variable_declarations = new Vector<>();
        BaseType_ast_node baseType_ast_node = (BaseType_ast_node) type_specification();
        while(current_token.type != Token_type.Token_semicolon)
        {
            var variable_node = (Variable_ast_node) declarator();
            // 学习chibicc的做法，将变量初始化视为一个(或多个，对数组)赋值语句
            Vector<AST_node> init_nodes = new Vector<>();
            if(current_token.type == Token_type.Token_assign)
            {
                eat_current_token();//吃"="
                if(current_token.type == Token_type.Token_left_brace)
                {
                    //数组初始化
                    eat_current_token();//吃"{"
                    int i = 0;// 数组的下标从0开始
                    while(current_token.type != Token_type.Token_right_brace)
                    {
                        //new_variable_node是赋值语句左边的数组元素对应的变量
                        assert variable_node != null;
                        var new_variable_node = new Variable_ast_node(variable_node.variable_name);
                        new_variable_node.type = Node_type.Node_array;
                        new_variable_node.baseType = baseType_ast_node.type;
                        // 构建一个与数组元素下标对应的表达式，即primary := identifier "[" expression "]"中的expression.
                        Token token = new Token(Token_type.Token_int_literal,String.valueOf(i),0,0);
                        var index_node = new NumLiteral_ast_node(token);
                        index_node.type = Node_type.Node_int;
                        new_variable_node.index_expression = index_node;
                        init_nodes.add(new Assignment_ast_node(new_variable_node,"=",expression()));
                        if(current_token.type == Token_type.Token_comma)
                            eat_current_token();//吃","
                        i++;
                    }
                    eat_current_token();//吃"}"
                }
                else
                {
                    // 普通变量，即基本类型变量初始化
                    init_nodes.add(new Assignment_ast_node(variable_node,"=",expression()));
                }
            }
            var node = new SingleVariableDeclaration_ast_node(baseType_ast_node,variable_node,init_nodes);
            variable_declarations.add(node);
            if(current_token.type == Token_type.Token_comma)
                eat_current_token();//吃","
        }
        eat_current_token();//吃";"
        return new VariableDeclaration_ast_node(variable_declarations);
    }
    // statement  := expression_statement
    //                   | block
    //                   | variable_declaration
    //                   | "return" expression-statement
    //                   | "if" "(" expression ")" statement ("else" statement)?
    //                   | "while" "(" expression ")" statement
    //                   | "do" statement "while" "(" expression ")" ";"
    //                   | for" "(" expression? ";" expression ";" expression? ")" statement
    //                   | "break" ";"
    //                   | "continue" ";"
    //                   | "print" "(" expression ")" ";"
    private static AST_node statement()
    {
        //block
        if(current_token.type == Token_type.Token_left_brace)
            return block();

        //variable_declaration
        if(current_token.type == Token_type.Token_int ||
                current_token.type == Token_type.Token_float ||
                current_token.type == Token_type.Token_bool ||
                current_token.type == Token_type.Token_char)
            return variable_declaration();

        //"return" expression-statement
        if(current_token.type == Token_type.Token_return)
        {
            eat_current_token();//吃"return"
            return new Return_ast_node(expression_statement());
        }

        //"if" "(" expression ")" statement ("else" statement)?
        if(current_token.type == Token_type.Token_if)
        {
            eat_current_token();//吃"if"
            AST_node condition = null,then_statement,else_statement = null;
            if(current_token.type == Token_type.Token_left_parenthesis)
            {
                eat_current_token();//吃"("
                condition = expression();
                if(current_token.type == Token_type.Token_right_parenthesis)
                    eat_current_token();//吃")"
            }
            then_statement = statement();
            if(current_token.type == Token_type.Token_else)
            {
                eat_current_token();//吃"else"
                else_statement = statement();
            }
            return new If_ast_node(condition,then_statement,else_statement);
        }

        // "while" "(" expression ")" statement
        if(current_token.type == Token_type.Token_while)
        {
            eat_current_token();//吃"while"
            AST_node condition = null,statement;
            if(current_token.type == Token_type.Token_left_parenthesis)
            {
                eat_current_token();//吃"("
                condition = expression();
                if(current_token.type == Token_type.Token_right_parenthesis)
                    eat_current_token();//吃")"
            }
            statement = statement();
            return new While_ast_node(condition,statement);
        }

        // "do" statement "while" "(" expression ")" ";"
        if(current_token.type == Token_type.Token_do)
        {
            eat_current_token();//吃"do"
            AST_node statement,condition = null;
            statement = statement();
            if(current_token.type == Token_type.Token_while)
            {
                eat_current_token();//吃"while"
                eat_current_token();//吃"("
                condition = expression();
                eat_current_token();//吃")"
                eat_current_token();//吃";"
            }
            return new DoWhile_ast_node(statement,condition);
        }

        // "for" "(" expression? ";" expression ";" expression? ")" statement
        if(current_token.type == Token_type.Token_for)
        {
            eat_current_token();//吃"for"
            AST_node initialization = null,condition = null,increment = null,statement;
            if(current_token.type == Token_type.Token_left_parenthesis)
            {
                eat_current_token();//吃"("
                if(current_token.type != Token_type.Token_semicolon)
                    initialization = expression();
                eat_current_token();//吃";"
                condition = expression();
                eat_current_token();//吃";"
                if(current_token.type != Token_type.Token_right_parenthesis)
                    increment = expression();
                eat_current_token();//吃")"
            }
            statement = statement();
            return new For_ast_node(initialization,condition,increment,statement);
        }

        // "break" ";"
        if(current_token.type == Token_type.Token_break)
        {
            eat_current_token();//吃"break"
            var break_node = new Break_ast_node();
            eat_current_token();//吃";"
            return break_node;
        }

        // "continue" ";"
        if(current_token.type == Token_type.Token_continue)
        {
            eat_current_token();//吃"continue"
            var continue_node = new Continue_ast_node();
            eat_current_token();//吃";"
            return continue_node;
        }

        //                   | "print" "(" expression ")" ";"
        if(current_token.type ==Token_type.Token_print)
        {
            eat_current_token();//吃"print"
            eat_current_token();//吃"("
            var value_node = expression();
            eat_current_token();//吃")“
            eat_current_token();//吃”;“
            return new Print_ast_node(value_node);
        }

        // expression_statement
        return expression_statement();
    }
    // block  := "{" statement* "}"
    private static AST_node block()
    {
        Vector<AST_node> new_statements = new Vector<>();
        eat_current_token();//吃"{"
        //构建new_statements
        while(current_token.type != Token_type.Token_right_brace && current_token.type != Token_type.Token_eof)
        {
            AST_node new_statement = statement();
            if(new_statement != null)
                new_statements.add(new_statement);
        }
        AST_node node = new Block_ast_node(new_statements);
        eat_current_token();//吃"}"
        return node;
    }

    // formal_parameter  :=  type_specification identifier
    private static AST_node formal_parameter()
    {
        BaseType_ast_node type_ast_node = (BaseType_ast_node) type_specification();
        if(current_token.type ==Token_type.Token_id)
        {
            var variable_node = new Variable_ast_node(current_token.lexeme);
            eat_current_token();//吃参数名
            return new Parameter_ast_node(type_ast_node,variable_node);
        }
        else return null;
    }
    // function_declaration  :=  type_specification identifier "(" formal_parameters? ")" block
// formal_parameters  :=  formal_parameter ("," formal_parameter)*
    private static AST_node function_declaration()
    {
        // 函数类型
        BaseType_ast_node type_ast_node = (BaseType_ast_node) type_specification();

        // 函数名称
        String function_name = null;
        if(current_token.type == Token_type.Token_id)
        {
            function_name = current_token.lexeme;
            eat_current_token();
        }
        // 函数形参列表
        if(current_token.type == Token_type.Token_left_parenthesis)
            eat_current_token();//吃"("
        else
        {
            System.err.println("missing (");
            System.exit(1);
        }
        Vector<AST_node> formal_parameters = new Vector<>();
        if(current_token.type != Token_type.Token_right_parenthesis)
        {
            while (current_token.type != Token_type.Token_comma && current_token.type != Token_type.Token_right_parenthesis)
            {
                var new_formal_parameter = formal_parameter();
                formal_parameters.add(new_formal_parameter);
                if(current_token.type ==Token_type.Token_comma)
                    eat_current_token();
            }
        }
        if(current_token.type == Token_type.Token_right_parenthesis)
            eat_current_token();//吃")"
        // 函数body
        var body = block();
        return new FunctionDeclaration_ast_node(type_ast_node,function_name,formal_parameters,body);
    }

    // program  :=  (variable_declaration | function_declaration)*
    private static Program_ast_node program()
    {
        Vector<VariableDeclaration_ast_node> variableDeclaration_list = new Vector<>();
        Vector<FunctionDeclaration_ast_node> functionDeclaration_list = new Vector<>();
        while(current_token.type != Token_type.Token_eof)
        {
            if(current_token.type ==Token_type.Token_int ||
                    current_token.type == Token_type.Token_float ||
                    current_token.type == Token_type.Token_bool ||
                    current_token.type ==Token_type.Token_void ||
                    current_token.type == Token_type.Token_char)
            {
                eat_current_token();
                if(current_token.type == Token_type.Token_id)
                {
                    eat_current_token();
                    if(current_token.type == Token_type.Token_left_parenthesis)
                    {
                        rollback();
                        rollback();
                        var new_function = (FunctionDeclaration_ast_node) function_declaration();
                        functionDeclaration_list.add(new_function);
                    }
                    else
                    {
                        rollback();
                        rollback();
                        var new_variable_declarations = (VariableDeclaration_ast_node) variable_declaration();
                        variableDeclaration_list.add(new_variable_declarations);
                    }
                }
            }
        }
        return new Program_ast_node(variableDeclaration_list,functionDeclaration_list);
    }
    /******************** CFG (c-like)************************************
     * program  :=  (variable_declaration | function_declaration)*
     * function_declaration  :=  type_specification identifier "(" formal_parameters? ")" block
     * formal_parameters  :=  formal_parameter ("," formal_parameter)*
     * formal_parameter  :=  type_specification identifier
     * block  :=  "{" statement* "}"
     * statement  := expression_statement
     *                   | block
     *                   | variable_declaration
     *                   | "return" expression-statement
     *                   | "if" "(" expression ")" statement ("else" statement)?
     *                   | "while" "(" expression ")" statement
     *                   | "do" statement "while" "(" expression ")" ";"
     *                   | for" "(" expression? ";" expression ";" expression? ")" statement
     *                   | "break" ";"
     *                   | "continue" ";"
     *                   | "print" "(" expression ")" ";"
     * variable_declaration	 :=  type_specification declarator ("=" expression)? ("," declarator ("=" expression)?)* ";"
     *                         | type_specification declarator ("=" "{" (expression)? ("," expression)* "}")? ("," declarator ("=" expression)?)* ";"
     * type_specification  :=  "int" | "float" | "bool" | "void" | "char"
     * declarator  :=  identifier type-suffix
     * type-suffix  :=  ϵ | ("[" num_literal "]")?
     * expression_statement  :=  expression? ";"
     * expression  :=  logic ("=" expression)?
     * logic  :=  equality ("&&" equality | "||" equality)*
     * equality  :=  relational ("==" relational | "!=" relational)*
     * relational  :=  add_sub ("<" add_sub | "<=" add_sub | ">" add_sub | ">=" add_sub)*
     * add_sub  :=  mul_div ("+" mul_div | "-" mul_div)*
     * mul_div  :=  unary ("*" unary | "/" unary)*
     * unary :=	(“+” | “-” | “!” | “*” | “&”) unary | primary
     * primary  :=  num_literal | "(" expression ")" | identifier func_args? | identifier "[" expression "]"
     * func_args = "(" (expression ("," expression)*)? ")"
     *
     ********************/
    public static void parse(Vector<Token> token_list)
    {
        pos = 0;
        Parser.token_list = token_list;
        current_token = token_list.get(pos++);
        entry_node = program();
    }
    public static Program_ast_node get_entry_node()
    {
        return entry_node;
    }

}