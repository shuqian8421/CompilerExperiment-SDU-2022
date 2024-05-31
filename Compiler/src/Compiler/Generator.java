package Compiler;
import AST_nodes.*;
import Visitors.Visitor;
import java.util.Map;
import java.util.TreeMap;
import static Compiler.Analyzer.Semantic_analyzer.globals;
public class Generator
{
    private Generator(){}
    private static final TreeMap<String,String> print_format_strings = new TreeMap<>();
    public static void generate(AST_node entrance)
    {
        X86_conde_generator generator = new X86_conde_generator();
        entrance.accept(generator);
    }
    private static class X86_conde_generator extends Visitor
    {
        private static final String[] parameter_registers={"rdi","rsi","rdx","rcx","r8","r9"};
        private static final String[] parameter_registers_float = {"xmm0","xmm1","xmm2","xmm3","xmm4","xmm5","xmm6","xmm7"};
        private static int auto_label_no = 0;
        private static String current_break_label;
        private static String current_continue_label;

        private static int align_to(int n)
        {
            return ((n + 16 - 1) / 16) * 16;
        }
        private static void compare_zero(AST_node condition)
        {
            if(condition.baseType != Node_type.Node_float)
                System.out.println("    cmp $0, %rax");
            else
            {
                //  比如 if (3.1 - 3.1)，即 if (0.0) 的场合
                System.out.println("    xorpd %xmm1, %xmm1");
                System.out.println("    ucomisd %xmm1, %xmm0");
            }
        }
        public void generate_variable_address(Variable_ast_node variable_ast_node)
        {
            int variable_offset = variable_ast_node.symbol.offset;
            /* 1. 全局变量 */
            if(variable_offset==0)// offset=0，则是全局变量，位于.data段
                System.out.println("    lea "+variable_ast_node.variable_name+"(%rip), %rax");
                /* 2. 局部变量 */
            else if(variable_ast_node.type ==Node_type.Node_array)
            {
                // 若是数组元素，数组各个元素从左往右，内存地址从小到大(从下到上).
                variable_ast_node.index_expression.accept(this);
                System.out.println("    imul $8, %rax");
                System.out.println("    push %rax");
                System.out.println("    lea "+variable_offset+"(%rbp), %rax");
                System.out.println("    pop %rdi");
                System.out.println("    add %rdi, %rax");
            }
            else// 若不是数组
                System.out.println("    lea "+variable_offset+"(%rbp), %rax");
        }
        @Override
        public void visit(NumLiteral_ast_node node)
        {
            if(node.baseType == Node_type.Node_int)
                System.out.println("    mov $"+node.literal+", %rax\n");
            else if(node.baseType ==Node_type.Node_float)
            {
                float f64 = Float.parseFloat(node.literal);
                System.out.println("    mov $"+", %rax   # float "+f64);
                System.out.println("    movq %rax, %xmm0\n");
            }
            else if(node.baseType == Node_type.Node_bool)
            {
                // 类似c语言，true用整数1表示，false用整数0表示.
                if(node.literal.equals("true"))
                    System.out.println("    mov $1, %rax");
                else
                    System.out.println("    mov $0, %rax");
            }
            else if(node.baseType == Node_type.Node_char)
            {
                char c = node.literal.charAt(0);
                System.out.println("    mov $"+(int)c+", %rax   # char ");
            }
        }

        @Override
        public void visit(Assignment_ast_node node)
        {
            // 赋值语句的左值是地址.
            // 获取该地址，放入%rax，并由%rax入栈.
            generate_variable_address((Variable_ast_node) node.left);
            System.out.println("    push %rax");
            // 赋值语句的右值是数值.
            node.right.accept(this);
            System.out.println("    pop %rdi");
            // 若right-side是int类型的结果，则位于%rax; 若是float类型，则位于%xxm0.
            if(node.baseType==Node_type.Node_float)
            {
                // float类型的结果.
                if(node.right.baseType==Node_type.Node_float || node.right.baseType==Node_type.Node_int)// 若%rax参与float类型的运算，
                    System.out.println("    cvtsi2sd %rax, %xmm0");// 则需将之转换成float类型，并放入%xmm0.
                System.out.println("    movsd %xmm0, (%rdi)");
            }
            else
                System.out.println("    mov %rax, (%rdi)");// int or bool, 即int类型的结果.
        }

        @Override
        public void visit(Return_ast_node node)
        {
            if(node.expression != null)
                node.expression.accept(this);
            System.out.println("    jmp L."+node.function_name+".return");
        }

