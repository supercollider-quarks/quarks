AmbientLight {

	var serial;
	var <colors;
	
	
	/*TODO: prevent nil serial / add default device*/
	*new {|serial|
		^super.new.init(serial)
	}

	init {|argSerial|
		serial = argSerial;
		colors = Color.black!3;
		colors.do{|color, i|
			this.set(i, color)
		}
	}
	/*TODO add dt-support, linear interpolation */
	set {|which(0), color(Color.black), dt(0), action|
		var floppedArgs;
	
		floppedArgs = [which, color, dt, action].flop;
		if (floppedArgs.size > 1, {
			floppedArgs.do{|args| 
				this.performList(\set, args);
			};
			^this;
		});
		
		{
			this.pr_set(which, color, dt);
	    		action.value(this);
		}.fork;
 		colors[which] = color;
	}

	pr_set {|which, color, dt = 0|
		var msg = "";
		color = color ? Color.black;
		color = color.asArray;
		(65 + (0..2) + (which * 3)).collect{|val, i|
			val.asAscii ++ ((color[i]*255).asInt + 1000).asString[1..3]
		}.do{|elem|
			msg = msg ++ elem;
		};
		serial.putAll(msg);
		dt.wait;
	}


}