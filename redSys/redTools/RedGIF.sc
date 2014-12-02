//redFrik

//related: RedBMP

RedGIF {
	var	<>type,									//string
		
		<>width, <>height,							//integers
		<>background,								//color
		<>aspectRatio,							//integer
		<>depth,									//integer
		<>globalColorMap,							//array of colors
		
		<>controls,								//array of RedGIFControl
		<>comments,								//array of strings
		
		<>appId, <>appCode,						//strings
		<>appData,								//array
		
		<>images,									//array of redgifimages
		
		dict,									//array used for lzw decompression
		codeSize,									//integer used for lzw decompression
		clearCode,								//integer used for lzw decompression
		endCode;									//integer used for lzw decompression
	
	*read {|path|
		^super.new.read(path);
	}
	read {|path|
		path= path.standardizePath;
		if(File.exists(path), {
			this.prRead(path);
		}, {(this.class.name++": file"+path+"not found").error});
	}
	
	makeWindow {|bounds|
		var b= bounds ?? {Rect(300, 300, width, height)};
		var win= Window(this.class.name, b, false);
		var index= 0, img, row;
		var drawPixel= {|x, y, col|
			if(img.control.transparentFlag.not or:{img.control.transparent!=col}, {
				Pen.fillColor= col;
				Pen.fillRect(Rect(x, y, 1, 1));
			});
		};
		var interlace= {|passes, step, offset|
			passes.do{|j|
				img.data.copyRange(row*width, row+1*width-1).do{|col, i|
					drawPixel.value(i, j*step+offset, col);
				};
				row= row+1;
			};
		};
		win.view.background= background ?? {Color.grey};
		win.drawFunc= {
			Pen.smoothing= false;
			img= images.wrapAt(index);
			if(img.interlaced, {
				row= 0;
				interlace.value((height+7).div(8), 8, 0);
				interlace.value((height+3).div(8), 8, 4);
				interlace.value((height+1).div(4), 4, 2);
				interlace.value(height.div(2), 2, 1);
			}, {
				img.data.do{|col, i|
					drawPixel.value(i%width, i.div(width), col);
				};
			});
		};
		if(images.size>1, {						//if animated gif
			Routine({
				while({win.isClosed.not}, {
					win.refresh;
					//check userinputflag here
					(img.control.duration*0.01).max(0.01).wait;
					index= index+1;
				});
			}).play(AppClock);
		});
		win.front;
		^win;
	}
	
	//--private
	prRead {|path|
		var file= File(path, "r");
		var separator;
		this.prReadHeader(file);
		if(type=="GIF87a" or:{type=="GIF89a"}, {
			this.prReadLogicalScreenDescriptor(file);
			images= [];
			while({separator= file.getInt8.bitAnd(0xff); separator!=0x3b}, {
				switch(separator,
					0x21, {this.prReadExtensionBlock(file)},
					0x2c, {this.prReadImageDescriptor(file)},
					{(this.class.name++": unknown separator"+separator).warn}
				);
			});
			images.do{|x, i|						//copy over control objects to image objects
				if(i<controls.size, {				//set corresponding control object
					x.control= controls[i];
				}, {
					if(controls.size>0, {			//use last found control object
						x.control= controls.last;
					}, {							//use default control
						x.control= RedGIFControl.new;
						x.control.duration= 0;
						x.control.transparent= Color.grey;
						x.control.disposalMethod= 0;
						x.control.userInputFlag= false;
						x.control.transparentFlag= true;
					});
				});
			};
		}, {
			(this.class.name++": type"+type+"not recognized").error;
		});
		file.close;
	}
	prReadHeader {|file|							//header with signature and version (6bytes)
		type= {file.getChar}.dup(6).join;
	}
	prReadLogicalScreenDescriptor {|file|				//logical screen descriptor (7bytes)
		var flags;
		width= file.getInt16LE;						//logical screen width
		height= file.getInt16LE;					//logical screen height
		flags= file.getInt8.bitAnd(0xff);			//packed fields
		background= file.getInt8.bitAnd(0xff);		//background colour index
		aspectRatio= file.getInt8.bitAnd(0xff);		//pixel aspect ratio
		if(aspectRatio!=0, {"aspectRatio not zero".warn});//debug
		depth= flags.bitAnd(0x07)+1;				//size of global colour table
		//resolution= flags.rightShift(4).bitAnd(0x07);//colour resolution (unused)
		globalColorMap= [];
		if(flags.bitTest(7), {						//global colour map (3bytes*numColors)
			globalColorMap= {
				Color.new255(file.getInt8.bitAnd(0xff), file.getInt8.bitAnd(0xff), file.getInt8.bitAnd(0xff));
			}.dup(2.pow(depth));
			background= globalColorMap[background];
		}, {
			background= nil;
			//(this.class.name++": no global color map").postln;//debug
		});
	}
	prReadExtensionBlock {|file|					//extension block (?bytes)
		var flags, blockSize, ctrl;
		switch(file.getInt8.bitAnd(0xff),
			0x01, {
				(this.class.name++": plain text not yet supported").warn;
				blockSize= file.getInt8.bitAnd(0xff);
				if(blockSize!=12, {"blockSize in plaintext not 12".warn});//debug
				("\ttextgrid left pos:"+file.getInt16.bitAnd(0xff)).postln;
				("\ttextgrid top pos :"+file.getInt16.bitAnd(0xff)).postln;
				("\ttextgrid width   :"+file.getInt16.bitAnd(0xff)).postln;
				("\ttextgrid height  :"+file.getInt16.bitAnd(0xff)).postln;
				("\tchar cell width  :"+file.getInt8.bitAnd(0xff)).postln;
				("\tchar cell height :"+file.getInt8.bitAnd(0xff)).postln;
				("\tforecolor index  :"+file.getInt8.bitAnd(0xff)).postln;
				("\tbackcolor index  :"+file.getInt8.bitAnd(0xff)).postln;
				while({blockSize= file.getInt8.bitAnd(0xff); blockSize!=0}, {
					"plaintext block...".postln;	//debug
					{(file.getInt8.bitAnd(0xff)).asAscii}.dup(blockSize).join.postln;
				});
			},
			0xf9, {								//graphic control extension block
				ctrl= RedGIFControl.new;
				blockSize= file.getInt8.bitAnd(0xff);
				flags= file.getInt8.bitAnd(0xff);	//packed field
				ctrl.duration= file.getInt16LE;		//delay time (1/100ths of a second)
				ctrl.transparent= globalColorMap[file.getInt8.bitAnd(0xff)];//transparent colour
				ctrl.disposalMethod= flags.bitAnd(2r00111000).rightShift(3);//undraw
				ctrl.userInputFlag= flags.bitTest(1);	//wait for user input flag
				ctrl.transparentFlag= flags.bitTest(0);//transparency flag
				controls= controls.add(ctrl);
			},
			0xfe, {								//comment extension block
				while({blockSize= file.getInt8.bitAnd(0xff); blockSize!=0}, {
					(this.class.name++": found comment block").postln;//debug
					comments= comments.add({(file.getInt8.bitAnd(0xff)).asAscii}.dup(blockSize).join);
				});
			},
			0xff, {								//application extension block
				blockSize= file.getInt8.bitAnd(0xff);
				//if(appData.notNil, {"already have application data!!!".postln});//debug
				appId= {(file.getInt8.bitAnd(0xff)).asAscii}.dup(8).join;
				appCode= {(file.getInt8.bitAnd(0xff)).asAscii}.dup(3).join;
				while({blockSize= file.getInt8.bitAnd(0xff); blockSize!=0}, {
					appData= {file.getInt8.bitAnd(0xff)}.dup(blockSize);
				});
			},
			{(this.class.name++": extensionblock - code not recognised").warn}
		);
	}
	prReadImageDescriptor {|file|					//local descriptor (10bytes)
		var flags, bounds, image, data= [];
		bounds= {file.getInt16LE}.dup(4).asRect;
		flags= file.getInt8.bitAnd(0xff);
		image= RedGIFImage(bounds, flags);
		if(image.hasColorMap, {
			image.colorMap= {
				Color.new255(file.getInt8.bitAnd(0xff), file.getInt8.bitAnd(0xff), file.getInt8.bitAnd(0xff));
			}.dup(2.pow(image.depth));
		}, {
			//(this.class.name++": no local color map - using global").postln;//debug
			image.colorMap= globalColorMap;
		});
		
		//--read table based image data
		codeSize= file.getInt8.bitAnd(0xff);
		this.prInitDict;
		image.data= this.prReadData(file).collect{|x|
			image.colorMap[x];
		};
		images= images.add(image);
	}
	prReadData {|file|
		var blockSize, data= Int8Array[];
		while({blockSize= file.getInt8.bitAnd(0xff); blockSize!=0}, {
			blockSize.do{
				data= data++(file.getInt8.bitAnd(0xff));
			};
		});
		^this.prDecode(data);
	}
	prInitDict {
		var size= 2.pow(codeSize).asInteger;
		dict= Array.newClear(4096);
		size.do{|i| dict.put(i, [i])};
		clearCode= size;
		endCode= clearCode+1;
		dict.put(clearCode, [clearCode]);
		dict.put(endCode, [endCode]);
	}
	prDecode {|arr|								//lzw decompression
		var str= Int8Array.fill(arr.size*8, {|i| arr[i.div(8)].rightShift(i%8).bitAnd(1)});
		var old, out= [], val, sub, more= true;
		var k, tempCodeSize= codeSize+1, index= 0;
		var cnt= endCode+1;
		while({more}, {
			k= 0;
			tempCodeSize.do{|i|
				k= k+(str[index]*2.pow(i));
				index= index+1;
			};
			if(k==clearCode, {
				this.prInitDict;
				cnt= endCode+1;
				tempCodeSize= codeSize+1;
				if(index>tempCodeSize, {			//first time do not shift position
					index= index+3;
				});
			}, {
				if(k==endCode, {
					more= false;
				}, {
					if(dict[k].notNil, {
						sub= dict[k];
					}, {
						sub= dict[old]++val;
					});
					out= out++sub;
					val= sub[0];
					if(old.notNil, {
						dict.put(cnt, dict[old]++val);
						cnt= cnt+1;
					});
					old= k;
					if(cnt==2.pow(tempCodeSize), {
						tempCodeSize= tempCodeSize+1;
						if(tempCodeSize==13, {
							tempCodeSize= codeSize+1;
							old= nil;
							val= nil;
						});
					});
				});
			});
		});
		^out;
	}
	
}
RedGIFImage {
	var <bounds, <flags, <>colorMap, <>data, <>control;
	*new {|bounds, flags| ^super.newCopyArgs(bounds, flags)}
	depth {^flags.bitAnd(0x07)+1}
	hasColorMap {^flags.bitTest(7)}
	interlaced {^flags.bitTest(6)}
}
RedGIFControl {
	var	<>duration,								//integer
		<>transparent,							//color
		<>disposalMethod,							//integer
		<>userInputFlag,							//boolean
		<>transparentFlag;							//boolean
}
