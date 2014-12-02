+ GeneralHIDDevice {

	addToNetwork{ |network,id,prefix=''|
		var b;
		b = this.slots.asSortedArray.collect{ |it| it[1].asSortedArray.collect{ |jt| [it[0],jt[0]] } }.flatten;
		//	b.dump;
		this.spec.map.keysValuesDo{ |key,it| 
			var id2 = b.selectIndex( { |jt| it == jt } ).first;
			if ( id2.notNil ){ b.put( id2, key )  };
		};

		b.do{ |it,i|
			network.add( (prefix++it).asSymbol, [id,i] );
		};

		this.action = { network.setData( id, 
			this.slots.asSortedArray.collect{ |it| 
				it[1].asSortedArray.collect{ |jt| jt.at(1).value };
			}.reject{ |it| it.isNil }.flatten ); };

	}

}