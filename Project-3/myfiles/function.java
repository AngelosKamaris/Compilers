package myfiles;
import java.util.*;

public class function {
    String Name;
    String Type;
    public ArrayList<variables> argumentslist;
    public LinkedHashMap<String, variables> Variablelist;
    boolean ghost;
    int offset;

    public void enter(String n, String t, boolean g){
        this.Name=n;
        this.Type=t;
        this.ghost=g;
        this.offset=0;
        argumentslist = new ArrayList<>();
        Variablelist = new LinkedHashMap<>();
    }

    public void putinvariablelist(String varname, String vartype, boolean arg,boolean cls, String fnam){
        variables current = new variables();
        current.enter(varname, vartype,arg,cls,false);
        Variablelist.put(varname, current);
    }

    public void putinargumentslist(String varname, String vartype){
        variables current = new variables();
        current.enter(varname, vartype,false,false,true);
        argumentslist.add(current);
    }

    public void print(){
        System.out.println("Function Name = "+this.Name+" Type = "+this.Type+" ghost="+ghost);
    }
    

}
