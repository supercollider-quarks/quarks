//redFrik

RedIntelHex {
	var array;
	data {
		^array.collect{|x| x[1]}.flat;
	}
	addressData {
		^array;
	}
	hexFormatString {
		var str= "";
		array.do{|x|
			str= str++x[0].asHexString(2)++":";
			x[1].do{|x| str= str++x.asHexString(2)};
			str= str++"\n";
		};
		^str;
	}
	
	*read {|path|
		^super.new.read(path);
	}
	read {|path|
		path= path.standardizePath;
		if(File.exists(path), {
			^this.prRead(path);
		}, {
			(this.class.name++": file"+path+"not found").error;
		});
	}
	
	//--private
	prRead {|path|
		var f= File(path, "r");
		var byteCount, address, recordType, dataBytes, checksum;
		var segment= 0, extended= 0;	//not thoroughly tested
		var eof= false;
		var line;
		array= [];
		
		while({line= f.getLine(266); line.notNil}, {
			
			//--check start character
			if(line[0]!=$:, {
				(this.class.name++": read error - no colon at beginning of line").warn;
			}, {
				
				//--read line
				byteCount= ("0x"++line[1..2]).interpret;
				address= ("0x"++line[3..6]).interpret;
				recordType= ("0x"++line[7..8]).interpret;
				dataBytes= [];
				checksum= ("0x"++line[byteCount*2+9]++line[byteCount*2+10]).interpret;
				
				//--messages
				switch(recordType,
					0, {	//data
						dataBytes= {|i| ("0x"++line[i*2+9]++line[i*2+10]).interpret}!byteCount;
					},
					1, {	//end of file
						eof= true;
					},
					2, {	//extended segment address
						segment= ("0x"++line[9..10]).interpret;//not thoroughly tested
					},
					3, {	//start segment address
						(this.class.name++": Start Segment Address Record not implemented").warn;
					},
					4, {	//extended linear address
						extended= ("0x"++line[9..10]).interpret;//not thoroughly tested
					},
					5, {	//start linear address
						(this.class.name++": Start Linear Address Record not implemented").warn;
					}
				);
				
				//--checksum
				if(256-(byteCount+(address%255)+recordType+dataBytes.sum.bitAnd(255))!=checksum, {
					(this.class.name++": checksum error").warn;
				});
				
				//--add to resulting array
				if(eof.not, {
					array= array.add([extended*65536+(segment*16+address), dataBytes]);
				});
			});
		});
		f.close;
		if(eof.not, {
			(this.class.name++": no end-of-file found").warn;
		});
	}
}
