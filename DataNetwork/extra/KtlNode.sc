MIDIKtlNode {
	var <>ktl, <>network, <>ids, <>name;

	var <cbnodes;

	*new{ |ktl,network,ids,name|
		^super.newCopyArgs( ktl, network, ids, name ).init;
	}

	init{
		var mydict;
		cbnodes = IdentityDictionary.new;

		if ( ktl.hasScenes ){
			ktl.ktlNames.sortedKeysValuesDo{ |ky,dict,i|
				network.addExpected( ids[i], ( name ++ "_" ++ ky).asSymbol, dict.size );
				cbnodes.put( ky, SWCombineNode.new( ids[i], network, dict.size ) );
				dict.sortedKeysValuesDo{ |k2,v,j|
					network.add( name ++ "_" ++ k2, [ ids[i], j ] );
					ktl.mapS( ky, k2, { |val| cbnodes[ ky ].set( j, [ val/127 ] ); });
				};
			};
		}{
			mydict = ktl.ktlNames;
			if ( ids.isKindOf( Array ) ){
				ids = ids[0];
			};
			network.addExpected( ids, name.asSymbol, mydict.size );
			cbnodes = SWCombineNode.new( ids, network, mydict.size );
			mydict.sortedKeysValuesDo{ |k2,v,j|
				network.add( name ++ "_" ++ k2, [ ids, j ] );
				ktl.map( k2, { |val| cbnodes.set( j, [ val/127 ] ); });
			};
		}
	}
}

HIDNode {
	var <>hid, <>network, <>id, <>name;
	var <>mode;

	var <cbnode;

	*new{ |hid,network,id,name,mode=\specOnly|
		^super.newCopyArgs( hid, network, id, name, mode ).init;
	}

	init{
		var b;
		network.addExpected( id, name );
		if ( mode == \specOnly ){
			cbnode = SWCombineNode.new(id,network,hid.spec.map.size);
			hid.spec.map.sortedKeysValuesDo{ |key,it,i| 
				network.add( (name ++ "_" ++ key).asSymbol, [id, i] );
				hid[ key ].action_({ |slot| cbnode.set( i, [ slot.value ] ) });
			};
		}{
			b = hid.slots.asSortedArray.collect{ |it| it[1].asSortedArray.collect{ |jt| [it[0],jt[0]] } }.flatten;
			cbnode = SWCombineNode.new(id,network,b.size);
			b.do{ |it,i|
				hid.slots[ it[0] ][ it[1] ].action_{ |slot| cbnode.set( i, [slot.value])};
			};
			hid.spec.map.keysValuesDo{ |key,it,i| 
				var id2 = b.selectIndex( { |jt| it == jt } ).first;
				if ( id2.notNil ){ b.put( id2, key )  };
			};
			b.do{ |it,i|
				network.add( (name++"_"++it).asSymbol, [id,i] );
			};
		}
	}
}

WIINode {

	var <>wii, <>network, <>ids, <>name;

	var <cbnodes;

	*new{ |wii,network,ids,name|
		^super.newCopyArgs( wii, network, ids, name ).init;
	}

	init{
		var map,devs;
		cbnodes = IdentityDictionary.new;

		switch( wii.ext_type,
			0, { devs = [ \wii_mote ] },
			1, { devs = [ \wii_mote, \wii_nunchuk ] },
			2, { devs = [ \wii_mote, \wii_classic ] }
		);

		devs.do{ |key,i|
			map = WiiMote.devicesMap[key];
			cbnodes.put( key, SWCombineNode.new( ids[i], network, map.size ) );
			network.addExpected( ids[i], key, map.size );
			map.do{ |jt,j|
				network.add( (\wii_++jt).asSymbol, [ids[j] , j ] );
				wii.setAction( jt, { |v| cbnodes[key].set( j, [ v ] ) });
			};
		};
	}
}

