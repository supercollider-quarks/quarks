/*
Features to add:

+name for the verbosity of the thing that calls it to print with message, and for lookup in all
+relative verbosity to raise all verbosity levels
-gui to adjust verbosity
+verbosity redirect method to document
+time stamps for verbosity
- remember : keep a history of verbosity messages?
*/

Verbosity {

	classvar <all;
	classvar <>globalLevel = 0;
	classvar <doc;
	classvar <target = \post;
	classvar <>timeStamp = false;

	var <>level = 0;
	var <>key;
	//	var <>target = \post;

	*initClass{
		Class.initClassTree(Spec);
		all = IdentityDictionary.new;
		Spec.add( \verbosity, [0,10,\linear,1,0,"V"] );
	}

	*add{ |key,inst|
		all.put( key.asSymbol, inst );
	}

	*remove{ |key|
		all.removeAt( key.asSymbol );
	}

	*new{ |level,key|
		^super.newCopyArgs(level,key).init;
	}

	*closedDoc{
		doc = nil;
	}

	destroy{
		this.class.remove( key );
	}

	init{
		var size = all.size;
		if ( key.isNil ){
			key = ("Verbosity_"++size);
		};
		this.class.add( key, this );
	}

	*target_{ |nt|
		if ( target != nt ){
			if ( doc.notNil ){
				doc.close;
			}
		};
		target = nt;
	}

	value{ |lev,string,method|
		var postString = string;
		if ( (level+globalLevel) >= lev ){
			if ( method.notNil ){
				postString = method + ":" + postString;
			};
			postString = key + ":" + postString;
			if ( timeStamp ){
				postString = (Date.localtime.asString + "-" + string)
			}{
				postString = string;
			};
			this.class.post( postString );
		}
	}

	*post { |string|
		switch( target,
			\post, { string.postln },
			\doc, { this.createDoc; doc.string_( string ); doc.front; },
			\win, {
				this.createDoc;
				string = string ++ "\n" ++ doc.view.children.first.string;
				doc.view.children.first.string_( string );
				doc.front;
			}
		);
	}

	*createDoc {
		if (doc.isNil ){
			switch( target,
				\doc, {
					doc = Document.new( "Verbosity" );
					if ( Platform.ideName == \scapp ){
						doc.bounds_( Rect(300, 500, 500, 100) );
					};
					doc.onClose_({ this.closedDoc });
				},
				\win, {
					doc = Window( "Verbosity", Rect(300, 500, 500, 100));
					doc.addFlowLayout;
					TextView(doc, doc.bounds.resizeBy(-8, -8)).resize_(5);
					doc.onClose_({ this.closedDoc });
				});
		};
	}

	*makeGui{
		^VerbosityAllGui.new;
	}

	makeGui{
		^VerbosityGui.new( this );
	}
}

/*
USAGE:

~verbose = Verbosity.new(2,"myclass");

VerbosityAllGui.new;

g = VerbosityGui( ~verbose );

~verbose.value( 3, "level 3 verbosity");

~verbose.level_(3)

Verbosity.all
Verbosity.timeStamp = true;

Verbosity.globalLevel_( 2 )
Verbosity.target = \doc;
Verbosity.target = \win;


g = JITGui.new(~verbose, 1); // make one
g.object.key
~verbose.dump;


g.object = 123; // object gets shown asCompileString
g.object = (key: \otto); // if the object understands .key, key gets shown
g.object = Pseq([1, 2, 3], inf);
g.close;

*/