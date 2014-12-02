+ SimpleNumber{
	
	asTimeCodeString{ |acc=1,fps=25| // 0 is frames, 1 is seconds, 2 is minutes, 3 is hours
		var str = "";
		var lb = ["h","m","s","f"];
		var tc = this.asTimeCode( fps );
		tc = tc.drop( -1 * acc - 1 );
		lb = lb.drop( -1 * acc );

		/*		tc = tc.select{ |it| it > 0 };
		tc.reverseDo{ |it,i,j| i.postln; str = it.asString ++ lb[tc.size-i-1] ++ str };
		*/
		tc.do{ |it,i| str = str ++ it.asString ++ lb[i] };
		^str;
	}

	// receiver is a time in frames (25fps)
	asTimeCode{ |fps=25,long=true|
		var decimal, hours, minutes, seconds, frames;
		decimal = this.asInteger;
		
		hours = (decimal.div(3600*fps));
		//		if(hours.size < 2, { hours = "0" ++ hours });
		
		minutes = (decimal.div(60*fps) % (60));
		//		if(minutes.size < 2, { minutes = "0" ++ minutes });
		
		seconds = (decimal.div(fps) % (60));
		//		if(seconds.size < 2, { seconds = "0" ++ seconds });
		
		frames = (decimal % fps);
		
		if ( long ){
			^[hours, minutes, seconds, frames, decimal ] 
		}{
			^[hours, minutes, seconds, frames ] 
		}
	}
}

+Array{
	asFrame{ |fps=25|
		^(this.at(0)*60 + this.at(1) * 60 + this.at(2) * fps + this.at(3))
	}
}