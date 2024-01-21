package myfiles;

public class variables {
    String Name;
    String type;
    int Value;
    boolean arg;
    boolean inclass;
    int offset;

    public void enter(String n, String t,boolean a,boolean i,boolean f){
        this.Name=n;
        this.type=t;
        this.arg=a;
        this.inclass=i;
        this.offset=0;
        if(t=="int"){
            this.Value=4;
        }
        else if(t=="boolean"){
            this.Value=1;
        }
        else{
            this.Value=8;
        }

    }

    public void print(){
        System.out.println("Variable Name = "+this.Name+ " Types = "+ this.type+" Value = "+this.Value+" arg="+arg+"in class="+inclass );
    }

}
