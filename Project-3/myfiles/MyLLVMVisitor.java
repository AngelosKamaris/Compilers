package myfiles;

import syntaxtree.*;
import visitor.*;
import java.io.*;




public class MyLLVMVisitor extends GJDepthFirst<String, Void>{

    public SymbolTable symbolt;
    Classes CurClass;
    boolean flag=false;     
    String curfunc="none";
	public String filename;
	String text="";             //string to store contents of .ll file
    int regcount=0;             //register counter
    int ifcount=0;              //if label counter
    int loopcount=0;            //loop >>
    int oobcount=0;             //oob >>
    int allocount=0;            //alloc >>
    int store=0;                //tells Identifier if he will be loaded in ll file, and if so how depending on its value
    String namereg;             //keeps the real name of register
    boolean offlag=false;       //flag to print current function and offset in .ll
    String tmpcls=null;         //stores the class to be printed after ; in .ll
    
    public boolean loaded(String string){   //function to check if string needs to be loaded in .ll file   (we don't load numbers or registers)
        try {
            Integer.parseInt(string);
            return false;
        } catch (NumberFormatException e) {
            if(string.startsWith("%")){
                return false;
            }
            else{
                return true;
            }
        }
    }

    public String typeclass(String type){
        String temptype=type;
        if(temptype.equals("%this")){
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

	String turni (String type) {                    //turn a java type to an llvm type
		if ( type.equals("boolean") ) 
			return "i1";
		else if ( type.equals("int") )
			return "i32";
		else if ( type.equals("int[]") )
			return "i32*";
		else
			return "i8*";
	}

	/**
	 * Grammar production:
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 */
    public String visit(Goal n,  Void argu ) throws Exception {
		
		
		File temp= new File(filename);
		String name=temp.getName().substring(0, temp.getName().length() - 5);   //keep the name without .java and the path
		String pathname="llvm/"+name+".ll";
		File file=new File(pathname);
		file.createNewFile();
		FileWriter fileWrite=new FileWriter(pathname);
		for(String names: symbolt.Classlist.keySet()){
            boolean f=false;
            text=text+"@."+symbolt.Classlist.get(names).Name+ "_vtable = global ["+symbolt.Classlist.get(names).functionlist.size()+" x i8*] [";
            Classes tempC=symbolt.Classlist.get(names);
            for(String fnames: tempC.functionlist.keySet()){
                if(f){
                    text=text+", ";
                }
                function tempF=tempC.functionlist.get(fnames);
                text=text+"i8* bitcast ("+turni(tempF.Type)+ " (i8*";
                for(int i=0; i<tempF.argumentslist.size(); i++){
                    variables temparg=tempF.argumentslist.get(i);
                    text = text +", "+turni(temparg.type);
                }
                text = text + ")* @." + tempC.Name + "." + tempF.Name + " to i8*)";
                f=true;
            }
            text=text+ "]\n";
        }
		fileWrite.write(text);
		text="";
		fileWrite.write("\n\ndeclare i8* @calloc(i32, i32)\n");
		fileWrite.write("declare i32 @printf(i8*, ...)\n");
		fileWrite.write("declare void @exit(i32)\n\n");
		fileWrite.write("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n");
		fileWrite.write("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n");
		fileWrite.write("define void @print_int(i32 %i) {\n");
		fileWrite.write("\t%_str = bitcast [4 x i8]* @_cint to i8*\n");
		fileWrite.write("\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n");
		fileWrite.write("\tret void\n");
		fileWrite.write("}\n\n");
		fileWrite.write("define void @throw_oob() {\n");
		fileWrite.write("\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n");
		fileWrite.write("\tcall i32 (i8*, ...) @printf(i8* %_str)\n");
		fileWrite.write("\tcall void @exit(i32 1)\n");
		fileWrite.write("\tret void\n");
		fileWrite.write("}\n\n");
        String classname = n.f0.accept(this, null);
        CurClass=new Classes();
        CurClass.enter(classname,false);
        n.f1.accept(this,null);
		fileWrite.write(text);
		fileWrite.close();
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
        store=0;
        String classname = n.f1.accept(this, null);
        CurClass=new Classes();
        CurClass.enter(classname,false);
        text=text+"define i32 @main() {\n";
        for (int i=0; i<n.f14.size(); i++) {
            n.f14.elementAt(i).accept(this, null);
        }
        for (int i=0; i<n.f15.size(); i++) {
            n.f15.elementAt(i).accept(this, null);
        }
		text = text + "\tret i32 0\n";
		text = text + "}\n\n";
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
        CurClass=new Classes();
        CurClass.enter(classname,true);
        Classes extended = symbolt.Classlist.get(n.f3.accept(this, null));
        CurClass.isextended=true;
        CurClass.parentName=extended.Name;
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
        String type=n.f0.accept(this, null);
        String name=n.f1.accept(this, null); 
        text=text+"\t%" + name + " = alloca " + turni(typeclass(type))+"\n\n";       
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
        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);
        curfunc=myName;
        regcount=0;
        ifcount=0;                                                          //reinitialize counters for each new function
        loopcount=0;
        oobcount=0;
        allocount=0;
        text = text + "\ndefine " + turni(myType) + " @" + CurClass.Name + "." + myName + "(i8* %this";
        if (n.f4.present()) {
                n.f4.accept(this, null);
            }
         function tempF=symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc);
         for(int i=0; i<tempF.argumentslist.size(); i++){
            variables temparg=tempF.argumentslist.get(i);
            text = text +", "+turni(temparg.type)+" %."+temparg.Name;
        }
        text=text+") {\n";
        for(int i=0; i<tempF.argumentslist.size(); i++){
            variables temparg=tempF.argumentslist.get(i);
            text = text + "\t%" + temparg.Name + " = alloca " + turni(temparg.type) + "\n\tstore " + turni(temparg.type) + " %." + temparg.Name + ", " + turni(temparg.type) + "* %" + temparg.Name + "\n";
        }
        for ( int i=0; i<n.f7.size(); i++ ) {
            flag=true;
            n.f7.elementAt(i).accept(this,null);
            flag=false;
         }
         for ( int i = 0; i < n.f8.size(); i++ ) {
            n.f8.elementAt(i).accept(this, null);
        }
        store=1;
        String returnt=n.f10.accept(this, null);
        store=0;
        text = text + "\tret " + turni(tempF.Type) + " " + returnt + "\n"+ "}\n";
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

    /**
    * Grammar production:
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Void argu) {
        namereg=n.f0.toString();
        if(store==0){
            if(offlag==true){
                text = text +namereg+" : "+symbolt.func(namereg, symbolt.Classlist.get(tmpcls)).offset/8+"\n";
            }
            return namereg;
        }
        else if(store==1){
            String id=n.f0.toString();
            variables tempv=symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).Variablelist.get(id);
            if(tempv==null){
                for(int i=0;i<symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).argumentslist.size();i++){
                    if(id==symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).argumentslist.get(i).Name){
                        tempv=symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).argumentslist.get(i);
                        break;
                    }
                }
            }
            if(tempv!=null){
				text = text + "\t%_" + regcount + " = load " + turni(tempv.type) + ", " + turni(tempv.type) + "* " +  "%" + tempv.Name + "\n";
				regcount +=1;
            }
            else{
                tempv=symbolt.vari(id, CurClass);               
				text = text + "\t%_" + regcount + " = getelementptr i8, i8* %this, i32 " + (tempv.offset + 8) + "\n";
				regcount += 1;
				text = text + "\t%_" + regcount + " = bitcast i8* %_" + (regcount - 1) + " to " + turni(tempv.type) + "*\n";
				regcount += 1;
				text = text + "\t%_" + regcount + " = load " + turni(tempv.type) + ", " + turni(tempv.type) + "* %_" + (regcount - 1) + "\n";
				regcount +=1;
			}
            return "%_" + (regcount - 1);
        }
        else if(store==2){
            String id = n.f0.toString();
			variables tempv=symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).Variablelist.get(id);
            if(tempv!=null){
                String ret="%"+n.f0.toString();
				return ret;
			} else {
				tempv=symbolt.vari(id, CurClass); 
				text = text + "\t%_" + regcount + " = getelementptr i8, i8* %this, i32 " + (tempv.offset + 8) + "\n";
				regcount += 1;
				text = text + "\t%_" + regcount + " = bitcast i8* %_" + (regcount - 1) + " to " + turni(tempv.type) + "*\n";
				regcount += 1;
			}
            return "%_" + (regcount - 1);
        }
        return "%_" + (regcount - 1);
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
        return n.f0.accept(this, null);
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
        variables tempv=symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).Variablelist.get(id);
        if(tempv==null){
            tempv=symbolt.Classlist.get(CurClass.Name).Variablelist.get(id);
        }
        store=1;
        String exp = n.f2.accept(this, null);
        store=0;
        if (loaded(exp)) {
			exp = "%" + exp;
			text = text + "\t%_" + regcount + " = load " + turni(tempv.type) + ", " + turni(tempv.type) + "* " + exp + "\n";
			exp = "%_" + regcount;
			regcount += 1;
		}
        store=2;
        id = n.f0.accept(this, null);
        store=0;
		text = text + "\tstore " + turni(tempv.type) + " " + exp + ", " + turni(tempv.type) + "* " + id + "\n";
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
        int temps=store;
        store=1;
        String id = n.f0.accept(this, null);
        String temp=id;
        if(loaded(id)){
            text=text+ "\t%_" + regcount + " = load i32, i32* " + id + "\n";
            temp="%_"+regcount;
            regcount+=1;
        }
        store=0;
        String bracketExp = n.f2.accept(this, null);
        store=temps;
        if ( loaded(bracketExp)){
			bracketExp = "%" + bracketExp;
			text = text + "\t%_" + regcount + " = load i32, i32* " + bracketExp + "\n";
			bracketExp = "%_" + regcount;
			regcount += 1;
		}
        
        text = text + "\t%_" + regcount + " = icmp ult i32 " + bracketExp + ", " + temp + "\n";
		regcount += 1;	
		text = text + "\tbr i1 %_" + (regcount - 1) + ", label %oob" + oobcount + ", label %oob" + (oobcount+1) + "\n";
		text = text + "oob" + oobcount + ":\n";
		regcount += 2;
		text = text + "\t%_" + (regcount - 2) + " = add i32 " + bracketExp + ", 1\n";
		text = text + "\t%_" + (regcount - 1) + " = getelementptr i32, i32* " + id + ", i32 %_" + (regcount - 2) + "\n";
		String assignExp = n.f5.accept(this, null);
		if (loaded(assignExp)) {
			assignExp = "%" + assignExp;
			text = text + "\t%_" + regcount + " = load i32, i32* " + assignExp + "\n";
			assignExp = "%_" + regcount;
			regcount += 1;
		}
		text = text + "\tstore i32 " + assignExp + ", i32* %_" + (regcount - 1) + "\n";
		text = text + "\tbr label %oob" + (oobcount+2) + "\n";
		text = text + "oob" + (oobcount+1) + ":\n";
		text = text + "\tcall void @throw_oob()\n";
		text = text + "\tbr label %oob" + (oobcount+2) + "\n";
		text = text + "oob" + (oobcount+2) + ":\n";
        oobcount += 3;
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
        int temp=store;
        store=1;
        String exp = n.f2.accept(this, null);
        store=temp;
        int tempcount=ifcount;
        ifcount += 3;
        text = text + "\tbr i1 " + exp + ", label %if" + tempcount + ", label %if" + (tempcount+1) + "\n";
		text = text + "if" + tempcount + ":\n";
        n.f4.accept(this, null);
		text = text + "\tbr label %if" + (tempcount+2) + "\n";
		text = text + "if" + (tempcount+1) + ":\n";
        n.f6.accept(this, null);
		text = text + "\tbr label %if" + tempcount+2 + "\n";
		text = text + "if" + (tempcount+2) + ":\n";
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
        int tempc=loopcount;
        loopcount += 2;
		text = text + "\tbr label %loop" + tempc + "\n";
		text = text + "loop" + tempc + ":\n";
        int temp=store;
        store=1;
        String exp = n.f2.accept(this, null);
        store=temp;
		text = text + "\tbr i1 " + exp + ", label %loop" + (tempc+1) + ", label %loop" + (tempc+2)+ "\n";
		text = text + "loop" + (tempc+1) + ":\n";
		n.f4.accept(this, null);
		text = text + "\tbr label %loop" + tempc + "\n";
		text = text + "loop" + (tempc+2) + ":\n";
        return null;
    }

     /**
     * f0 -> "//System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, Void argu) throws Exception {
        int temp=store;
        store=1;
        String exp = n.f2.accept(this, null);
        store=temp;
        text=text+ "\tcall void (i32) @print_int(i32 " + exp + ")\n";
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
     */
    public String visit(Expression n, Void argu) throws Exception {
        int temp=store;
        String exp = n.f0.accept(this, null);
        store=temp;
        return exp;
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, Void argu) throws Exception {
        int temp=store;
        store=1;
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);
        store=temp;
        if (loaded(left)) {
			left = "%" + left;
		}
		if (loaded(right)) {
			right = "%" + right;
		}
		text = text + "\t%_" + regcount + " = icmp slt i32 " + left + ", " + right + "\n";
		regcount += 1;
		return "%_" + (regcount - 1);
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, Void argu) throws Exception {
        int temp=store;
        store=1;
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);
        store=temp;
        if (loaded(left)) {
			left = "%" + left;
		}
		if (loaded(right)) {
			right = "%" + right;
		}
        text = text + "\t%_" + regcount + " = add i32 " + left + ", " + right + "\n";
		regcount += 1;
		return "%_" + (regcount - 1);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, Void argu) throws Exception {
        int temp=store;
        store=1;
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);
        store=temp;
        if (loaded(left)) {
			left = "%" + left;
		}
		if (loaded(right)) {
			right = "%" + right;
		}
        text = text + "\t%_" + regcount + " = sub i32 " + left + ", " + right + "\n";
		regcount += 1;
		return "%_" + (regcount - 1);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, Void argu) throws Exception {
        int temp=store;
        store=1;
        String left = n.f0.accept(this, null);
        String right = n.f2.accept(this, null);
        store=temp;
        if (loaded(left)) {
			left = "%" + left;
		}
		if (loaded(right)) {
			right = "%" + right;
		}
        text = text + "\t%_" + regcount + " = mul i32 " + left + ", " + right + "\n";
		regcount += 1;
		return "%_" + (regcount - 1);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, Void argu) throws Exception {
        int temps=store;
        store=1;
        String id = n.f0.accept(this, null);
        String temp = id;
		if (loaded(id)) {
			text = text + "\t%_" + regcount + " = load i32, i32* " + id + "\n";
			temp = "%_" + regcount;
			regcount += 1;
		}
        store=1;
        String exp = n.f2.accept(this, null);
        store=temps;
		if (loaded(exp)) {
            exp="%"+exp;
			text = text + "\t%_" + regcount + " = load i32, i32* " + exp + "\n";
			exp = "%_" + regcount;
			regcount += 1;
		}
		regcount += 4;
		text = text + "\t%_" + (regcount - 4) + " = icmp ult i32 " + exp + ", " + temp + "\n";
        oobcount += 3;
		text = text + "\tbr i1 %_" + (regcount - 4) + ", label %oob"+ oobcount + ", label %oob" + (oobcount+1) + "\n";
		text = text + "oob" + oobcount + ":\n";
		text = text + "\t%_" + (regcount - 3) + " = add i32 " + exp + ", 1\n";
		text = text + "\t%_" + (regcount - 2) + " = getelementptr i32, i32* " + exp + ", i32 %_" + (regcount - 3) + "\n";
		text = text + "\t%_" + (regcount - 1) + " = load i32, i32* %_" + (regcount - 2) + "\n";
		text = text + "\tbr label %oob" + (oobcount+2) + "\n";
		text = text + "oob" + (oobcount+1) + ":\n";
		text = text + "\tcall void @throw_oob()\n";
		text = text + "\tbr label %oob" + (oobcount+2) + "\n";
		text = text + "oob" + (oobcount+2) + ":\n";
		return "%_" + (regcount - 1);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, Void argu) throws Exception {
        int temp=store;
        store=1;
        String id = n.f0.accept(this, null);
        store=temp;
        text = text + "\t%_" + regcount + " = load i32, i32* " + id + "\n";
		regcount += 1;
		return "%_" + (regcount - 1);
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
        int temps=store;
        store=1;
        String name = n.f0.accept(this, null);
        store=temps;
        String cname=namereg;
		function func;
        Classes temp=symbolt.Classlist.get(cname);
        variables tempv=null;
        if(curfunc!="none"){
            tempv=symbolt.Classlist.get(CurClass.Name).functionlist.get(curfunc).Variablelist.get(cname);
        }
        else{
            tempv=symbolt.vari(cname, temp);
        }
        if(tempv==null){
            tempv=symbolt.vari(typeclass(cname),symbolt.Classlist.get(CurClass.Name));
        }
        if(tempv==null){
            temp=symbolt.Classlist.get(typeclass(cname));
        }
        else{
            temp=symbolt.Classlist.get(typeclass(tempv.type));
        }
        String classn=CurClass.Name;
        if(temp!=null){
            classn=temp.Name;
        }
        text = text + "\t; "+classn+".";
        offlag=true;
        tmpcls=classn;
        temps=store;
        store=0;
        String method = n.f2.accept(this, null);
        store=temps;
        offlag=false;
        if(temp!=null){
            func=symbolt.func(method, temp);
        }
        else{
            func=symbolt.func(method, symbolt.Classlist.get(typeclass(CurClass.Name)));
        }
		regcount += 6;
		text=text + "\t%_" + (regcount - 6) + " = bitcast i8* " + name + " to i8***\n\t%_" + (regcount - 5) + " = load i8**, i8*** %_" + (regcount - 6) + "\n\t%_" + (regcount - 4) + " = getelementptr i8*, i8** %_" + (regcount - 5) + ", i32 " + (func.offset/8) + "\n\t%_" + (regcount - 3) + " = load i8*, i8** %_" + (regcount - 4) + "\n\t%_" + (regcount - 2) + " = bitcast i8* %_" + (regcount - 3) + " to " + turni(func.Type) + " (i8*";
        int tempreg=regcount;
		for (int j = 0; j < func.argumentslist.size(); j++) {
			if ( func.argumentslist.get(j) == null ){
				continue;
            }
			text=text + "," + turni(func.argumentslist.get(j).type);
		}
		text=text + ")*\n";
        String[] callArgs=null;
        if(n.f4.present()) {            
            store=1;
            callArgs = n.f4.accept(this, null).split(",");
            store=0;
        }
		text=text + "\t%_" + (tempreg - 1) + " = call " + turni(func.Type) + " %_" + (regcount- 2) + "(i8* ";
		text=text + name;
        

		for (int j = 0; j < func.argumentslist.size(); j++) {
            text=text + ", " + turni(func.argumentslist.get(j).type) + " " + callArgs[j];
		}
		text=text + ")\n";
        namereg=func.Name;
		return "%_" + (regcount - 1);
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
     */
    public String visit(AndExpression n, Void argu) throws Exception {
        //text=text+"\nAnd Expression\n";
        int temps=store;
        store=1;
        String left = n.f0.accept(this, null);
        regcount+=2;
        int tempc=ifcount;
        ifcount+=4;
        text=text+"\tbr label %and" + tempc + "\n";
        text=text+ "and" + tempc + ":\n";
        text=text+ "\tbr i1 " + left + ", label %and" +(tempc+1) + ", label %and" + (tempc+3) + "\n";
        text=text+ "and" +(tempc+1) + ":\n";
        String right = n.f2.accept(this, null);
        store=temps;
        text=text+"\tbr label %and" + (tempc+2) + "\n";
        text=text+"and" +(tempc+2) + ":\n";
        text=text+"\tbr label %and" + (tempc+3) + "\n";
        text=text+"and" + (tempc+3) + ":\n";
        text=text+"\t%_" + (regcount - 1) + " = phi i1 [ 0, %and" + tempc + "], [ " + right + ", %and" + (tempc+2) + "]\n";

        return "%_" + (regcount - 1);
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, Void argu) throws Exception {
        int temp=store;
        String ret=n.f0.accept(this, null) + "," +  n.f1.accept(this, null);
        store=temp;
        return ret;
    
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        int temp=store;
        String ret=n.f1.accept(this, null);
        store=temp;
        return ret;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, Void argu) throws Exception {
        StringBuffer sb = new StringBuffer();
        int temp=store;
        for ( int i=0; i< n.f0.size(); i++ ) {
            sb.append(n.f0.elementAt(i).accept(this, null));
            if ( i != n.f0.size() - 1 ) sb.append(",");
        }
        store=temp;
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
        String exp=n.f0.accept(this, null);
        return exp;
    }

    public String visit(IntegerLiteral n, Void argu) throws Exception {
        return n.f0.toString();
    }

    public String visit(TrueLiteral n, Void argu) throws Exception {
        return "1";
    }
    public String visit(FalseLiteral n, Void argu) throws Exception {
        return "0";
    }
    public String visit(ThisExpression n, Void argu) throws Exception {
        namereg=CurClass.Name;
        return "%this";
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
        if (loaded(exp)) {
			exp = "%" + exp;
			text = text + "\t%_" + regcount + " = load i32, i32* " + exp + "\n";
			exp = "%_" + regcount;
			regcount += 1;
		}
		regcount += 4;
		text = text + "\t%_" + (regcount - 4) + " = icmp slt i32 " + exp + ", 0\n";
        text = text + "\tbr i1 %_"+ (regcount - 4) + ", label %arr_alloc"+ allocount + ", label %arr_alloc" + (allocount+1) + "\n";
		text = text + "arr_alloc" + allocount + ":\n";
		text = text + "\tcall void @throw_oob()\n";
		text = text + "\tbr label %arr_alloc" + (allocount+1) + "\n";
		text = text + "arr_alloc" + (allocount+1) + ":\n";		
		text = text + "\t%_" + (regcount - 3) + " = add i32 " + exp + ", 1\n";
		text = text + "\t%_" + (regcount - 2) + " = call i8* @calloc(i32 4, i32 %_" + (regcount - 3) + ")\n";
		text = text + "\t%_" + (regcount - 1) + " = bitcast i8* %_" + (regcount - 2) + " to i32*\n";
		text = text + "\tstore i32 " + exp + ", i32* %_" + (regcount - 1) + "\n";
        allocount += 2;
		return "%_" + (regcount - 1);
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, Void argu) throws Exception {
        int temp=store;
        store=0;
        String id = n.f1.accept(this, null);
        store=temp;
        namereg=id;
		Classes classCalled = symbolt.Classlist.get(id);
		regcount += 3;
		text = text + "\t%_" + (regcount - 3) + " = call i8* @calloc(i32 1, i32 " + (8 + classCalled.varOffset) + ")\n";
		text = text + "\t%_" + (regcount - 2) + " = bitcast i8* %_" + (regcount - 3) + " to i8***\n";
		text = text + "\t%_" + (regcount - 1) + " = getelementptr [" + classCalled.functionlist.size() + " x i8*], [" + classCalled.functionlist.size() + " x i8*]* @." + id + "_vtable, i32 0, i32 0\n";
		text = text + "\tstore i8** %_" + (regcount - 1) + ", i8*** %_" + (regcount - 2) + "\n";
		return "%_" + (regcount - 3);
    }


    /**
     * f0 -> "!"
     * f1 -> PrimaryExpression()
     */
    public String visit(NotExpression n, Void argu) throws Exception {
        String exp = n.f1.accept(this, null);
        text = text+ "\t%_" + regcount + " = xor i1 1, " + exp + "\n";
        regcount += 1;
        return "%_" + (regcount - 1);
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