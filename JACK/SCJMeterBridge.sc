// 2005-9, Marije Baalman

SCJMeterBridge { classvar <>no, <typelist, <>prepend;

	*initClass{
		no = 0;
		prepend = "";
		typelist = [ "dpm", "vu", "ppm", "jf", "sco" ];
	}

	*new { arg channels, type, reflevel, pre, io;
		^super.new.init(channels, type, reflevel, pre, io);
	}

	*info { arg pre;
		prepend = pre ? prepend;
		(prepend++"meterbridge").unixCmd;
	}

	init { arg channels, type, reflevel, pre, io = \out;
		var meterbridge, correcttype;
		//nochan = nochan ? 1;
		//startchan = startchan ? 0;
		channels = channels ? [0];
		type = type ? "dpm";
		correcttype = false;
		prepend = pre ? prepend;
		typelist.do{ |it| if ( type == it , { correcttype = true } ) };
		if ( correcttype.not, 
			{ type = "dpm"; 
				("incorrect metertype; possible types " + typelist ++". Now displaying dpm type").postln; 
			} );
		meterbridge = prepend++"meterbridge -t"+ type + "-n SuperMeter"++no+ "-c" + channels.size;
		if ( reflevel.notNil , { meterbridge = meterbridge + "-r" + reflevel; });

		if ( io == \out ){
			channels.do{ |it,i| meterbridge=meterbridge ++ " SuperCollider:out_" ++ (it+1); };
		};
		if ( io == \in ){
			channels.do{ |it,i| meterbridge=meterbridge ++ " SuperCollider:in_" ++ (it+1); };
		};
		if ( io == \inout ){
			channels[0].do{ |it,i| meterbridge=meterbridge ++ " SuperCollider:out_" ++ (it+1); };
			channels[1].do{ |it,i| meterbridge=meterbridge ++ " SuperCollider:in_" ++ (it+1); };
		};
		meterbridge.unixCmd;
		no = no + 1;
	}
}