// Regression test: there was a bug that made UMLGraph skip all associations
// of one kind if the first one was not to be printed (and more in general,
// to skip all those that followed the one with a hidden destination)
/**
 * @opt hide B
 * @hidden
 */
class UMLOptions {}

/**
 * @depend - - - B
 * @depend - - - C
 */
class A {}
class B {}
class C {}

