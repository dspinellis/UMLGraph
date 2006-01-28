package gr.spinellis.context.classes;

import javax.swing.JComponent;

class ABaseClass {
    
}

public class A extends ABaseClass {
    B b;
    JComponent component;
    
    public E getChild() {};
}

class B {
    C c;
}

/**
 * Linked indirectly, won't be rendered
 */
class C {
    
}

class D {
    A a;
}

class E {
    
}

class ASubclass extends A {
    
}

class AClient {
    public void compute(A a);
}

/**
 * This won't be rendererd since it's not linked to the A 
 */
class AloneInTheDark {
    
}
