/*
 * Advanced relationships
 * UML User Guide p. 137
 */

/**
 * @opt attributes
 * @opt operations
 * @hidden
 */
class UMLOptions {}

class Controller {}
class EmbeddedAgent {}
class PowerManager {}

/**
 * @extends Controller
 * @extends EmbeddedAgent
 * @navassoc - - - PowerManager
 */
class  SetTopController implements URLStreamHandler {
	int authorizationLevel;
	void startUp() {}
	void shutDown() {}
	void connect() {}
}

/** @depend - <friend> - SetTopController */
class ChannelIterator {}

interface URLStreamHandler {
	void OpenConnection();
	void parseURL();
	void setURL();
	void toExternalForm();
}
