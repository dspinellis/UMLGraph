/**
 * @opt nodefontname "Helvetica" 
 * @opt edgefontname "Helvetica" 
 * @hidden
 */
class UMLOptions{}

/**
 * @opt shape node
 * @note Located in the
 * machine room
 * @note Sun Blade 6048
 * @depend - - - MapLocation
 * @depend - - - DataMine
 */
class Server{}

/** @opt shape component */
class MapLocation {}

/** @opt shape component */
class DataMine {}

/**
 * CPU-munching
 * components that must
 * run on this server
 * @opt shape note
 * @opt commentname
 * @assoc - - - MapLocation
 * @assoc - - - DataMine
 */
class munchComment {}
