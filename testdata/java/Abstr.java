package test;

/**
 * @hidden
 * @opt operations
 */
class UMLOptions{}

abstract class AbstractNode {
    public abstract void abstractMethod();
    public int concreteMethod() { return 1; }
}

/**
 * @composed 1 has * test.AbstractNode
 */
class InnerNode extends AbstractNode {}

class Leaf extends AbstractNode {}
