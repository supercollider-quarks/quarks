SWHookSet {

	var <collection;
	var <verbose;

	*new{
		^super.new.init;
	}

	init{
		collection = IdentityDictionary.new;
		verbose = Verbosity.new( 0, \swHookSet )
	}

	verbose_{ |newveb|
		verbose.destroy;
		verbose = newveb;
	}

	add{ |type,id,action,permanent=false|
		//	"adding hook to HookSet".postln;
		collection.put( (type++id).asSymbol, SWHook.new( type,id,action,permanent ) );
	}

	removeAt{ |id,type=\newnode|
		var mykey;
		mykey = (type++id).asSymbol;
		collection.removeAt( mykey );
	}

	perform{ |type,id, args|
		var myhook,mykey;
		mykey = (type++id).asSymbol;
		myhook = collection.at( mykey );
		if ( myhook.notNil ){
			myhook.perform( *args );
			verbose.value( 1, ("performing hook action" + type + id ) );
			if ( myhook.permanent.not ){
				collection.removeAt( mykey ); // remove the hook after executing it
			};
		}{
			verbose.value( 2, ("no hooks for" + type + id ) );
		};
	}

}

SWHook {

	var <>type, <>id, <>action, <>permanent = false;

	*new{ |...args|
		^super.newCopyArgs( *args );
	}

	perform{ |...args|
		this.action.value( *args );
	}

}