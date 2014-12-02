+ SimpleNumber{
	specMap{ |from,to|
		^to.asSpec.map( from.asSpec.unmap( this ) );
	}
}

+ Collection{
	specMap{ |from,to|
		^to.asSpec.map( from.asSpec.unmap( this ) );
	}
}

+ AbstractFunction{
	specMap{ |from,to|
		^to.asSpec.map( from.asSpec.unmap( this ) );
	}
}
