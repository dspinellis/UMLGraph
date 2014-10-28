
/**
 * Associations with visibility
 * UML User Guide p. 145
 *
 * @opt horizontal
 */
package a.b.c;

class UMLOptions {}

/** @assoc * - "*\n\n+user " User */
class UserGroup {}

/** @navassoc "1\n\n+owner\r" - "*\n\n+key" Password */
class User{}

class Password{}

class Hidden{}
