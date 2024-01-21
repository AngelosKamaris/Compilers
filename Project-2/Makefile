all: compile

compile:
	java -jar ./jtb132di.jar -te minijava.jj
	java -jar ./javacc5.jar minijava-jtb.jj
	javac ./myfiles/*.java
	javac Main.java

clean:
	rm -f *.class *~
	rm -f ./myfiles/*.class *~


errors:
		java Main minijava-examples-new/minijava-error-extra/BadAssign2.java   minijava-examples-new/minijava-error-extra/BadAssign.java      minijava-examples-new/minijava-error-extra/Classes-error.java   minijava-examples-new/minijava-error-extra/DoubleDeclaration1.java   minijava-examples-new/minijava-error-extra/DoubleDeclaration4.java   minijava-examples-new/minijava-error-extra/DoubleDeclaration6.java               minijava-examples-new/minijava-error-extra/NoMatchingMethod.java   minijava-examples-new/minijava-error-extra/NoMethod.java   minijava-examples-new/minijava-error-extra/Overload1.java      minijava-examples-new/minijava-error-extra/test18.java   minijava-examples-new/minijava-error-extra/test21.java   minijava-examples-new/minijava-error-extra/test35.java   minijava-examples-new/minijava-error-extra/test52.java   minijava-examples-new/minijava-error-extra/test68.java      minijava-examples-new/minijava-error-extra/UseArgs.java


extra:

	java Main minijava-examples-new/minijava-extra/Add.java minijava-examples-new/minijava-extra/ArrayTest.java minijava-examples-new/minijava-extra/CallFromSuper.java  minijava-examples-new/minijava-extra/Classes.java  minijava-examples-new/minijava-extra/DerivedCall.java  minijava-examples-new/minijava-extra/Example1.java  minijava-examples-new/minijava-extra/FieldAndClassConflict.java  minijava-examples-new/minijava-extra/Main.java  minijava-examples-new/minijava-extra/ManyClasses.java  minijava-examples-new/minijava-extra/OutOfBounds1.java  minijava-examples-new/minijava-extra/Overload2.java  minijava-examples-new/minijava-extra/ShadowBaseField.java  minijava-examples-new/minijava-extra/ShadowField.java  minijava-examples-new/minijava-extra/test06.java  minijava-examples-new/minijava-extra/test07.java  minijava-examples-new/minijava-extra/test15.java  minijava-examples-new/minijava-extra/test17.java  minijava-examples-new/minijava-extra/test20.java  minijava-examples-new/minijava-extra/test62.java  minijava-examples-new/minijava-extra/test73.java  minijava-examples-new/minijava-extra/test82.java  minijava-examples-new/minijava-extra/test93.java  minijava-examples-new/minijava-extra/test99.java

examples:

	java Main minijava-examples-new/BinaryTree.java	minijava-examples-new/BubbleSort-error.java	minijava-examples-new/BubbleSort.java	minijava-examples-new/Factorial-error.java	minijava-examples-new/Factorial.java	minijava-examples-new/LinearSearch-error.java	minijava-examples-new/LinearSearch.java	minijava-examples-new/LinkedList-error.java	minijava-examples-new/LinkedList.java	minijava-examples-new/MoreThan4-error.java	minijava-examples-new/MoreThan4.java	minijava-examples-new/QuickSort-error.java	minijava-examples-new/QuickSort.java	minijava-examples-new/TreeVisitor-error.java	minijava-examples-new/TreeVisitor.java

