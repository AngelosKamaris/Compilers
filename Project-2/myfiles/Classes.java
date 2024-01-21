package myfiles;
import java.util.*;


public class Classes {
    String Name;
    int varOffset;
    boolean isextended=false;
    String parentName;
    int funcOffset;
    int parvarOffset;
    int parfuncOffset;
    boolean pr;
    public LinkedHashMap<String, variables> Variablelist;
    public LinkedHashMap<String, function> functionlist;

    Classes() {
        Variablelist = new LinkedHashMap<>();
        functionlist = new LinkedHashMap<>();
    }

    public void enter(String n, boolean p){
        this.Name=n;
        this.pr=p;
    }
    
    public void putinvariablelist(String varname, String vartype, boolean arg,boolean cls, String fnam){
        variables current = new variables();
        current.enter(varname, vartype,arg,cls,false);
        Variablelist.put(varname, current);
        if(arg&&fnam!=null){
            if(this.functionlist.get(fnam)!=null){
            
            this.functionlist.get(fnam).putinargumentslist(varname, vartype);
            }
        }
        if((!arg)&&(!cls)){
            this.parvarOffset+=current.Value;
        }
    }

    public void putinfunctionlist(String methname, String methtype,boolean g){
        function current = new function();
        current.enter(methname, methtype,g);
        functionlist.put(methname, current);
        if(!g){
            this.parfuncOffset+=8;
        }
    }

    public void printdebug(){
        System.out.println("In class: "+this.Name);
        for(String names: this.Variablelist.keySet()){
            this.Variablelist.get(names).print();
        }
        for(String names: this.functionlist.keySet()){
            this.functionlist.get(names).print();
            System.out.println("Arguments:");
            for(int i=0;i<this.functionlist.get(names).argumentslist.size();i++){
                this.functionlist.get(names).argumentslist.get(i).print();
            }
            System.out.println("Variables:");
            for(String vnames: this.functionlist.get(names).Variablelist.keySet()){
                this.functionlist.get(names).Variablelist.get(vnames).print();
            }
        }

    }


    public void print(){
        for(String names: this.Variablelist.keySet()){
            if(!this.Variablelist.get(names).inclass){
                System.out.println(this.Name+"."+this.Variablelist.get(names).Name+" : "+this.varOffset);
                this.varOffset+=this.Variablelist.get(names).Value;
            }
        }
        for(String names: this.functionlist.keySet()){
            if(!this.functionlist.get(names).ghost){
                System.out.println(this.Name+"."+this.functionlist.get(names).Name+" : "+this.funcOffset);
                this.funcOffset+=8;
            }
        }
    }

}
