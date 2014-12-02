+ Collection {
// replaces all items in Collection that satisfy testFunc, with the result of replaceFunc. This changes the original collection!
	/*replaceAllSuchThat { | testFunc, replaceFunc |
	//copy = this.copy;
	this.do { | item, i |
		if ( testFunc.value(item, i), 
			{
				this.put(i,replaceFunc.value( item, i ) );
			})
	};
	^this
}*/

replaceAllSuchThat { | testFunc, replaceFunc |
	//copy = this.copy;
	this.do { | item, i |
		if ( testFunc.value(item, i), 
			{
				this.put(i,replaceFunc );
			})
	};
	^this
	}
}
