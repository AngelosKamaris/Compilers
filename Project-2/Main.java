import syntaxtree.*;
import myfiles.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }
        for(int i = 0 ; i < args.length ; i++) {


            FileInputStream fis = null;
            try{
                SymbolTable sym=new SymbolTable();

                fis = new FileInputStream(args[i]);
                System.out.println("In file: "+args[i]+"\n\n");
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                System.err.println("Program parsed successfully.");

                MyVisitor eval = new MyVisitor();
                eval.symbolt=sym;
                root.accept(eval, null);
                MySecondVisitor evaltwo = new MySecondVisitor();
                evaltwo.symbolt=sym;
                root.accept(evaltwo, null);
                sym.printall();
                System.out.println("_________________________________________\n\n");
                
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch(Exception ex){
				System.err.println(ex.getMessage());
                System.out.println("\n");
			}
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}


