+ SequenceableCollection {

// 	Structure this collection as a model is structured with respect to a source.
//	Source is a subset of model. This and model are of the same size.
	structureAs {| source, model| 
		^this[source.collect{|s| model.indexOf(s)}]	
	}

// this one is way slower though:
	structureAsEqual {| source, model| 
		^this[source.collect{|s| model.indexOfEqual(s)}]	
	}


}