package myfiles;

import syntaxtree.*;
import visitor.*;




public class MySecondVisitor extends GJDepthFirst<String, Void>{


    public SymbolTable symbolt;
    Classes CurClass;
    boolean flag=false;
    String curfunc="none";

    public boolean equal(String a, String b) {
        return (a == null ? b == null : a.equals(b));
    }
    
    public String typeclass(String type){
        String temptype=type;
        if(temptype.equals("this")){
            return CurClass.Name;
        }

        if(symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc)!=null){
            if(symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).Variablelist.get(type)!=null){
                String ftemptype=symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).Variablelist.get(type).type;
                return ftemptype;
            }
            else{
                for(int i=0;i<symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).argumentslist.size();i++){
                    if(symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).argumentslist.get(i).Name==type){
                        return symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).argumentslist.get(i).type;
                    }
                }
            }
        }
       
        
        variables var=symbolt.vari(type, CurClass);
        if(var!=null){
            return var.type;
        }

        if(symbolt.Classlist.get(temptype)!=null){
            temptype=symbolt.rootparent(symbolt.Classlist.get(temptype)).Name;
            return temptype;
        }
        return temptype;
    }
    public boolean sametype(String a, String b){
        return(!equal(a, b)&& !equal(typeclass(a), typeclass(b))&&!equal(typeclass(a),b)&&!equal(a, typeclass(b)));
    }
    public boolean typecheck(String type){

        if(symbolt.Classlist.get(CurClass.Name).Variablelist.get(type)!=null){
            return false;
        }
        else if(symbolt.Classlist.get(CurClass.Name).isextended&&(symbolt.Classlist.get(CurClass.parentName).Variablelist.get(type)!=null)){
            return false;
        }
        else if(symbolt.Classlist.get(type)!=null){
            return false;
        }
        if(type!="int"&&type!="boolean"&&type!="int[]"&&type!="boolean[]"&&type!="this"){
            return true;
        }
        return false;
    }
    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public String visit(Goal n,  Void argu ) throws Exception {
        String classname = n.f0.accept(this, null);
        CurClass=new Classes();
        CurClass.enter(classname,false);
        for (int i=0; i<n.f1.size(); i++) {
            n.f1.elementAt(i).accept(this,null);
        }
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
        for (int i=0; i<n.f14.size(); i++) {
            n.f14.elementAt(i).accept(this, null);
        }

        for (int i=0; i<n.f15.size(); i++) {
            n.f15.elementAt(i).accept(this, null);
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
        CurClass=new Classes();
        CurClass.enter(classname,true);
        for ( int i=0; i<n.f3.size(); i++ ) {
            n.f3.elementAt(i).accept(this, null);
        }
        for ( int i=0; i<n.f4.size(); i++ ) {
            n.f4.elementAt(i).accept(this, null);
        }
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
        n.f3.accept(this, null);
        CurClass=new Classes();
        CurClass.enter(classname,true);
        Classes extended = symbolt.Classlist.get(n.f3.accept(this, null));
        CurClass.isextended=true;
        CurClass.parentName=extended.Name;
        for ( int i=0; i<n.f5.size(); i++ ) {
            n.f5.elementAt(i).accept(this, null);
        }
        for ( int i=0; i<n.f6.size(); i++ ) {
            n.f6.elementAt(i).accept(this, null);
        }

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, Void argu) throws Exception {
        String type = n.f0.accept(this, null);
        //String name = n.f1.accept(this, null);

        if(typecheck(type)){
            throw new Exception("Error found:variable has invalid type "+ type +" in Class "+CurClass.Name);
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
        if(typecheck(myType)){
            throw new Exception("Error found:function " + myName + " has invalid type "+ myType +" in Class "+CurClass.Name);
        }
        if (n.f4.present()) {
                n.f4.accept(this, null);
            }
         for ( int i=0; i<n.f7.size(); i++ ) {
            flag=true;
            n.f7.elementAt(i).accept(this,null);
            flag=false;
         }
         for ( int i = 0; i < n.f8.size(); i++ ) {
            n.f8.elementAt(i).accept(this, null);
        }
        String returnt=n.f10.accept(this, null);

            if(returnt==null){
                throw new Exception("Error found:function " + myName + " returns null in Class "+CurClass.Name+" , must return "+myType);
            }
            else if(sametype(returnt, myType)){
                throw new Exception("Error found:function " + myName + " doesn't return type "+ myType +", returns: "+typeclass(returnt)+" in Class "+CurClass.Name);
            }
        return null;
    }
    
     /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
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
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        if(typecheck(type)){
            throw new Exception("Error found:FormalParameter "+ name +" has type " + type );
        }
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
        String returnt=n.f0.accept(this, null);
        return returnt;
    }

    @Override
    public String visit(ArrayType n, Void argu) throws Exception {
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


    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public String visit(Statement n, Void argu) throws Exception {
        n.f0.accept(this, null);

        return null;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block n, Void argu) throws Exception {
        for ( int i=0; i<n.f1.size(); i++ ) {
            n.f1.elementAt(i).accept(this, null);
        }

        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, Void argu) throws Exception {
        String id = n.f0.accept(this, null);
        String exp = n.f2.accept(this, null);
        String exptype=null;
        if(exp=="this"){
            exptype=CurClass.Name;
        }
        else{
            variables var=symbolt.vari(exp, CurClass);
            if(var!=null){
                exptype=var.type;
            }
            else if(!typecheck(exptype)){
                throw new Exception("Error found: right side of statement contains type: "+exptype+", type "+ typeclass(id) +"Was expected");
            }
            else{
                exptype=exp;
            }
        }
        String idtype=typeclass(id);
        

        if ( !idtype.equals(exptype) && sametype(idtype, exptype)&&sametype(id, exptype) ) {
            throw new Exception("Error found:left side with " + id + " has type " + idtype + " while right side with " + exp + " has type "+ exptype);
        }

        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, Void argu) throws Exception {
        String id = n.f0.accept(this, null);
        String type = typeclass(id);

        if ( sametype(id, "int[]") ) {
            throw new Exception("Error found:left-hand side type should be int[] but it is " + type);
        }

        String bracketExp = n.f2.accept(this, null);

        if (sametype(bracketExp, "int") && !typeclass(bracketExp).equals("int")) {
            throw new Exception("Error found: array " + id + " is inititiated with "+ typeclass(bracketExp) +"instead of int");
        }

        String assignExp = n.f5.accept(this, null);

        if (!assignExp.equals("int") && !typeclass(assignExp).equals("int")) {
            throw new Exception("Error found:in the right side of the statement type should but instead is " + typeclass(assignExp) );
        }

        return null;
    }


    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, Void argu) throws Exception {
        String exp = n.f2.accept(this, null);

        if (!exp.equals("boolean") && !typeclass(exp).equals("boolean")) {
            throw new Exception("Error found:in if expected type should be boolean but instead it is " + typeclass(exp));
        }
        n.f4.accept(this, null);
        n.f6.accept(this, null);
        return null;
    }



    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, Void argu) throws Exception {
        String exp = n.f2.accept(this, null);

        if (!exp.equals("boolean") && !typeclass(exp).equals("boolean")) {
            throw new Exception("Error found:in if expected type should be boolean but instead it is " + typeclass(exp) );
        }

        return null;
    }


     /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, Void argu) throws Exception {
        String exp = n.f2.accept(this, null);
        if (!exp.equals("int") && sametype(exp, "int")) {
            throw new Exception("Error found:in print Expected type should be int but instead it is " + exp);
        }

        return null;
    }


    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */
    public String visit(Expression n, Void argu) throws Exception {
        String exp = n.f0.accept(this, null);
        return exp;
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, Void argu) throws Exception {
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if ( sametype(left, "int") || sametype(right, "int")) {
            throw new Exception("Error found:comparison between" + left + " and " + right + " doesn't contain the same types");
        }

        return "boolean";
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, Void argu) throws Exception {
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if ( sametype(left, "int") || sametype(right, "int")) {
            throw new Exception("Error found:addition between" + left + " and " + right + " doesn't contain the same types");
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, Void argu) throws Exception {
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if ( sametype(left, "int") || sametype(right, "int")) {
            throw new Exception("Error found:subtraction between" + left + " and " + right + " doesn't contain the same types");
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, Void argu) throws Exception {
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if ( sametype(left, "int") || sametype(right, "int")) {
            throw new Exception("Error found:multiplication between" + left + " and " + right + " doesn't contain the same types");
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, Void argu) throws Exception {
        String id = n.f0.accept(this, null);
        String exp = n.f2.accept(this, null);

        if ( sametype(id,"int[]")) {
            throw new Exception("Error found:Expected"+ id +" type to be int[]");
        }

        if ( sametype(exp,"int")) {
            throw new Exception("Error found: indexing with " + id + "who is not an int");
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, Void argu) throws Exception {
        String id = n.f0.accept(this, null);
        if ( sametype(id, "int[]") ) {
            throw new Exception("Error found:Expected expression to be int[]");
        }

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, Void argu) throws Exception {
        String name = n.f0.accept(this, null);
        String method = n.f2.accept(this, null);
        function func;
        if ( name.equals("this")) {
            func = symbolt.func(method, CurClass);
        }
        else {
            Classes temp=symbolt.Classlist.get(name);
            if(temp==null){
                temp=symbolt.Classlist.get(typeclass(name));
            }
            func=symbolt.func(method, temp);

        }
        if (func == null) {
            throw new Exception("Error found:Method " + method + " is not defined");
        }
        
        if(n.f4.present()) {            
            
            String[] callArgs = n.f4.accept(this, null).split(",");

            if(func.argumentslist == null || func.argumentslist.size() != callArgs.length) {
                throw new Exception("Error found:Wrong number of arguments for " + func.Name +" , " + func.argumentslist.size() +"are needed");
            }
            for(int i = 0 ; i < func.argumentslist.size() ; i++) {
                if(sametype(typeclass(callArgs[i]), func.argumentslist.get(i).type)) {
                    throw new Exception("Error found:Method overload is not allowed. There are :"+typeclass(callArgs[i])+" instead of "+func.argumentslist.get(i).type);
                }
            }

        }
        else if(func.argumentslist.size() != 0) {
            throw new Exception("Error found:Wrong number of arguments for " + func.Name + " , " + func.argumentslist.size() +"are needed");
        }
        return func.Type;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
     */
    public String visit(AndExpression n, Void argu) throws Exception {
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);

        if ( sametype(left, "boolean") || sametype(right, "boolean")) {
            throw new Exception("Error found:in AND boolean are needed, " + left + " and " + right + "are not both booleans");
        }

        return "boolean";
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, Void argu) throws Exception {
        return n.f0.accept(this, null) + "," +  n.f1.accept(this, null);
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        return n.f1.accept(this, null);
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, Void argu) throws Exception {
        StringBuffer sb = new StringBuffer();

        for ( int i=0; i< n.f0.size(); i++ ) {
            sb.append(n.f0.elementAt(i).accept(this, null));
            if ( i != n.f0.size() - 1 ) sb.append(",");
        }

        return sb.toString();
    }

    


    /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    *       | BracketExpression()
    */
    public String visit(PrimaryExpression n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }

    public String visit(IntegerLiteral n, Void argu) throws Exception {
        return "int";
    }

    public String visit(TrueLiteral n, Void argu) throws Exception {
        return "boolean";
    }
    public String visit(FalseLiteral n, Void argu) throws Exception {
        return "boolean";
    }
    public String visit(ThisExpression n, Void argu) throws Exception {
        return "this";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, Void argu) throws Exception {

        String exp = n.f3.accept(this, null);
        if ( sametype(exp, "int")) {
            throw new Exception("Error found: array allocation should be allocated with int");
        }
        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, Void argu) throws Exception {
        String id = n.f1.accept(this, null);
        return id;
    }


    /**
     * f0 -> "!"
     * f1 -> PrimaryExpression()
     */
    public String visit(NotExpression n, Void argu) throws Exception {
        String exp = n.f1.accept(this, null);

        if ( sametype(exp, "boolean")) {
            throw new Exception("Error found:NOT schould have boolean ");
        }

        return "boolean";
    }

     /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, Void argu) throws Exception {
        return n.f1.accept(this, null);
    }
    
}