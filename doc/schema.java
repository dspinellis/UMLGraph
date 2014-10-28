/*
 * Schema model
 * UML User Guide p. 112
 */

/** 
 * @opt operations 
 * @opt attributes 
 * @opt types 
 * @hidden
 */
class UMLOptions {}

/* Define some types we use */
/** @hidden */
class Name {}
/** @hidden */
class Number {}

/**
 * @has 1..* Member * Student
 * @composed 1..* Has 1..* Department
 */
class School {
	Name name;
	String address;
	Number phone;
	void addStudent() {}
	void removeStudent() {}
	void getStudent() {}
	void getAllStudents() {}
	void addDepartment() {}
	void removeDepartment() {}
	void getDepartment() {}
	void getAllDepartments() {}
}

/**
 * @has 1..* AssignedTo 1..* Instructor
 * @assoc 1..* - 1..* Course
 * @assoc 0..* - "0..1 chairperson" Instructor
 */
class Department {
	Name name;
	void addInstructor() {}
	void removeInstructor() {}
	void getInstructor() {}
	void getAllInstructors() {}
}

/**
 * @assoc * Attends * Course
 */
class Student {
	Name name;
	Number studentID;
}

class Course {
	Name name;
	Number courseID;
}

/**
 * @assoc 1..* Teaches * Course
 */
class Instructor {
	Name name;
}
