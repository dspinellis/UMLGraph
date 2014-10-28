/*
 * Relationship inference
 */

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @opt inferrel
 * @opt collpackages java.util.*
 * @opt inferdep
 * @opt inferdepinpackage
 * @opt hide java.*
 * @hidden
 */
class UMLOptions {}

class Person {
    House[] houses;
    List<Dog> dogs;
    
    public Room getFavouriteRoom(House house) {}
}

/**
 * @opt inferreltype composed
 */
class House {
    Map<String, Room> nameRoomMap;
}

class Room {}

class Dog {
    Person owner;
}
