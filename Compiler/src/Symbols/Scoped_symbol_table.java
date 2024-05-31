package Symbols;
import java.util.TreeMap;
import java.util.Vector;
public class Scoped_symbol_table
{
    public String name;// 作用域名称
    public String category;// 如：全局作用域、函数作用域、block作用域等
    public TreeMap<String,Symbol> symbols;
    public int level;
    public int child_no_count = 0;
    public Scoped_symbol_table enclosing_scope;
    public Vector<Scoped_symbol_table> sub_scopes = new Vector<>();
    public Scoped_symbol_table(TreeMap<String ,Symbol> symbols,int scope_level,Scoped_symbol_table parent_scope)
    {
        this.symbols = symbols;
        this.level = scope_level;
        this.enclosing_scope = parent_scope;
    }
    public void insert(Symbol symbol)
    {
        symbols.put(symbol.name,symbol);
    }
    public Symbol lookup(String symbol_name,boolean current_scope_only)
    {
        //采用python版本的lookup方法
        Symbol symbol = symbols.get(symbol_name);
        if(symbol != null)
            return symbol;
        if(current_scope_only)
            return null;
        if(enclosing_scope != null)
            return enclosing_scope.lookup(symbol_name,false);
        return null;
    }
}
