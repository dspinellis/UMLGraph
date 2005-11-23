// $Id$
package test;

abstract class AbstractNode {}

/**
 * @composed 1 has * test.AbstractNode
 */
class InnerNode extends AbstractNode {}

class Leaf extends AbstractNode {}
