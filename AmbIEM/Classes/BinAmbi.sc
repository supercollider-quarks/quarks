/*
	Filename: BinAmbi.sc 
	created: 15.4.2005 

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


/* 	Class: BinAmbi
	Ambisonics decoder and Binaural Renderer
*/
BinAmbi { 

	// the path to Kemar
	classvar <>kemarPath = "KemarHRTF/";
	
	// the bufnums for the convolution
	classvar <>bufnums;
	
	/* 	Class method: *init
	   	intialise the buffers for convolution 
	   	Parameter
			setup: a key (symbol) to the decoding scheme
			server: the server
			doneAction: the Action to be evaluated when the buffers are loaded and ready
	*/
	*init { arg setup = '1_4_7_4', server, doneAction;

		var decoder, hrtf, decBin;

		server = server ? Server.default;
		doneAction = doneAction ? {};

		// get decode matrix 
		decoder = this.getSetup(setup); 
		if (decoder.isNil, 
			{ "BinAmbi: no setup for this name. Check DecodeAmbi.sc for available setups".postln; ^nil });

		// get the Positions and HRTFs
		Kemar.path_(kemarPath);
		hrtf = this.getPositions(setup).flop.collect({ | pos | Kemar.loadFileFor(pos[1], pos[0])});
	
		// reduce to the left ear hrtfs
		hrtf = hrtf.flop.at(0);
		
		// multiply decoder matrix and hrtfs
		decBin =  decoder.flop.collect({ |decline| (hrtf*decline).sum});
		
		// load filters to server buffers and execute doneAction when done with all
		bufnums = decBin.collect({ |filter, i| Buffer.loadCollection(server, filter, action: {
			if (i == (decBin.size - 1), { doneAction.value; });
		}).bufnum });
	}	

	/* 	Class method: *ar
	   	Audio rate method
	   	Parameter
	   		in: Array[] of Ambisonics channels (number depends on order)
	   		bufs: Array[] of bufnums containing the HRTFs
			level: Level/gain 
		Return
			out: Array[2] binaural signal 
	*/
	*ar { arg in, level=1; 
		
		var left;
		
		// check number of in channels match order 
		if (in.size != (this.order + 1).squared, 
			{ "BinAmbi: in has wrong number of channels.".postln; ^nil });
		// Do convolution and add channels according the bin matrix 
		left = Convolution2.ar(in*level, bufnums, 0, 128);
		^this.bin.collect({| row | (row*left).sum });
	}	
}

/* 	Class: BinAmbi3O
	Ambisonics decoder 3rd order and Binaural Renderer
*/
BinAmbi3O : BinAmbi { 

	classvar <order = 3;

	// Matrix that determines the combination of filtersets 
	classvar <bin;
	
	*initClass { 
		bin = [ 	[ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1], 
				[ 1, 1,-1, 1, 1,-1, 1,-1, 1, 1,-1, 1,-1, 1,-1, 1] ];
	}

	/* 	Class method: *getSetup
	   	ask DecodeAmbi3O for setups
	   	Parameter
			setup: a key (symbol) to the decoding scheme
		Return
			coeffs: Array[] the setup
	*/
	*getSetup { arg setup; ^DecodeAmbi3O.setups[setup]; }
		
	/* 	Class method: *getPositions
	   	ask DecodeAmbi3O for positions
	   	Parameter
			setup: a key (symbol) to the decoding scheme
		Return
			pos: Array[] the positions
	*/
	*getPositions { arg setup; ^DecodeAmbi3O.positions[setup]; }
	

}