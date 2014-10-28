// Author:  Vadim Nasardinov

import java.util.List;
import java.util.Map;

/**
 * @assoc "1..1" - "0..n" Adapter
 * @assoc "" - "0..n" ObjectType
 * @assoc "" - "0..n" ObjectMap
 * @assoc "" - "0..n" Table
 * @assoc "" - "0..n" DataOperation
 **/
class Root {
    private Map m_adapters;
    private List m_types;
    private List m_maps;
    private List m_tables;
    private List m_ops;

    public Adapter getAdapter(Class klass) {}
}

class Adapter {
    public Root getRoot();
}

abstract class Element {
    Root getRoot() {}
}

class ObjectType extends Element {}

/**
 * @has "1..1" - "1..1" ObjectType
 **/
class ObjectMap extends Element {
    private ObjectType m_type;
}

class Table extends Element {}

class DataOperation extends Element {}
