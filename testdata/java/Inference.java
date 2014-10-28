
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * @opt inferrel
 * @opt inferdep
 * @opt inferdepinpackage
 * @opt attributes
 * @opt operations
 * @opt types
 * @opt collpackages java.util.*
 * @hidden
 */
class UMLOptions {}

/**
 * @opt inferreltype has 
 */
class A {
    B first;
    B second;
    C third;
}

class B {
    public A doSomething(B b, C c);
}

class C {
    List<A> collectionOfA;
    ArrayList<B> collectionOfB;
    Map<String, D> mapOfD;
    C[] childs;
    List anOpaqueList;
}

class MyFunnyList<T, V> extends ArrayList<T> {
    V myField;
}

class MyList extends MyFunnyList<A, B> {}

/**
 * @opt inferassoctype composed
 */
class D {
    MyList anotherListOfA;
}