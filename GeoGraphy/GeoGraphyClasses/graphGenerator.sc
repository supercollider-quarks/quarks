// A class containing all the possible generating algorithms for creating graphs
// Better use this instead of cluttering Graph

GraphGenerator {
	
	// a temporal asymmetrycal cycle on an array of labels
	//	 a -> b -> c -> d ------------> a
	// NOTE: if you duplicate the same label in labelArray you can have loops
	*asymmetricalCycle { arg labelArray, minVal = 0.1, maxVal = 1.0, asymmetricalFactor = 3 ;
		var graph = Graph.new ;
		var parser = GraphParser.new(graph) ;
		var label, next, str ;
		(labelArray.size-1).do({ arg index ;
			label = labelArray[index] ;
			next = labelArray[index+1] ;
			str = "e+"+label+rrand(minVal, maxVal).asString+next ;
			parser.parse(str) ;
			}) ;
		parser.parse("e+"
			+ labelArray[labelArray.size-1]
			+ rrand(minVal*asymmetricalFactor, maxVal*asymmetricalFactor)
				.asString
		 	+ labelArray[0]) ;
		^graph
		} 
	 

}