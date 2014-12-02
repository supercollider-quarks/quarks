/*
	Filename: Binaural.sc 
	created: 11.4.2005
	
	Copyright (C) IEM 2005, Christopher Frauenberger [frauenberger@iem.at] 

	This program is free software; you can redistribute it and/or 
	modify it under the terms of the GNU General Public License 
	as published by the Free Software Foundation; either version 2 
	of the License, or (at your option) any later version. 

	This program is distributed in the hope that it will be useful, 
	but WITHOUT ANY WARRANTY; without even the implied warranty of 
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
	GNU General Public License for more details. 

	You should have received a copy of the GNU General Public License 
	along with this program; if not, write to the Free Software 
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 

	IEM - Institute of Electronic Music and Acoustics, Graz 
	Inffeldgasse 10/3, 8010 Graz, Austria 
	http://iem.at
*/


/* 	Class: Binaural
	Binuaral rendering of sources - base class 
	This class is not intended to be used directly. Derived classes are available for different 
	sets of HRTFs and MUST overload the loadFileFor method. 
	Note:
		The buffers are used as class variable. This means you are forced to use the same setup 
		each instance and you should not use this class more often than once. 	
*/
Binaural { 

	// the buffer numbers for the HRTFs
	classvar <bufNums;
	
	// the buffers for the HRTFs, in stereo pairs
	classvar <bufferPairs;
	
	
	/* 	Class method: *initBuffers
		Initialises the buffers for the HRTFs for the given source directions 
		Parameter
			azi: Array of azimuth values of the sources
			elev: Array of elevation values of the sources
			server: Server for the buffers
			firstBufNum: Buffer number of the first buffer used for the HRTFs
	*/	
	*initBuffers { arg azi=0, elev=0, server, firstBufNum; 
	
		server = server ? Server.default;
	
		// check the number of azi and elev values 
		if ( azi.size != elev.size, 
			{"Binaural: Number of azimuth values and elevation values dont match, exiting.".postln; ^nil });
		
		if (firstBufNum.notNil, { bufNums = (0..(2*azi.size)-1) + firstBufNum; }, 
			{ bufNums = { server.bufferAllocator.alloc(1) } ! (2*azi.size); } );
			
		bufferPairs = bufNums.collect( { |num| Buffer.alloc(server, numFrames: 128, numChannels: 1, bufnum: num) }).clump(2); 
			
		bufferPairs.do { | bufPair, index| this.loadBuffers( azi[index], elev[index], bufPair); };
	}
	
		
	/* 	Class method: *loadBuffers
		load the HRTF set into buffers
		Parameter
			azi: The azimuth value
			elev: The elevation value
			bufPair. The number of the buffers to load the data into
	*/
	*loadBuffers { arg azi, elev, bufPair; 
		var cL, cR;
		// load the files and send them to the buffer pair
		#cL, cR = this.loadFileFor(azi, elev);
		bufPair.do { |buf, i| buf.numFrames_(cL.size).sendCollection([cL, cR][i]) };

	}
	/* 	Class method: *loadfileFor
		load the files from a HRTF set. MUST be overloaded for derived classes
		Parameter
			azi: The azimuth value
			elev: The elevation value
	*/
	*loadFileFor { arg azi, elev; 

	}
	
	/* 	Class method: *ar
		Audio rate method
		Parameter
			in: Audio input signals (number variable)
		Return
			out: Binaural stereo audio signal for headphone usage
	*/
	*ar { |in|
	
		if ( in.size != ( bufNums.size / 2), 
			{ "Binaural: buffers may not be initialised correctly, input does not match hrtf set".postln; ^nil; });
		
		// 
		^Convolution2.ar([in, in].flop, bufNums.clump(2), 0, 128).sum;
	}
}

/* 	Class: Kemar
	Binuaral rendering of sources using KEMAR data
*/
Kemar : Binaural {

	// the path to the set of HRTFs as downloaded from http://sound.media.mit.edu/KEMAR/full.tar.Z
	classvar <>path = "full/"; 
	
	// the window for HRTFs
	classvar <>window;
	
	/*	Class method: *initClass
		initialisation of the class used to calculate a window to trim and fade the HRTFs
	*/
	*initClass { 
		
		// create a window to fade in/out HRTFs with length 128
		window = Array.fill(128, { arg i;
			if (i < 14, { 1 - (cos ( i*pi/13 ) / 2 + 0.5) }, {
				if (i > 113, { ( cos( (i - 114) * pi/13 ) / 2 + 0.5) }, { 1; })
				});
		});
	}

	
	/* 	Class method: *loadFileFor
		loads the files from the HRTF set for the given pair of angles
		Parameter
			azi: The azimuth value
			elev: The elevation value
	*/
	*loadFileFor { arg azi, elev; 
		
		var fileL, fileR, cL, cR, stringAzi;

		// reformatting the azi values
		stringAzi = [azi, (360 - azi) % 360].collect({ arg a;
		if (a < 100,
			{ if (a < 10,
				{ "00" ++ a.asString; },
				{ "0" ++ a.asString; }) },
			{ a.asString; }) });

		// read the files, normalise, cut them and multiply with window
		fileL = File(path ++ "/elev" ++ elev ++ "/L"  ++ elev
			++ "e" ++ stringAzi[0] ++"a.dat","r");
		cL = fileL.read(Int16Array.newClear(fileL.length))  / 2.pow(16);
		cL = cL.copySeries(0, 1, 127) * window;	
		fileL.close; 
		
		fileR = File(path ++ "/elev" ++ elev ++ "/L"  ++ elev
			++ "e" ++ stringAzi[1] ++"a.dat","r");
		cR = fileR.read(Int16Array.newClear(fileR.length)) / 2.pow(16);
		cR = cR.copySeries(0, 1, 127) * window;
		fileR.close; 
		
		^[cL, cR];
	}
}
