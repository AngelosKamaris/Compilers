all: compile

compile:
	jflex scanner.flex
	java -jar java-cup-11b.jar -interface -parser Parser parser.cup
	javac -cp java-cup-11b-runtime.jar *.java

execute:
	rm -f /Main.java
	java -cp java-cup-11b-runtime.jar:. Main

clean:
	rm -f *.class *~
	rm -f output/Main.java
	rm -f output/*.class
	rm -f sym.java
	rm -f Scanner.java
	rm -f Parser.java
