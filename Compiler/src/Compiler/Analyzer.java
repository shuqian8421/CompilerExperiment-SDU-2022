package Compiler;
import AST_nodes.*;
import Symbols.Function_symbol;
import Symbols.Scoped_symbol_table;
import Symbols.Symbol;
import Symbols.Variable_symbol;
import Visitors.Visitor;
import java.util.TreeMap;
import java.util.Vector;
public class Analyzer
{
    private Analyzer(){}
    private static Program_ast_node node;
    public static void analyze(Program_ast_node node)
    {
        Analyzer.node = node;
        //semantic analyze
        Semantic_analyzer s = new Semantic_analyzer();
        s.visit(Analyzer.node);
    }
    static class Semantic_analyzer extends Visitor {
        public static Vector<Variable_ast_node> globals = new Vector<>();
        private static int offset_sum = 0;
        private static Scoped_symbol_table current_scope;
        private static Scoped_symbol_table global_scope;
        private static void print_err(String message)
        {
            System.err.println("error: "+message);
        }

        @Override
        public void visit(NumLiteral_ast_node node)
        {
            node.baseType = node.type;
        }

        @Override
        public void visit(Assignment_ast_node node)
        {
            node.left.accept(this);
            node.right.accept(this);
            node.type = node.left.type;
            node.baseType = node.left.baseType;
        }

        @Override
        public void visit(Return_ast_node node)
        {
            Scoped_symbol_table temp_scope = current_scope;
            while(temp_scope != null)
            {
                if(!temp_scope.category.equals("function"))
                    temp_scope = temp_scope.enclosing_scope;
                else
                {
                    node.function_name = temp_scope.name;
                    break;
                }
            }
            //case: "return ;"
            if(node.expression ==null)
                node.baseType = Node_type.Node_void;
            else
            {
                node.expression.accept(this);
                node.baseType = node.expression.baseType;
            }
            // 检查return语句的类型是否与函数返回值类型一致，不一致的话，则需要类型转换.
            // 首先获取函数的符号表项.
            assert current_scope != null;
            Symbol function_symbol = current_scope.lookup(node.function_name,false);
            if(function_symbol == null)
            {
                System.err.println("something wrong! info about function "+node.function_name+" is missing");
                System.exit(1);
            }
            // 类型不一致，则为用户提供warning信息，并记录实际返回值类型.
            if(node.baseType != function_symbol.type)
                System.err.println("Warning: type returned does not match the type of function \""+node.function_name+"\".");
            // 类型转换也可以放在visit BinaryOperation的时候进行.
            // 下面这种不一致有点严重，退出.
            if(node.baseType == Node_type.Node_void && function_symbol.type != Node_type.Node_void)
            {
                System.err.println("return type of "+node.function_name+" should not be void!");
                System.exit(1);
            }
        }

        @Override
        public void visit(BinaryOperator_ast_node node)
        {
            node.left.accept(this);
            node.right.accept(this);
            // 检查运算数是否兼容.
            Node_type left_type = node.left.baseType;
            Node_type right_type = node.right.baseType;
            // 若兼容，取大，即bool < char < int < float
            node.baseType = (left_type.compareTo(right_type) > 0) ? left_type : right_type;

        }

        @Override
        public void visit(UnaryOperator_ast_node node)
        {
            node.right.accept(this);
            node.baseType = node.right.baseType;
        }

        @Override
        public void visit(Variable_ast_node node)
        {
            //用python版本
            var variable_symbol = (Variable_symbol)current_scope.lookup(node.variable_name,false);
            if(variable_symbol == null)
            {
                System.err.println("something wrong with "+node.variable_name);
                System.exit(1);
            }
            node.type =variable_symbol.type;
            node.baseType = variable_symbol.base_type;
            if(node.symbol == null)
                node.symbol =  variable_symbol;
            if(node.index_expression != null)
                node.index_expression.accept(this);
        }

        @Override
        public void visit(BaseType_ast_node node)
        {
            node.baseType = node.type;
        }

        @Override
        public void visit(Block_ast_node node)
        {
            //创建block scope symbol_table.
            TreeMap<String,Symbol> symbols  = new TreeMap<>();
            var scope = new Scoped_symbol_table(symbols,1,null);
            scope.level = current_scope.level + 1;
            current_scope.child_no_count++;
            scope.name = current_scope.name + "." + current_scope.child_no_count;
            scope.category = "block";
            scope.enclosing_scope = current_scope;
            current_scope.sub_scopes.add(scope);
            current_scope = scope;
            if(!node.statements.isEmpty())
                for(var n:node.statements)
                    n.accept(this);
            current_scope = current_scope.enclosing_scope;
        }

        @Override
        public void visit(If_ast_node node)
        {
            if(node.condition != null)
                node.condition.accept(this);
            if(node.then_statement != null)
                node.then_statement.accept(this);
            if(node.else_statement != null)
                node.else_statement.accept(this);
        }

        @Override
        public void visit(While_ast_node node)
        {
            if(node.condition != null)
                node.condition.accept(this);
            if(node.statement != null)
                node.statement.accept(this);
        }

        @Override
        public void visit(DoWhile_ast_node node)
        {
            if(node.statement != null)
                node.statement.accept(this);
            if(node.condition != null)
            {
                node.condition.accept(this);
                if(node.condition.baseType != Node_type.Node_bool)
                    print_err("Condition in do-while should be a bool value!");
            }
            else
                print_err("do-while statement has a null condition!");
        }

