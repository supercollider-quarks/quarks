SWMiniBeeOSCGui : ObjectGui {
	var <id, <inputs, <outputs, <configID, <serialNumber, <status;

	guiBody { arg layout;

		id = EZNumber.new( layout, 60@20, "id", [1,1000,\linear,1].asSpec, initVal: model.id, labelWidth: 25 );
		status = EZText.new( layout, 100@20, "status", initVal: model.status, labelWidth: 40 );
		inputs = EZNumber.new( layout, 80@20, "inputs", [1,100,\linear,1].asSpec, initVal: (model.inputs ?? 0), labelWidth: 40 );
		outputs = EZNumber.new( layout, 90@20, "outputs", [1,100,\linear,1].asSpec, initVal: (model.outputs ?? 0), labelWidth: 50 );
		configID = EZNumber.new( layout, 80@20, "config", [1,100,\linear,1].asSpec, initVal: (model.configID ?? 0), labelWidth: 40 );
		serialNumber = EZText.new( layout, 180@20, "serial", initVal: (model.serialNumber ?? 0), labelWidth: 40 );
	}

	update{ arg changed,changer;
		
		if(changer !== this,{
			defer{
				id.value_( model.id );
				status.value_( model.status );
				inputs.value_( model.inputs ?? 0 );
				outputs.value_( model.outputs ?? 0 );
				configID.value_( (model.configID ?? 0) );
				serialNumber.value_( (model.serialNumber ?? 0) );
			};
		});
	}

}

SWDataNetworkOSCHiveClientGui : ObjectGui {
	
	guiBody{ arg layout;
		model.activeBees.keys.asArray.sort.do{ |it|
			layout.startRow;
			model.activeBees[ it ].gui(layout);
		}
	}

}