+ Score {
	playAsTask  { arg server, clock;
		var size, osccmd, timekeep, inserver, rout;
		isPlaying.not.if({
			inserver = server ? Server.default;
			size = score.size;
			timekeep = 0;
			routine = Task({
				size.do { |i|
					var deltatime, msg;
					osccmd = score[i];
					deltatime = osccmd[0];
					msg = osccmd.copyToEnd(1);
					(deltatime-timekeep).wait;
					inserver.listSendBundle(inserver.latency, msg);
					timekeep = deltatime;
				};
				isPlaying = false;
			}, clock);
			isPlaying = true;
			routine.start;
		}, {"Score already playing".warn;}
		);
		}
		
	pause {
		isPlaying.if({routine.pause; 
			isPlaying = false; }, {"Score not playing".warn;}
		);
		}
	
	resume {
		isPlaying.not.if({routine.resume; 
			isPlaying = true; }, {"Score is already playing".warn;}
		);
		
		}
	
	}