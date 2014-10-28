import java.util.*;

/**
 * @opt attributes
 * @opt operations
 * @opt visibility
 * @opt noqualify
 * @opt types
 * @opt enumerations
 * @opt enumconstants
 * @hidden
 */
class UMLOptions {}

/** Test Java 5 features */
class Java5 {
	/** Enum */
	enum States {start, dash, colon, space, open, w, close};
	States state = States.start;
	/** Generics */
	public java.util.Set<java.lang.String> specifiedPackages;
	/** Varargs */
	public static void printAll(String... args) {
		for (java.lang.String n : args)
			System.out.println("Hello " + n + ". ");
	}
}
