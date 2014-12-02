/// DynamicScaleSystem is dynamically scaling a system of SensorData
/// created for Schwelle, www.schwelle.org
/// (c) 2007, Marije Baalman

DynamicScaleSystem{
	var <data;
	
	*new{ |data|
		^super.new.init( data );
	}
	
	init{ |dat|
		data = dat ? Array.fill( 5, {DynamicScaleData.new} );
	}

	test{
		var maxscale = 0;
		var minscale = 100000;
		data.do{ |it,i|
			it.test;
			if ( maxscale < it.scale, { maxscale = it.scale } );
			if ( minscale > it.scale, { minscale = it.scale } );
		};
		data.do{ |it,i|
			it.scale = minscale;
		};
	}

	output{
		^data.collect{ |it| it.output };
	}
}

/// DynamicScaleData is dynamically scaling one set of SensorData.
DynamicScaleData{
	var <>data;
	var <>scale = 1;
	var <>top = 1.1;
	var <>bottom = 0.9;
	var <>scalefact = 0.75;

	*new{ |data|
		^super.new.init( data );
	}

	init{ |dat|
		data = dat ? SensorData.new;
	}

	test{
			if ( data.fluctuation > top,
				{
					scale = scalefact / data.longStdDev;
				});
			if ( data.fluctuation < bottom,
				{
					scale = scalefact / data.longStdDev;
				});
	}

	output{
		^(data.shortStdDev * scale)
	}
}