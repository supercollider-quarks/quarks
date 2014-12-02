////////////////////////////////////////////////////////////////////////////
//
// Copyright ANDRÉS PÉREZ LÓPEZ, September 2014 [contact@andresperezlopez.com]
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; withot even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>
//
////////////////////////////////////////////////////////////////////////////
//
// SpatialRender_synthdefs.sc
//
// Synth definitions for the Spatial Render class
// loaded in SpatialRender.init
//
////////////////////////////////////////////////////////////////////////////

+ SpatialRender {

	initSynthDefs {

		/////////////// DISTANCE ENCODERS  ///////////////
		//
		//   from SPATDIF SPECIFICATION, APPENDIX F:
		//   http://redmine.spatdif.org/attachments/download/105/SpatDIF-specs-V0.3.pdf
		//

		// air absorption models

		SynthDef(\airAbsorption0,{ |externalIn=0,busOut,gain=1,present=1|
			var sig= SoundIn.ar(externalIn,1);
			var amp= Lag.kr(Clip.kr(gain)*present);
			Out.ar(busOut,sig*amp);
		}).add;

		SynthDef(\airAbsorption1,{ |externalIn=0,r=1,busOut,gain=1,present=1|
			var sig, filterfreq;
			var amp;

			sig = SoundIn.ar(externalIn,1);
			filterfreq = 15849 + (r * (−785.71 + (r * (18.919 - (0.1668 * r)))));
			filterfreq = filterfreq.clip(0,20000); // freq above 24kHz destroy your ears!!
			sig = LPF.ar(sig,filterfreq); // 2nd order butterworth lpf

			amp= Lag.kr(Clip.kr(gain)*present);
			Out.ar(busOut,sig*amp);

		}).add;


		// distance attenuation models

		SynthDef(\distanceAttenuation0,{ |externalIn, r=1, busOut|
			var sig= In.ar(externalIn,1);
			Out.ar(busOut,sig);
		}).add;

		SynthDef(\distanceAttenuation1,{ |externalIn, r=1, refDistance=1, maxAttenuation=0.000016, maxDistance=62500, busOut|
			var sig, rof, amp;
			sig= In.ar(externalIn,1);

			rof = ( ( ( refDistance * (10.pow(-0.05 * maxAttenuation.ampdb)) ) - refDistance) / (maxDistance - refDistance));
			amp =  refDistance / ( ( (r - refDistance) * rof ) + refDistance );
			amp = Clip.kr(amp, maxAttenuation, 1);

			sig = sig * amp;

			Out.ar(busOut,sig);
		}).add;

		SynthDef(\distanceAttenuation2,{ |externalIn = 32, r=1, refDistance=1, maxAttenuation=0.000016, maxDistance=62500, busOut|
			var sig, a, amp;
			sig= In.ar(externalIn,1);

			a = maxAttenuation.ampdb / (20 * log10( refDistance / maxDistance ));
			amp = ( refDistance / r )**a;
			amp = Clip.kr(amp,maxAttenuation,1);

			sig = sig * amp;

			Out.ar(busOut,sig);
		}).add;


		//
		//
		////////////// POSITION MODELS //////////////
		//
		//

		// ambisonics point source encoders

		SynthDef(\ambiEncoder1,{ |busIn,azi=0,ele=0|
			var sig = In.ar(busIn);
			var enc = AmbEnc1.ar(sig,azi,ele);
			Out.ar(0,enc);
		}).add;

		SynthDef(\ambiEncoder2,{ |busIn,azi=0,ele=0|
			var sig = In.ar(busIn);
			var enc = AmbEnc2.ar(sig,azi,ele);
			Out.ar(0,enc);
		}).add;

		SynthDef(\ambiEncoder3,{ |busIn,azi=0,ele=0|
			var sig = In.ar(busIn);
			var enc = AmbEnc3.ar(sig,azi,ele);
			Out.ar(0,enc);
		}).add;

		// ambisonics ring source encoderes

		SynthDef(\ambiEncoder1Ring,{ |busIn,ele=0|
			var sig = In.ar(busIn);
			var enc = AmbREnc1.ar(sig,ele);
			Out.ar(0,enc);
		}).add;

		SynthDef(\ambiEncoder2Ring,{ |busIn,ele=0|
			var sig = In.ar(busIn);
			var enc = AmbREnc2.ar(sig,ele);
			Out.ar(0,enc);
		}).add;

		SynthDef(\ambiEncoder3Ring,{ |busIn,ele=0|
			var sig = In.ar(busIn);
			var enc = AmbREnc3.ar(sig,ele);
			Out.ar(0,enc);
		}).add;

		// ambisonics extended source encoders

		SynthDef(\ambiEncoder1Ext,{ |busIn,azi=0,dAzi=0,ele=0,dEle=0,preserveArea=0|
			var sig = In.ar(busIn);
			var enc = AmbXEnc1.ar(sig,azi,dAzi,ele,dEle,preserveArea);
			Out.ar(0,enc);
		}).add;

		SynthDef(\ambiEncoder2Ext,{|busIn,azi=0,dAzi=0,ele=0,dEle=0,preserveArea=0|
			var sig = In.ar(busIn);
			var enc = AmbXEnc2.ar(sig,azi,dAzi,ele,dEle,preserveArea);
			Out.ar(0,enc);
		}).add;

		SynthDef(\ambiEncoder3Ext,{ |busIn,azi=0,dAzi=0,ele=0,dEle=0,preserveArea=0|
			var sig = In.ar(busIn);
			var enc = AmbXEnc3.ar(sig,azi,dAzi,ele,dEle,preserveArea);
			Out.ar(0,enc);
		}).add;

		// ambisonics semi-meridian source encoders

		SynthDef(\ambiEncoder1Mer,{ |busIn,azi=0|
			var sig = In.ar(busIn);
			var enc = AmbSMEnc1.ar(sig,azi);
			Out.ar(0,enc);
		}).add;

		SynthDef(\ambiEncoder2Mer,{|busIn,azi=0|
			var sig = In.ar(busIn);
			var enc = AmbSMEnc2.ar(sig,azi);
			Out.ar(0,enc);
		}).add;

		SynthDef(\ambiEncoder3Mer,{ |busIn,azi=0|
			var sig = In.ar(busIn);
			var enc = AmbSMEnc3.ar(sig,azi);
			Out.ar(0,enc);
		}).add;

		/*		// vbap encoder
		{
		SynthDef(\vbapEncoder,{ |busIn,azi=0,ele=0|
		var sig = In.ar(busIn);
		var enc = VBAP.ar(vbapNumSpeakers,sig,vbapSpeakerBuffer.bufnum,azi,ele);
		Out.ar(0,enc);
		}).add;
		}.defer(1); // wait for the vbapSpeakerBuffer...

		// this.initBinauralSynthDef;*/
	}

	initVbapSynthDef{
		{
			SynthDef(\vbapEncoder,{ |busIn,azi=0,ele=0|
				var sig = In.ar(busIn);
				var enc = VBAP.ar(vbapNumSpeakers,sig,vbapSpeakerBuffer.bufnum,azi,ele);
				Out.ar(0,enc);
			}).add;
		}.defer(1); // wait for the vbapSpeakerBuffer...
	}

	initBinauralSynthDef {
		// Binaural
		{
			SynthDef(\binauralEncoder,{ |busIn,azi=0,ele=0|
				var sig = In.ar(busIn);
				var enc = FoaPanB.ar(sig,azi,ele);
				var dec = FoaDecode.ar(enc, binauralDecoder);
				Out.ar(0,dec);
			}).add;
		}.defer(1); // give time to the binauralDecoder to be loaded
	}
}