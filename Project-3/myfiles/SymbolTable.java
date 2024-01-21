package myfiles;
import java.util.*;

public class SymbolTable {

    public LinkedHashMap<String, Classes> Classlist;

    public SymbolTable() {
        Classlist = new LinkedHashMap<>();
    }
    
    public void putinClasslist(Classes current,int varoffset, int funcoffset){
        current.varOffset=varoffset;
        current.funcOffset=funcoffset;
        Classlist.put(current.Name, current);

    }


    public void printtable(Classes curClasses){
        curClasses.print();
    }

    public void printall(){
        for(String names: this.Classlist.keySet()){
            if(Classlist.get(names).pr){
                printtable(Classlist.get(names));
            }

        }
    }

    public void printdebug(){
        for(String names: this.Classlist.keySet()){
            Classlist.get(names).printdebug();
        }
    }

    public Classes rootparent(Classes Cur){
        
        Classes temp=this.Classlist.get(Cur.Name);
        while(temp.isextended==true){
            temp=this.Classlist.get(temp.parentName);
        }
        return temp;

    }

    public function func(String funcname,Classes Cur){
        Classes temp=this.Classlist.get(Cur.Name);
        while(temp!=null){
            if(temp.functionlist.get(funcname)!=null){
                return temp.functionlist.get(funcname);
            }
            else{
                temp=this.Classlist.get(temp.parentName);
            }
        }

        return null;

    }

    public variables vari(String varname,Classes Cur){
        Classes temp=this.Classlist.get(Cur.Name);
        while(temp!=null){
            if(temp.Variablelist.get(varname)!=null){
                return temp.Variablelist.get(varname);
            }
            else{
                temp=this.Classlist.get(temp.parentName);
            }
        }

        return null;

    }


}