        @Override
        public void visit(For_ast_node node)
        {
            if(node.initialization != null)
                node.initialization.accept(this);
            node.condition.accept(this);
            if(node.increment != null)
                node.increment.accept(this);
            node.statement.accept(this);
        }

        @Override
        public void visit(Break_ast_node node) {}

        @Override
        public void visit(Continue_ast_node node) {}

        @Override
        public void visit(Print_ast_node node)
        {
            if(node.value != null)
                node.value.accept(this);
        }

        @Override
        public void visit(SingleVariableDeclaration_ast_node node)
        {
            node.type_node.accept(this);
            BaseType_ast_node baseType_ast_node = (BaseType_ast_node) node.type_node;
            Variable_ast_node variable_ast_node = (Variable_ast_node) node.variable_node;
            if(variable_ast_node.type == Node_type.Node_array)
            {
                node.baseType = variable_ast_node.baseType = baseType_ast_node.type;
                node.type = Node_type.Node_array;
            }
            else
                node.type = node.baseType = variable_ast_node.type = variable_ast_node.baseType = baseType_ast_node.type;
            Variable_symbol variable_symbol;
            if(current_scope.lookup(variable_ast_node.variable_name,true) == null)
            {
                // 在符号表中找不到，即尚未定义.
                if(current_scope.level == 0)
                {
                    //全局变量
                    variable_symbol = new Variable_symbol(variable_ast_node.variable_name, variable_ast_node.type,variable_ast_node.baseType,0,current_scope.level);
                    for(var element: node.inits)
                    {
                        String init_value_literal = ((NumLiteral_ast_node)((Assignment_ast_node)element).right).literal;
                        variable_ast_node.elements.add(init_value_literal);
                    }
                    globals.add(variable_ast_node);
                }
                else
                {
                    // 局部变量
                    if(variable_ast_node.type != Node_type.Node_array)
                    {
                        // 非数组
                        if(baseType_ast_node.type == Node_type.Node_char)
                            offset_sum += 1;
                        else
                            offset_sum += 8;
                    }
                    else
                    {
                        // 数组
                        if(baseType_ast_node.type == Node_type.Node_int || baseType_ast_node.type == Node_type.Node_float)
                            offset_sum = offset_sum + variable_ast_node.size * 8;
                    }
                    variable_symbol = new Variable_symbol(variable_ast_node.variable_name,variable_ast_node.type,baseType_ast_node.type,-offset_sum,current_scope.level);
                }
                variable_ast_node.symbol = variable_symbol;
                current_scope.insert(variable_symbol);
                variable_ast_node.symbol = variable_symbol;
            }
            else
            {
                System.err.println(variable_ast_node.variable_name+" is declared more than one time.");
                System.exit(1);
            }
            //初始化
            for(var init:node.inits)
                if(init != null)
                    init.accept(this);
        }

        @Override
        public void visit(Parameter_ast_node node)
        {
            node.parameter_type.accept(this);
            node.type = node.baseType = node.parameter_type.type;
            BaseType_ast_node parameter_type_node = (BaseType_ast_node) node.parameter_type;
            Variable_ast_node parameter_variable_node = (Variable_ast_node) node.parameter_variable;
            offset_sum += 8;
            var sym = new Variable_symbol(parameter_variable_node.variable_name,parameter_type_node.type,parameter_type_node.type,-offset_sum,current_scope.level);
            current_scope.insert(sym);
            node.symbol = sym;
        }

        @Override
        public void visit(VariableDeclaration_ast_node node)
        {
            if(!node.variable_declarations.isEmpty())
                for(var n:node.variable_declarations)
                    n.accept(this);
        }

        @Override
        public void visit(FunctionDeclaration_ast_node node)
        {
            offset_sum = 0;// 为每个函数初始化offsetSum.

            node.function_type.accept(this);
            var sym = new Function_symbol(node.function_name,node.function_type.type);
            global_scope.insert(sym);
            TreeMap<String,Symbol> symbols = new TreeMap<>();
            // level = 1
            var scope = new Scoped_symbol_table(symbols,1,null);
            scope.name = node.function_name;
            scope.category = "function";
            scope.enclosing_scope = global_scope;
            global_scope.sub_scopes.add(scope);
            current_scope = scope;
            // visit形参列表.
            for(var each_parameter:node.formal_parameters)
                each_parameter.accept(this);
            // visit函数体.
            var function_block = (Block_ast_node)node.function_block;
            for(var statement:function_block.statements)
                statement.accept(this);
            node.offset = offset_sum;// 此值即代码生成阶段的stack_size.
        }

        @Override
        public void visit(FunctionCall_ast_node node)
        {
            Symbol function_symbol = current_scope.lookup(node.function_name,false);
            if(function_symbol == null)
            {
                print_err("something wrong!!!");
                System.exit(1);
            }
            node.type = node.baseType = function_symbol.type;
            // 目的是为了确定每一个实参node的Type.
            for(var each_arg:node.arguments)
                each_arg.accept(this);
        }

        @Override
        public void visit(Program_ast_node node)
        {
            // 创建global scope (program) symbol_table.
            TreeMap<String,Symbol> symbols = new TreeMap<>();
            // level = 0
            global_scope = new Scoped_symbol_table(symbols,0,null);
            global_scope.name = "c-link program symbol-table";
            global_scope.category = "global scope";
            if(!node.global_variable_declarations_list.isEmpty())
            {
                current_scope = global_scope;
                for(var variable:node.global_variable_declarations_list)
                    variable.accept(this);
            }
            if(!node.function_declaration_list.isEmpty())
                for(var function:node.function_declaration_list)
                    function.accept(this);
        }
    }
}