        @Override
        public void visit(BinaryOperator_ast_node node)
        {
            /* 1. float类型 */
            if(node.baseType ==Node_type.Node_float)
            {
                // 算符右侧运算数.
                node.right.accept(this);
                if(node.right.baseType == Node_type.Node_float || node.right.baseType == Node_type.Node_int)
                {
                    // 整数转换为浮点数.
                    System.out.println("    cvtsi2sd %rax, %xmm0");
                }
                // push float
                System.out.println("    sub $8, %rsp");
                System.out.println("    movsd %xmm0, (%rsp)");
                // 算符左侧运算数.
                node.left.accept(this);
                if(node.left.baseType == Node_type.Node_float || node.left.baseType == Node_type.Node_int)
                {
                    // 整数转换为浮点数.
                    System.out.println("    cvtsi2sd %rax, %xmm0");
                }
                // pop float
                System.out.println("    movsd (%rsp), %xmm1");
                System.out.println("    add $8, %rsp");
                switch(node.operator)
                {
                    case Token_plus -> System.out.println("    addsd %xmm1, %xmm0");
                    case Token_minus -> System.out.println("    subsd %xmm1, %xmm0");
                    case Token_multiply_or_dereference -> System.out.println("    mulsd %xmm1, %xmm0");
                    case Token_divide -> System.out.println("    divsd %xmm1, %xmm0");
                    case Token_equal,Token_not_equal,Token_less_than,Token_less_equal,Token_greater_than,Token_greater_equal ->
                    {
                        System.out.println("    ucomisd %xmm0, %xmm1");
                        switch(node.operator)
                        {
                            case Token_equal ->
                            {
                                System.out.println("    sete %al");
                                System.out.println("    setnp %dl");
                                System.out.println("    and %dl, %al");
                            }
                            case Token_not_equal ->
                            {
                                System.out.println("    setne %al");
                                System.out.println("    setp %dl");
                                System.out.println("    or %dl, %al");
                            }
                            case Token_less_than -> System.out.println("    setb %al");
                            case Token_less_equal -> System.out.println("    setbe %al");
                            case Token_greater_than -> System.out.println("    seta %al");
                            case Token_greater_equal -> System.out.println("    setae %al");
                        }
                        System.out.println("    and $1, %al");
                        System.out.println("    movzb %al, %rax");
                        return;
                    }
                    default -> {
                        return;
                    }
                }
            }
            /* 2. 非float类型 */
            // 算符右侧运算数.
            node.right.accept(this);
            System.out.println("    push %rax");
            // 算符左侧运算数.
            node.left.accept(this);
            System.out.println("    pop %rdi");
            switch(node.operator)
            {
                case Token_plus -> System.out.println("    add %rdi, %rax");
                case Token_minus -> System.out.println("    sub %rdi, %rax");
                case Token_multiply_or_dereference -> System.out.println("    imul %rdi, %rax");
                case Token_divide -> System.out.println("    div %rdi, %rax");
                case Token_equal,Token_not_equal,Token_less_than,Token_less_equal,Token_greater_than,Token_greater_equal ->
                {
                    System.out.println("    cmp %rdi, %rax");
                    switch(node.operator)
                    {
                        case Token_equal -> System.out.println("    sete %al");
                        case Token_not_equal -> System.out.println("    setne %al");
                        case Token_less_than -> System.out.println("    setl %al");
                        case Token_less_equal -> System.out.println("    setle %al");
                        case Token_greater_than -> System.out.println("    setg %al");
                        case Token_greater_equal -> System.out.println("    setge %al");
                    }
                    System.out.println("    movzb %al, %rax");
                }
            }
        }

        @Override
        public void visit(UnaryOperator_ast_node node)
        {
            switch(node.op)
            {
                case Token_plus -> {}
                case Token_minus ->
                {
                    // "-" 取相反数.
                    node.right.accept(this);// 获得右值，存于%rax.
                    if(node.baseType==Node_type.Node_int)
                        System.out.println("    neg %rax");
                    else
                    {
                        // 下面的四步将一个float取负数，即实现 %xmm0 = (-1) * %xmm0.
                        // 是有点啰嗦. chibicc有另一个解决方案，但是也需要四步，同样啰嗦 :)
                        System.out.println("    mov $1, %rax");
                        System.out.println("    neg %rax");
                        System.out.println("    cvtsi2sd %rax, %xmm1");
                        System.out.println("    mulsd %xmm1, %xmm0");
                    }
                }
                case Token_not ->
                {
                    // "!" 取反.
                    node.right.accept(this);
                    // 该步获得右值，存于%rax.
                    if(node.baseType==Node_type.Node_bool)
                        System.out.println("    not %rax");
                }
                case Token_address ->
                {
                    // "&" 取地址.
                    Variable_ast_node variable_ast_node = (Variable_ast_node) node.right;
                    // 获取其在内存中的地址：
                    int variable_offset = variable_ast_node.symbol.offset;
                    if(variable_offset==0)// offset=0，则是全局变量，位于.data段
                        System.out.println("    lea "+variable_ast_node.variable_name+"(%rip), %rax");
                    else// 否则，是局部变量或参数，位于栈
                        System.out.println("    lea "+variable_offset+"(%rbp), %rax");
                }
                case Token_multiply_or_dereference ->
                {
                    // "*" 取值.
                    node.right.accept(this);// 该步获得左值(即地址)，存于%rax.
                    // 然后，将其值由内存放入寄存器.
                    if(node.baseType==Node_type.Node_float)// 若是float，
                    {
                        System.out.println("    movsd (%rax), %xmm0");// 其值放入%xmm0.
                    }
                    else if(node.baseType==Node_type.Node_int||node.baseType==Node_type.Node_bool||node.baseType==Node_type.Node_char)
                        System.out.println("    mov (%rax), %rax");// 其值放入%rax.
                }
            }

        }

