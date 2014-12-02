+ SynthDef {
	makeAU { |identifier, changedSpecs, install = \global, cleanFiles = true, silent = true,
			superColliderAUFolder|
			
		// silent: only about the separate actions; the method will always post result
			
		// requires SuperColliderAU 0.2 ( http://supercolliderau.sourceforge.net/  )
		
		// obsolete with SCAU v0.3
		
		// Automaticly creates and installs an AudioUnit plugin from a synthdef.
		// The plugin is created in "~/scwork/AUPlugins", where all versions of it are kept. 
		// The plugin name will be the synthdef name.
		// Names > 12 chars or with underscores (_) seem to be unsupported.
		
		var pluginFolder, result;
		var componentsLib;
		
		superColliderAUFolder = 
			(superColliderAUFolder ? "/Applications/SuperColliderAU_0_2ppc/")
				.standardizePath;
				
		if( install == \global )
			{ componentsLib = "/Library/Audio/Plug-Ins/Components/".standardizePath; }
			{ componentsLib = "~/Library/Audio/Plug-Ins/Components/".standardizePath; }; 
		
		pluginFolder = (superColliderAUFolder ++ "/makecustomplugin")
			.copyRename( "~/scwork/AUPlugins/" ++ name, "Version 1", true, silent );
		identifier = identifier ? ( name.asString[..3] );
		if( pluginFolder != false )
			{ 	this.writeDefFile( pluginFolder ++ "/" );
			
				this.makeAUPluginSpec( pluginFolder, changedSpecs );
				
				result = ( "cd" + pluginFolder.quote ++ "\n" ++
					"bash makecustomplugin" + name + identifier ).systemCmd; 
					
				if( (result == 0) && { (install == \global) or: (install == \local) }  )
					{ (pluginFolder ++ "/" ++ name ++ ".component")
						.copyReplace( componentsLib, silent: silent ); 
					};
						
				if( (result == 0) && cleanFiles )
					{ [	"makecustomplugin", 
						"pluginSpec.plist", 
						"SuperColliderAU.component",
						"SuperColliderAU.r",
						name.asString ++ ".scsyndef" ].do({ |item|
							( pluginFolder ++ "/" ++ item ).removeFile( false, false, silent ); });
					};
					
				if( result != 0 )
					{ ("SynthDef-makeAU failed (result :" + result + ")" ).postln; }
					{ ("SynthDef-makeAU: succesfully created" ++ 
						( if(  (install == \global) or: (install == \local) )
							{ " and installed" } { "" } ) +
						(name.asString ++ ".component").quote + 
						"(" ++ pluginFolder.basename ++ ")" ).postln; };
				}
			{ ("SynthDef-makeAU failed:\n\t" ++
				superColliderAUFolder.asString.quote + "not found").postln; }
		}
		
	makeAUPluginSpec { |folder = "~/scwork", changedSpecs|
		var d, root, rootDict, defNameKey, defNameString;
		var paramsKey, paramsArray;
		var controls, file;
		
		// specs default to minValue = 0 / maxValue = 1
		// if defaultValue is outside this range the range will be changed accordingly
		// changedSpecs contains changed specs in the following format:
		// [ [ \paramName, minValue, maxValue, defaultValue ], [ etc.. ] ] 
		
		changedSpecs = changedSpecs ? [];
		
		controls = this.allControlNames.collect({ |name|
			var changedSpec;
			changedSpec = changedSpecs.select({ |item| 
				item[0].asSymbol == name.name.asSymbol; }).first ? [];
				
			[	name.name, 
				changedSpec[1] ? 0.min(name.defaultValue), 
				changedSpec[2] ? 1.max(name.defaultValue), 
				changedSpec[3] ? name.defaultValue 
			] 
			
			});
			
		d = DOMDocument.new;
		
		root = d.createElement( "plist" );
		root.setAttribute( "version", "1.0" );
		d.appendChild( root );
		
		rootDict = d.createElement( "dict" );
		root.appendChild( rootDict );
		
		defNameKey = d.createElement( "key" );
		defNameKey.appendChild( d.createTextNode( "Synthdef" ) );
		rootDict.appendChild( defNameKey );
		defNameString = d.createElement( "string" );
		defNameString.appendChild( d.createTextNode( name.asString ) );
		rootDict.appendChild( defNameString );
		
		paramsKey = d.createElement( "key" );
		paramsKey.appendChild( d.createTextNode( "Params" ) );
		rootDict.appendChild( paramsKey );
		paramsArray = d.createElement( "array" );
		rootDict.appendChild( paramsArray );
		
		controls.do({ |item|
			var subDict, paramNameKey, paramNameString;
			subDict = d.createElement( "dict" );
			
			paramNameKey = d.createElement( "key" );
			paramNameKey.appendChild( d.createTextNode( "ParamName" ) );
			subDict.appendChild( paramNameKey );
			
			paramNameString = d.createElement( "key" );
			paramNameString.appendChild( d.createTextNode( item[0].asString ) );
			subDict.appendChild( paramNameString );
			
			[ "MinValue", "MaxValue", "DefaultValue" ].do({ |subItem, i|
				var subItemKey, subItemValue;
				
				subItemKey = d.createElement( "key" );
				subItemKey.appendChild( d.createTextNode( subItem ) );
				subDict.appendChild( subItemKey );
				
				subItemValue = d.createElement( "real" );
				subItemValue.appendChild( d.createTextNode( item[ i+1 ].asString ) );
				subDict.appendChild( subItemValue );
				
				});
				
			paramsArray.appendChild( subDict );
			});
		
		file = File( folder.standardizePath ++ "/pluginSpec.plist", "w" );
		d.write( file );
		file.close;
		^d.format;
		}

	
	}

