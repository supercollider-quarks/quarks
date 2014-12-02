/*
Mathematically-oriented additions to SequenceableCollection.
Dan Stowell 2009.
*/
+ SequenceableCollection {	
	
	/*
	// Generates a list of co-ordinates for creating a grid in a space,
	// orthogonal to the space's axes, at axis co-ords given.
	a = meshgrid([1,2,3,4], [6,7,8,9,10]).do(_.postln);""
	a = meshgrid([1,2,3], [6,7,8], [10,12]).do(_.postln);""
	*/
	meshgrid { |...arrays|
		^this.class.meshgrid([this] ++ arrays)
	}
	
	/*
	a = Array.meshgrid([[1,2,3,4], [6,7,8,9,10]]).do(_.postln);""
	a = Array.meshgrid([[1,2,3], [6,7,8], [10,12]]).do(_.postln);""
	*/
	*meshgrid {|arrays|
		var less;
	   ^if(arrays.size==1){
	     arrays[0]
	   }{
	     less = this.meshgrid(arrays[1..]);
	     arrays[0].collect{|val|
	       less.collect{|lval| val.asArray ++ lval}
	     }.flatten;
	   };
	}
	
}
