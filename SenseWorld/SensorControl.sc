// SensorControl is a class to deal with a set of SensorData streams, to trigger and control something above a certain threshold, and turn off and switch to another action below the threshold.
// Created for Schwelle, www.schwelle.org
// (c) 2007-8, Marije Baalman

SensorControl{
	var <>threshold, <>fadeout, <>topamp, <>density, <>ts, <>scale;
	var <>updateValues;
	var <ison,<turn,<turningoff,<trig;
	var <updateFunc;
	var <name;
	var <>onAction, <>offonAction, <>onoffAction, <>offAction;
	var <>defaultRetAction;
	var <>weights;
	var <>dataArray;
	
	var <task, <>waitTime = 0.05;

	*new{ |data,name|
		^super.new.init(data,name);
	}
	
	longshort_{ |long|
	
		if ( long, {
			updateValues = { var val;
				val = 0;
				dataArray.do{ |it,i|
					val = val + (weights[i]*it.longStdDev);
					};
				val;
				};
			},{
			updateValues = { var val;
				val = 0;
				dataArray.do{ |it,i|
					val = val + (weights[i]*it.shortStdDev);
					};
				val;
				};
			});
	}

	init{ |data,nm|
		name = nm ? \defaultSensorControl;
		dataArray = data; // must be an array of SensorData
	
		ison = 0;
		turn = 0;
		trig = 0;
		
		defaultRetAction = {};

		turningoff = Task({ 
			turn = 1; (name+"turning off").postln;
			fadeout.wait; 
			ison = 0; (name+"turned off").postln;
			turn = 0 });
		
		weights = Array.fill( dataArray.size, 1 );
		
		updateValues = { var val;
			val = 0;
				dataArray.do{ |it,i|
					val = val + (weights[i]*it.longStdDev);
			};
			val;
		};

		updateFunc = {
			var returnAction;
			var val = updateValues.value ;
			if ( ison == 0, {
				if ( val > threshold,
					{
						(name+"turning on").postln;
						ison = 1;
						if ( offonAction.notNil, { returnAction = offonAction; })
						
					},{ // while off
						if ( offAction.notNil, { returnAction = offAction; } )
					});
				},{
					if ( val > threshold,
						{ // while on
							if ( onAction.notNil, { returnAction = onAction; });
						},{  // turning off
							if ( turn == 0,
								{
								turningoff.reset.play; 
								if ( onoffAction.notNil, { returnAction = onoffAction; } );
								});
						});
				});
			if ( returnAction.isNil, { returnAction = defaultRetAction; } );
			returnAction.value( val );
		};
		task = Task.new( { loop{ (this.updateFunc).value; this.waitTime.wait; } } );
	}
	
	start{
		task.start;
	}
	
	stop{
		task.stop;
	}
}