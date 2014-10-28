
/**
 * Locally disable an option (test)
 *
 * @opt operations 
 * @opt attributes 
 * @opt types 
 * @opt visibility 
 * @hidden
 */
class UMLOptions {}

/**
 * @opt !operations
 */
class Person1 {
    String address;
    String name;
    String displayName();
}

/**
 * @opt !attributes
 */
class Person2 {
    Address address;
    String name;
    String displayName();
}

/**
 * @opt !visibility
 */
class Person3 {
    Address address;
    String name;
    String displayName();
}

