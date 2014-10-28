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

/** Test Java 5 genericity */
public class MyVector<E, P>
extends Vector<E>
implements List<E>, RandomAccess, Cloneable
{
	boolean addAll(int index, Collection<E> c) { return true; }
	MyVector<E,P> foo(MyVector<P, MyVector<E, E> > x) { return null; }
	boolean removeAll(Collection<?> c) { return true; }
	E set(int index, E element) { return null; }
}
