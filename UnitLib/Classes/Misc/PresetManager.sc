PresetManager {
	
	classvar <>all;
	classvar <>fileExtension = "presets";

	var <>object;
	var <presets;
	var <>getFunc, <>applyFunc, <>matchFunc;
	var <>id;
	var <>lastChosen;
	var <>lastObject;
	var <>filePath;
	
	*initClass {
		all = IdentityDictionary();
	}
	
	*new { |object, presets, getFunc, applyFunc, matchFunc|
		^super.newCopyArgs().init(object, presets, getFunc, applyFunc, matchFunc).addToAll;
	}
	
	init { |inObject, inPresets, inGetFunc, inApplyFunc, inMatchFunc|
		
		object = inObject;
		
		id = object.class.name;
		
		presets = inPresets ? [];
		
		// by default the presets replace the whole object, by returning a copy
		getFunc = inGetFunc ? { |object| object.deepCopy };
		applyFunc = inApplyFunc ? { |object, preset| preset.deepCopy };
		matchFunc = inMatchFunc ? { |object, preset| object == preset };
	}
	
	addToAll {
		all.put( object, this );
	}
	
	remove { all.remove( this ) }
	
	*forObject { |object, presets, getFunc, applyFunc|
		^all[ object ] ?? { this.new( object, presets, getFunc, applyFunc ) };
	}
	
	presets_ { |newPresets|
		presets = newPresets ? [];
		this.changed( \presets );
	}
	
	clear { this.presets = []; }
	
	put { |name = \default, obj|
		this.putRaw( name, getFunc.value( obj ? object ) );
	}
	
	putRaw { |name, preset|
		var index;
		name = name.asSymbol;
		index = presets[0,2..].indexOf( name );
		if( index.notNil ) { 
			presets.put((index * 2) + 1, preset);
		} {
			presets = presets ++ [ name, preset ];
		};
		this.changed( \presets );
	}
	
	match { |obj| // returns currently used preset, if any (otherwise nil)
		var i=0, size, preset;
		obj = obj ? object;
		size = presets.size-2;
		preset = getFunc.value( obj );
		while { i <= size } {
			if( matchFunc.value( preset, presets[i+1] ) ) {
				^presets[i];
			};
			i=i+2;
		};
		^nil;
	}
	
	removeAt { |name|
		var i=0, size, res;
		name = name.asSymbol;
		size = presets.size-2;
		while { i <= size } {
			if( name === presets[i] ) {
				presets.removeAt(i); // remove key
				res = presets.removeAt(i); // remove and return preset
				this.changed( \presets );
				^res;
			};
			i=i+2;
		};
		^nil;
	}
	
	at { |name|
		var i=0, size;
		name = name.asSymbol;
		size = presets.size-2;
		while { i <= size } {
			if( name === presets[i] ) {
				^presets[i+1];
			};
			i=i+2;
		};
		^nil;
	}
	
	apply { |name = \default, obj|
		var preset;
		obj = obj ? object;
		preset = this.at( name );
		if( preset.notNil ) {
			this.lastChosen = name;
			this.lastObject = obj !? { getFunc.value( obj  ) };
			^this.applyPreset( obj, preset );
		} {
			"%:apply - preset '%' not found\n".postf( this.class, name );
			^obj;
		};
	}
	
	undo { |obj|
		var res;
		if( lastObject.notNil ) {
			res = this.applyPreset( obj, lastObject );
			this.lastObject = nil;
			this.changed( \undo );
			^res;
		} {
			"%:undo - no undo state available\n".postf( this.class );
			^obj
		};
	}
	
	applyPreset { |obj, preset|
		var res;
		res = applyFunc.value( obj ? object, preset.value );
		this.changed( \apply );
		^res;
	}
	
	write { |path, overwrite=false, ask=true, successAction, cancelAction|
	    var writeFunc;
	    writeFunc = { |overwrite, ask, path, forceExtension = true|
		    var text, extension;
		    
		    text = this.presets.cs;
		    
		    if( forceExtension ) {	
			    extension = id !? { [ id, fileExtension ].join(".") } ? fileExtension;
			    
			    if( path.find( extension ).isNil ) {
				    path = path.replaceExtension( extension );
			    };
			};
		    
		    File.checkDo( path, { |f|
				f.write( text );
				successAction.value(path);
			}, overwrite, ask);
	    };

	    case { path.isNil && (filePath.notNil) } {
		   	writeFunc.value(overwrite,ask,filePath,false);
		} { path.isNil or: (path == \browse) } {
			Dialog.savePanel( { |pth|
			    path = pth;
			    writeFunc.value(true,false,path);
		    }, cancelAction );
	    } {
		    writeFunc.value(overwrite,ask,path);
	    };
    }
    
    read { |path, action, silent = false|
	    var readFunc;
	    
	    if( path === \browse ) {
		    path = nil; 
		} {
		    path = path ? filePath; 
		};
	    
	    readFunc = { |path|
		    this.presets = path.load;
		    if( silent.not ) { "read presets from file: %".postf( path ); };
		    action.value(this);
	    };
	    
	    if( path.isNil ) {
		    Dialog.getPaths( { |paths|
			    readFunc.value(paths[0])
			})
	    } {
		    path = path.standardizePath;
		    if( File.exists( path ) ) {
		   		readFunc.value(path);
		    } {
			    if( silent.not ) { "%:read - file not found : %\n".postf( this.class, path ); };
		    };
	    };
    }
    
    readAdd { |path, action, silent = false|
	    // read a file and add presets to current (overwriting double names)
	    var readFunc, addPresets;
	    
	    if( path === \browse ) {
		    path = nil; 
		} {
		    path = path ? filePath; 
		};
	    
	    readFunc = { |path|
		    addPresets = path.load;
		    addPresets.pairsDo({ |key, value|
			    this.putRaw( key, value );
		    });
		    if( silent.not ) { "added presets from file: %".postf( path ); };
		    action.value(this);
	    };
	    
	    if( path.isNil ) {
		    Dialog.getPaths( { |paths|
			    readFunc.value(paths[0])
			})
	    } {
		    path = path.standardizePath;
		    if( File.exists( path ) ) {
		   		readFunc.value(path);
		    } {
			    if( silent.not ) { "%:read - file not found : %\n".postf( this.class, path ); };
		    };
	    };
    }
    
}