        @Override
        public void visit(Variable_ast_node node)
        {
            /* 变量是右值 */
            // 1.首先，获取其在内存中的地址，存于%rax：
            int variable_offset = node.symbol.offset;
            // 情况1：若offset=0，则是全局变量，位于.data段
            if(variable_offset==0)
                System.out.println("    lea "+node.variable_name+"(%rip), %rax");
            else// 情况2：局部变量或参数，位于栈
                generate_variable_address(node);
            // 2.然后，将其值由内存放入寄存器.
            if(node.baseType==Node_type.Node_float) // 若是float，
                System.out.println("    movsd (%rax), %xmm0");  // 其值放入%xmm0.
            else if(node.baseType==Node_type.Node_int|| node.baseType==Node_type.Node_bool||node.baseType==Node_type.Node_char||node.baseType==Node_type.Node_array)// 否则，
                System.out.println("    mov (%rax), %rax");// 其值放入%rax.
        }

        @Override
        public void visit(BaseType_ast_node node) {}

        @Override
        public void visit(Block_ast_node node)
        {
            if(!node.statements.isEmpty())
                for(var n:node.statements)
                    n.accept(this);
        }

        @Override
        public void visit(If_ast_node node)
        {
            // 此处的label，也可以如龙书第2章2.8.4小节所述，作为If_AST_Node的属性，在其构造函数中确定。
            auto_label_no += 1;
            String auto_label = String.valueOf(auto_label_no);
            node.condition.accept(this);
            compare_zero(node.condition);
            System.out.println("    je  .L.else."+auto_label);
            node.then_statement.accept(this);
            System.out.println("    jmp .L.end."+auto_label);
            System.out.println(".L.else."+auto_label+":");
            if(node.else_statement!=null)
                node.else_statement.accept(this);
            System.out.println(".L.end."+auto_label+":");
        }

        @Override
        public void visit(While_ast_node node)
        {
            auto_label_no+=1;
            String auto_label = String.valueOf(auto_label_no);
            String previous_break_label=current_break_label;
            String previous_continue_label = current_continue_label;
            current_break_label = ".L.end."+auto_label;
            current_continue_label = ".L.condition."+auto_label;
            System.out.println(".L.condition."+auto_label+":");
            node.condition.accept(this);
            compare_zero(node.condition);
            System.out.println("    je  .L.end."+auto_label);
            node.statement.accept(this);
            System.out.println("    jmp  .L.condition."+auto_label);
            System.out.println(".L.end."+auto_label+":");
            current_break_label = previous_break_label;
            current_continue_label = previous_continue_label;
        }

        @Override
        public void visit(DoWhile_ast_node node)
        {
            auto_label_no +=1;
            String auto_label = String.valueOf(auto_label_no);
            String previous_break_label = current_break_label;
            String previous_continue_label = current_continue_label;
            current_break_label = ".L.end."+auto_label;
            current_continue_label =".L.condition."+auto_label;
            System.out.println(".L.do."+auto_label+":");
            node.statement.accept(this);
            node.condition.accept(this);
            compare_zero(node.condition);
            System.out.println("    je  .L.end."+auto_label);
            System.out.println("    jmp  .L.do."+auto_label);
            System.out.println(".L.end."+auto_label+":");
            current_break_label = previous_break_label;
            current_continue_label = previous_continue_label;
        }

        @Override
        public void visit(For_ast_node node)
        {
            auto_label_no += 1;
            String auto_label = String.valueOf(auto_label_no);
            String previous_break_label = current_break_label;
            String previous_continue_label = current_continue_label;
            if(node.initialization!=null)
                node.initialization.accept(this);
            System.out.println(".L.condition."+auto_label+":");
            node.condition.accept(this);
            System.out.println("    je  .L.end."+auto_label);
            node.statement.accept(this);
            if(node.increment!=null)
                node.increment.accept(this);
            System.out.println("    jmp  .L.condition."+auto_label);
            System.out.println(".L.end."+auto_label+":");
            current_continue_label = previous_continue_label;
            current_break_label = previous_break_label;
        }

