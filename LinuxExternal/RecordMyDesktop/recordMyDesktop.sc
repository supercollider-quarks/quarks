RecordMyDesktop{

	classvar <all;

	var <name;
	var <pid;

	*initClass{
		all = IdentityDictionary.new;
	}

	*new{ |name,v=30,s=5,n=2|
		^super.new.init(name,v,s,n);
	}

	init{ |name,v=30,s=5,nchan=2|
		var cmdline;
		name = name ? ("RecordMyDesktop"++Date.localtime.stamp);
		cmdline = "recordmydesktop -use-jack";
		nchan.do{ |i| cmdline = cmdline + "SuperCollider:out_" ++ (i+1); };
		cmdline = cmdline + "-v_quality"+v+"-s_quality"+s+"-o" + name ++ ".ogv --fps 15";
//		cmdline = cmdline + "-v_quality"+v+"-s_quality"+s+"-o" + name ++ ".ogv --on-the-fly-encoding";
		cmdline.postln;
		pid = cmdline.unixCmd;
	//	pid = ("recordmydesktop -use-jack SuperCollider:out_1 SuperCollider:out_2 -v_quality"+v+"-s_quality"+s+"-o" + name ++ ".ogv --on-the-fly-encoding").unixCmd;
		all.put( name, this );
	}

	stop{
		("kill"+pid).unixCmd;
		all.removeAt( name );
	}

}