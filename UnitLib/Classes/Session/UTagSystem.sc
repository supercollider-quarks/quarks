UTagSystem {
	
	// a global referencing system based on tag names
	
	// - one tag name can point to multiple objects
	// - an object can also have multiple tag names
	// - tags are not copied when an object is copied
	
	classvar <>objectDict, <>tagDict;
	
	*initClass {
		objectDict = IdentityDictionary();
		tagDict = IdentityDictionary()	
	}
	
	*defaultTag { ^\default }
	
	*add { |object, tag|
		tag = (tag ? this.defaultTag).asSymbol;
		objectDict[ object ] = (objectDict[ object ] ?? { IdentitySet() }).add( tag );
		tagDict[ tag ] = (tagDict[ tag ] ?? { IdentitySet() }).add( object );
	}
	
	*remove { |object, tag|
		tag = (tag ? this.defaultTag).asSymbol;
		objectDict[ object ].remove( tag );
		this.at( tag ).remove( object );
		if( this.at( tag ).size == 0 ) {
			this.removeKey( tag );
		};
	}
	
	*removeObject { |object|
		var tags;
		tags = objectDict[ object ];
		if( tags.notNil ) {
			objectDict[ object ] = nil;
			tags.do({ |tag|
				tagDict[ tag ].remove( object );
				if( tagDict[ tag ].size == 0 ) {
					tagDict[ tag ] = nil;
				};
			})
		};
	}
	
	*removeTag { |tag|
		var objects;
		tag = (tag ? this.defaultTag).asSymbol;
		objects = tagDict[ tag ];
		if( objects.notNil ) {
			tagDict[ tag ] = nil;
			objects.do({ |object|
				objectDict[ object ].remove( tag );
				if( objectDict[ object ].size == 0 ) {
					objectDict[ object ] = nil;
				};
			})
		};
	}
	
	*at { |tag| ^this.atTag( tag ); }
	*getTags { |object| ^this.atObject( object ); }
	
	*atTag { |tag| ^tagDict[ tag.asSymbol ]; }
	*atObject { |object| ^objectDict[ object ]; }
	
	*objects { ^objectDict.keys }
	*tags { ^tagDict.keys }
	
	*keys { ^this.tags } // synonym
	
	*clear { 
		tagDict.clear;
		objectDict.clear;
	}
}