        @Override
        public void visit(Break_ast_node node)
        {
            System.out.println("    jmp "+current_break_label);
        }

        @Override
        public void visit(Continue_ast_node node)
        {
            System.out.println("    jmp "+current_continue_label);
        }

        @Override
        public void visit(Print_ast_node node)
        {
            node.value.accept(this);
            switch(node.value.baseType)
            {
                case Node_float ->
                {
                    print_format_strings.put("printf_format_float","    .string   \"%f\\n\"");
                    System.out.println("    lea printf_format_float(%rip), %rdi");
                }
                case Node_int ->
                {
                    print_format_strings.put("printf_format_int","    .string   \"%d\\n\"");
                    System.out.println("    lea printf_format_int(%rip), %rdi");
                    System.out.println("    mov %rax, %rsi");
                }
                case Node_char ->
                {
                    print_format_strings.put("printf_format_char","    .string   \"%c\\n\"");
                    System.out.println("    lea printf_format_char(%rip), %rdi");
                    System.out.println("    mov %rax, %rsi");
                }
            }
            System.out.println("    call printf");// 调用外部函数printf.
        }

        @Override
        public void visit(SingleVariableDeclaration_ast_node node)
        {
            for(var init:node.inits)
                if(init != null)
                    init.accept(this);
        }

        @Override
        public void visit(Parameter_ast_node node)
        {
            node.accept(this);
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
            // Prologue
            System.out.println("\n    .text");
            System.out.println("    .global "+node.function_name);
            System.out.println(node.function_name+":");
            System.out.println("    push %rbp");
            System.out.println("    mov %rsp, %rbp");
            int stack_size = align_to(node.offset);
            if(stack_size !=0)
                System.out.println("    sub $"+stack_size+", %rsp");
            // 从左往右，将参数由寄存器压读出，放入栈.
            int i=0,f=0;
            for(var parameter:node.formal_parameters)
            {
                Parameter_ast_node p = (Parameter_ast_node) parameter;
                if(parameter.type ==Node_type.Node_float)
                    System.out.println("    movq %"+parameter_registers_float[f++]+", "+p.symbol.offset+"(%rbp)");
                else
                    System.out.println("    mov %"+parameter_registers[i++]+", "+p.symbol.offset+"(%rbp)");
            }
            node.function_block.accept(this);
            System.out.println("L."+node.function_name+".return:");
            // Epilogue
            System.out.println("    mov %rbp, %rsp");
            System.out.println("    pop %rbp");
            System.out.println("    ret");
        }

        @Override
        public void visit(FunctionCall_ast_node node)
        {
            int float_count=0,others_count=0;
            // 实参入栈.
            for(var argument:node.arguments)
            {
                argument.accept(this);
                if(argument.baseType == Node_type.Node_float)
                {
                    // push float.
                    System.out.println("    sub $8, %rsp");
                    System.out.println("    movsd %xmm0, (%rsp)");
                    float_count += 1;
                }
                else
                {
                    System.out.println("    push %rax");
                    others_count += 1;
                }
            }
            // 实参分发，将数值存入寄存器.
            for(int j=node.arguments.size() -1;j>=0;j--)
            {
                if(node.arguments.elementAt(j).baseType ==Node_type.Node_float)
                {
                    float_count = float_count -1;
                    // pop float
                    System.out.println("    movsd (%rsp), %"+parameter_registers_float[float_count]);
                    System.out.println("    add $8, %rsp");
                }
                else
                {
                    others_count = others_count -1;
                    System.out.println("    pop %"+parameter_registers[others_count]);
                }
            }
            System.out.println("    mov $0, %rax");
            System.out.println("    call "+node.function_name);
        }

        @Override
        public void visit(Program_ast_node node)
        {
            // 1. 遍历函数List，生成各个函数对应的汇编代码
            if(!node.function_declaration_list.isEmpty())
                for(var n:node.function_declaration_list)
                    n.accept(this);
            // 2. 对于全局变量，已经在语义分析阶段将其信息导入globals，
            //    故无需遍历全局变量List，而只需根据globals，
            //    将全局或静态变量对应的汇编代码放在最后即可
            if(!globals.isEmpty())
                System.out.println("\n    .data");
            for(var g_var:globals)
            {
                System.out.println(g_var.variable_name+":");
                for(var e:g_var.elements)
                    if(g_var.baseType ==Node_type.Node_float)
                        System.out.println("    .double "+e);
                    else// if (g_var.type == Ty_int)
                        System.out.println("    .quad "+e);
            }
            for(Map.Entry<String ,String> f :print_format_strings.entrySet())
            {
                System.out.println(f.getKey()+":");
                System.out.println(f.getValue());
            }
        }

    }
}
