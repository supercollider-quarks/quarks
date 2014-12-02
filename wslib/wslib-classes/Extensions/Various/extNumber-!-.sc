// wslib 2009
// reversed '-' and '/'
//
// (a !- b) == (b - a)

+ Object {
	!- { arg aNumber, adverb; ^aNumber.perform( '-', this, adverb ) }
	!/ { arg aNumber, adverb; ^aNumber.perform( '/', this, adverb ) }
	} 
