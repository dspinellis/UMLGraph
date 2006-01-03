// $Id$

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * @opt inferassoc
 * @opt inferdep
 * @opt attributes
 * @opt operations
 * @opt types
 * @hidden
 */
class UMLOptions {}

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
    Map<String, B> mapOfB;
    C[] childs;
    List anOpaqueList;
}

class MyFunnyList<T, V> extends ArrayList<T> {
    V myField;
}

class MyList extends MyFunnyList<A, B> {}

class D {
    MyList anotherListOfA;
}