//redFrik

RedArduino {
	var <>intel;
	*read {|path, post= true|
		^super.new.read(path);
	}
	read {|path, post= true|
		intel= RedIntelHex.read(path);
		if(post and:{intel.addressData.notNil}, {
			intel.hexFormatString.postln;
		});
	}
	upload {|server, out= 0, amp= 1, silenceBetweenPages= 0.02, startSequencePulses= 40, manchesterNumberOfSamplesPerBit= 4|
		var encoder= RedDifferentialManchesterCodeNegative(8, 1);
		var frameCnt= 0;
		var pageIndex= 0;
		var pageHeader= 1.dup(startSequencePulses)++0;
		var pageSize= 128;
		var crc= 0x55AA;
		var list= List[];
		var array= [];
		intel.data.size.roundUp(pageSize).do{|i|
			var x= if(i<intel.data.size, {intel.data[i]}, {255});
			if(frameCnt%pageSize==0, {
				//--frame intro header
				list.add(encoder.encode(pageHeader));
				list.add(encoder.encodeArray(255-[2, pageIndex, pageIndex.rightShift(8), crc, crc.rightShift(8)].bitAnd(255)));//frameParameters
				pageIndex= pageIndex+1;
			});
			//--frame data (8 bits)
			list.add(encoder.encodeValue(255-x));
			if(frameCnt%pageSize==(pageSize-1), {
				//--frame end
				array= array.add(list.flat);
				list= List.new;
			});
			frameCnt= frameCnt+1;
		};
		pageIndex= pageIndex-1;		//bug in original code?  need to 'not' increase the pageIndex here
		pageSize.do{|i|
			var x= 255;
			if(frameCnt%pageSize==0, {
				//--frame intro header
				list.add(encoder.encode(pageHeader));
				list.add(encoder.encodeArray(255-[3, pageIndex, pageIndex.rightShift(8), crc, crc.rightShift(8)].bitAnd(255)));//frameParameters
				pageIndex= pageIndex+1;
			});
			//--frame data (8 bits)
			list.add(encoder.encodeValue(x));
			if(frameCnt%pageSize==(pageSize-1), {
				//--frame end
				array= array.add(list.flat);
				list= List.new;
			});
			frameCnt= frameCnt+1;
		};
		(this.class.name++": number of pages="+array.size).postln;
		
		//--boot server and make sound
		server= server?Server.default;
		server.waitForBoot{
			var num= pageSize+5*8+startSequencePulses+1*manchesterNumberOfSamplesPerBit;
			SynthDef(\RedArduinoUploader, {
				var data= Control.names([\data]).ir(Array.fill(num.div(2), 0));
				var src= Duty.ar(manchesterNumberOfSamplesPerBit.div(2)*SampleDur.ir, 0, Dseq(data++0), 2);
				OffsetOut.ar(out, src*amp);
			}).add;
			server.sync;
			Routine.run({
				array.do{|x, i|
					(this.class.name++": uploading page"+i).postln;
					server.bind{
						Synth(\RedArduinoUploader, [\data, x*2-1]);
					};
					(num/server.sampleRate+silenceBetweenPages).wait;
				};
			});
		};
	}
}
