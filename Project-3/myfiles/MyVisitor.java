package myfiles;

import syntaxtree.*;
import visitor.*;




public class MyVisitor extends GJDepthFirst<String, Void>{

    public SymbolTable symbolt;
    Classes CurClass;
    String curfunc="none";
    boolean flag=false;
    int argsize=0;

    public String visit(Goal n,  Void argu ) throws Exception {
        super.visit(n, argu);
        return null;
    }



    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        CurClass=new Classes();
        CurClass.enter(classname,false);
        symbolt.putinClasslist(CurClass,0,0);

        for (int i=0; i<n.f14.size(); i++) {
            n.f14.elementAt(i).accept(this, null);
        }
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        if(symbolt.Classlist.containsKey(classname)){
            throw new Exception("Error found:Class "+classname+" has already been defined.");
        }
        CurClass=new Classes();
        CurClass.enter(classname,true);
        symbolt.putinClasslist(CurClass,0,0);
        super.visit(n, argu);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        String parentclass=n.f3.accept(this, null);
        
        if(symbolt.Classlist.containsKey(classname)){
            throw new Exception("Error found:Class "+classname+" has already been defined.");
        }
        if(!symbolt.Classlist.containsKey(parentclass)){
            throw new Exception("Error found:SuperClass "+parentclass+" has not been defined.");
        }
        CurClass=new Classes();
        CurClass.enter(classname,true);
        Classes extended = symbolt.Classlist.get(n.f3.accept(this, null));
        symbolt.putinClasslist(CurClass,extended.parvarOffset,extended.parfuncOffset);
        CurClass.isextended=true;
        CurClass.parentName=extended.Name;
        super.visit(n, argu);
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, Void argu) throws Exception {
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        if(flag==false){
            if(CurClass.Variablelist.get(name)!=null){
                throw new Exception("Error found: Variable "+ name +" has been defined again in class "+CurClass.Name+".");
            }
            CurClass.putinvariablelist(name,type,false,flag,curfunc);
        }
        else{
            if(CurClass.functionlist.get(curfunc).Variablelist.get(name)!=null){
                throw new Exception("Error found: Variable "+ name +" has been defined again in function "+curfunc+".");
            }
            else{
                for(int i=0;i<CurClass.functionlist.get(curfunc).argumentslist.size();i++){
                    if(CurClass.functionlist.get(curfunc).argumentslist.get(i).Name==name){
                        throw new Exception("Error found: Variable "+ name +" has been defined again in Arguments of "+curfunc+".");
                    }
                }
                
            }
            CurClass.functionlist.get(curfunc).putinvariablelist(name,type,false,flag,curfunc);
        }

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, Void argu) throws Exception {
        //String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
        
        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);
        curfunc=myName;
        argsize=0;
        if ( CurClass.functionlist.get(myName)!=null ) {
            throw new Exception("Error found: Method " + myName + " has been defined again in class "+CurClass.Name+".");
        }
        if(CurClass.isextended){
            function parentfunc=symbolt.Classlist.get(CurClass.parentName).functionlist.get(myName);
            if(parentfunc==null){
                CurClass.putinfunctionlist(myName,myType,false);
            }
            else{
            
                CurClass.putinfunctionlist(myName, myType,true);
            }
            if (n.f4.present()) {
                n.f4.accept(this, null);
            }
            if(parentfunc!=null){
                if(parentfunc.argumentslist.size()!=argsize){
                    throw new Exception("Error found: Method overload in arguments of method <" + myName + ">");
                }
                for(int i=0;i<parentfunc.argumentslist.size();i++){
                 if(parentfunc.argumentslist.get(i).type!= CurClass.functionlist.get(myName).argumentslist.get(i).type){
                        throw new Exception(" Error found:Method overload in types of arguments of method <" + myName + ">");
                    }    
                }
            }
            
        }
        else{
            CurClass.putinfunctionlist(myName,myType,false);
            if (n.f4.present()) {
                n.f4.accept(this, null);
            }
        }
        argsize=0;
         for ( int i=0; i<n.f7.size(); i++ ) {
             flag=true;
             n.f7.elementAt(i).accept(this,null);
             flag=false;
         }
        n.f10.accept(this, null);
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Void argu) throws Exception {
        n.f0.accept(this, null);
        
        n.f1.accept(this, null);
        
        

        return null;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
        n.f1.accept(this, argu);
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */    
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        

        for ( Node node: n.f0.nodes) {
            node.accept(this, null);
        }
        

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{
        argsize+=1;
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        if(CurClass.Variablelist.get(name)!=null&&CurClass.Variablelist.get(name).inclass==flag){
            throw new Exception("Error found: Variable "+name+" has already been defined in class "+CurClass.Name+".");
        }
        CurClass.putinvariablelist(name, type,true,true,curfunc);
        return null;
    }
    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    @Override
    public String visit(Type n, Void argu) throws Exception {

        return n.f0.accept(this, null);
    }

    @Override
    public String visit(ArrayType n, Void argu) {
        return "int[]";
    }
    @Override
    public String visit(BooleanType n, Void argu) {
        return "boolean";
    }
    @Override
    public String visit(IntegerType n, Void argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, Void argu) {
        
        return n.f0.toString();
    }
    
}
