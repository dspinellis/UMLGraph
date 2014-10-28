/*
 * Generalisation
 * UML User Guide p. 141
 */

/* Basic categorisations */
class Asset {}
class InterestBearingItem {}
class InsurableItem {}

/* Asset types */
/** 
 * @extends InsurableItem 
 * @extends InterestBearingItem 
 */
class BankAccount extends Asset {}
/** @extends InsurableItem */
class RealEstate extends Asset {}
class Security extends Asset {}

/* Securities */
class Stock extends Security {}
class Bond extends Security {}

/* Bank accounts */
class CheckingAccount extends BankAccount {}
class SavingsAccount extends BankAccount {}
