AudioUnitBuilder{
	classvar <>rez="/Developer/Tools/Rez", <>unitDict, <>displayDict;
	var name, specs, function, plistData, type, subtype;
	var <>doNoteOn=false,<>beatDiv=nil,<>port=9989,<>blockSize=64;
	var <>componentsPath = "~/Library/Audio/Plug-Ins/Components/";
	
	var xmlHead = '<?xml version="1.0" encoding="UTF-8"?>';
	var docType = '<!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">';
	var gpl = "/* SuperCollider real time audio synthesis system\n"
				"Copyright (c) 2002 James McCartney. All rights reserved.\n\n"
			"This program is free software; you can redistribute it and/or modify\n"
			"it under the terms of the GNU General Public License as published by\n"
			"the Free Software Foundation; either version 2 of the License, or\n"
				"(at your option) any later version.\n"
			"You should have received a copy of the GNU General Public License\n"
				"along with this program; if not, write to the Free Software\n"
				"Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */";

	*initClass{
		unitDict=IdentityDictionary[
		// from AudioUnitProperties.h

		\Generic->0,	/* untyped value generally between 0.0 and 1.0 */
		\Indexed-> 1,	/* takes an integer value (good for menu selections) */
		\Boolean-> 2,	/* 0.0 means FALSE, non-zero means TRUE */
		\Percent-> 3,	/* usually from 0 -> 100, sometimes -50 -> +50 */
		\Seconds-> 4,	/* absolute or relative time */
		\SampleFrames-> 5,	/* one sample frame equals (1.0/sampleRate) seconds */
		\Phase-> 6,	/* -180 to 180 degrees */
		\Rate-> 7,	/* rate multiplier, for playback speed, etc. (e.g. 2.0 ->-> twice as fast) */
		\Hertz-> 8,	/* absolute frequency/pitch in cycles/second */
		\Cents-> 9,	/* unit of relative pitch */
		\RelativeSemiTones-> 10,	/* useful for coarse detuning */
		\MIDINoteNumber-> 11,	/* absolute pitch as defined in the MIDI spec (exact freq may depend on tuning table) */
		\MIDIController-> 12,	/* a generic MIDI controller value from 0 -> 127 */
		\Decibels-> 13,	/* logarithmic relative gain */
		\LinearGain-> 14,	/* linear relative gain */
		\Degrees-> 15,	/* -180 to 180 degrees, similar to phase but more general (good for 3D coord system) */
		\EqualPowerCrossfade -> 16,	/* 0 -> 100, crossfade mix two sources according to sqrt(x) and sqrt(1.0 - x) */
		\MixerFaderCurve1-> 17,	/* 0.0 -> 1.0, pow(x, 3.0) -> linear gain to simulate a reasonable mixer channel fader response */
		\Pan-> 18,	/* standard left to right mixer pan */
		\Meters-> 19,	/* distance measured in meters */
		\AbsoluteCents-> 20,	/* absolute frequency measurement : if f is freq in hertz then */
                               /* absoluteCents -> 1200 * log2(f / 440) + 6900*/
		\Octaves				-> 21,	/* octaves in relative pitch where a value of 1 is equal to 1200 cents*/
		\BPM					-> 22,	/* beats per minute, ie tempo */
    	\Beats                  -> 23,	/* time relative to tempo, ie. 1.0 at 120 BPM would equal 1/2 a second */
		\Milliseconds		-> 24,	/* parameter is expressed in milliseconds */
		\Ratio				-> 25	/* for compression, expansion ratio, etc. */
		];
				
		displayDict=IdentityDictionary[		
			\Linear -> 0,
			\SquareRoot->1,
			\Squared->2,
			\Cubed->3,
			\CubeRoot->4,
			\Exponential->5,
			\Logarithmic->6
		];
	}
	
	*new { arg name, subtype, func, specs, type=\aumf;
		^super.new.init(name, subtype, func, specs, type);
	}
	
	init{ arg aName, aSubtype, aFunc, someSpecs, aType;
		name = aName;
		function = aFunc;
		specs = someSpecs;
		type=aType;
		plistData = this.makePluginSpec(name, function,specs);
		subtype = aSubtype;
	}
	
	
	getElement{|document, name, value|
	    var element = document.createElement(name);
	    element.appendChild(document.createTextNode(value));
	    ^element;
	}
	
	getPlistElement{|document|
	    var element = document.createElement("plist");
	    element.setAttribute("version","1.0");
	    ^element;
	}
	
	getKey{|document, name| ^this.getElement(document,"key",name);}
	getReal{|document, value| ^this.getElement(document,"real",value);}
	getString{|document, value|^this.getElement(document,"string",value);}
    
	makeServerConfig{
	    var domDoc, plist, dict, plistFile;
	    domDoc = DOMDocument.new;
	    dict = domDoc.createElement("dict");
	    dict.appendChild(this.getKey(domDoc,"PortNumber"));
	    dict.appendChild(this.getReal(domDoc,port.asString));
	    dict.appendChild(this.getKey(domDoc,"BlockSize"));
	    dict.appendChild(this.getReal(domDoc,blockSize.asString));
	    dict.appendChild(this.getKey(domDoc,"DoNoteOn"));
	    dict.appendChild(domDoc.createElement(doNoteOn.asString));
	    beatDiv.notNil.if({
	        dict.appendChild(this.getKey(domDoc,"BeatDiv"));
    	    dict.appendChild(this.getReal(domDoc,beatDiv.asString));
    	});    	
    	plist = this.getPlistElement(domDoc);
    	plist.appendChild(dict);
    	domDoc.appendChild(plist);
    	^domDoc;
	}
	
	makePluginSpec{
	    var domDoc, plist, dict, paramArray, plistFile, source, sourceNode;
	    domDoc = DOMDocument.new;
	    dict = domDoc.createElement("dict");
	    dict.appendChild(this.getKey(domDoc,"Synthdef"));
	    dict.appendChild(this.getString(domDoc,name));
	    dict.appendChild(this.getKey(domDoc,"Params"));
	    paramArray = domDoc.createElement("array");
	    specs.do({|spec,i|
	        var param = domDoc.createElement("dict");
	        param.appendChild(this.getKey(domDoc,"ParamName"));
	        param.appendChild(this.getString(domDoc,function.def.argNames[i].asString));
	        param.appendChild(this.getKey(domDoc,"MinValue"));
	        param.appendChild(this.getReal(domDoc,spec[0].asString));
	        param.appendChild(this.getKey(domDoc,"MaxValue"));	        
	        param.appendChild(this.getReal(domDoc,spec[1].asString));
	        param.appendChild(this.getKey(domDoc,"Display"));
	        param.appendChild(this.getReal(domDoc,displayDict[spec[2].asSymbol].asString));
	        param.appendChild(this.getKey(domDoc,"DefaultValue"));
	        param.appendChild(this.getReal(domDoc,spec[3].asString));
	        param.appendChild(this.getKey(domDoc,"Unit"));
	        param.appendChild(this.getReal(domDoc,unitDict[spec[4].asSymbol].asString));
	        paramArray.appendChild(param);
	    });
	    dict.appendChild(paramArray);	    
	    sourceNode = domDoc.createElement("string");
	    source = "\n%\n\nvar name, func, specs;\n\nname = %;\n\n "
	    "func = %;\n\nspecs = \n%;\n\n".format(gpl, name.quote, function.asCompileString,specs.asCompileString);
	    sourceNode.appendChild(domDoc.createCDATASection(source));
	    dict.appendChild(this.getKey(domDoc,"sourcecode"));
	    dict.appendChild(sourceNode);
	    plist = this.getPlistElement(domDoc);
    	plist.appendChild(dict);
    	domDoc.appendChild(plist);
    	^domDoc;
	}
		
	writePlist{|doc, fileName| 
	    var plistFile = File(fileName,"w");
	    plistFile.write(xmlHead);
	    plistFile.write(docType);
	    doc.write(plistFile);
	    plistFile.close;
	}
	
	copyPlugins{
	    var pipe, line, synthDef, ugens, cmd;
	    synthDef = SynthDef(name, function);
	    ugens = synthDef.children.collect({|i| i.class.name}).asSet;
	    cmd = "grep  -e _"++ugens.asSequenceableCollection.join("_ -e _") + "_ plugins/*.scx";
	    pipe = Pipe.new(cmd,"r");
        line = pipe.getLine;
        while({line.notNil}, {
            "cp % %".format(line.findRegexp("[^ ]*.scx")[0][1],(componentsPath++name++".component/Contents/Resources/plugins/")).systemCmd;
            line = pipe.getLine; 
        });
        pipe.close;
	}
	
	makePlugin{
		var synthDef, cmd, result;
		var dir = this.class.filenameSymbol.asString.dirname;
		var unixDir = dir.escapeChar($ );
		var component = "%%.component".format(componentsPath,name);
		var resources = component++"/Contents/Resources/";
		result = "cp -R %SuperColliderAU.component/ %/".format(componentsPath,component).systemCmd;		
		if(result!=0,{"Error copying %SuperColliderAU.component".format(componentsPath).postln});
		"rm -r %/plugins/*.scx".format(resources).systemCmd;
		this.writePlist(this.makePluginSpec,dir++"/pluginSpec.plist");
		this.writePlist(this.makeServerConfig,dir++"/serverConfig.plist");
		"sed -e 's/@@NAME@@/%/' -e 's/@@COMP_TYPE@@/%/' -e 's/@@COMP_SUBTYPE@@/%/' "
		"%/SuperColliderAU.r>%/tmp.r".format(name,type,subtype,unixDir,unixDir).systemCmd;
        "mv %/serverConfig.plist %".format(unixDir,resources).systemCmd;
        "mv %/pluginSpec.plist %".format(unixDir,resources).systemCmd;
        synthDef = SynthDef(name, function);
		"mkdir -p %/synthdefs".format(resources).systemCmd;
		synthDef.writeDefFile(dir++"/");		
        "mv %/%.scsyndef %/synthdefs".format(unixDir,name,resources).systemCmd;
        result= "% -o %/SuperColliderAU.rsrc -useDF %/tmp.r".format(rez,unixDir,unixDir).systemCmd;
		if(result!=0,{"Error running Rez ".postln},{"Created %".format(component).postln});
		"mv %/SuperColliderAU.rsrc % %".format(unixDir,resources).systemCmd;
		"rm %/tmp.r".format(unixDir).systemCmd;
		this.copyPlugins;
		
	}

	makeInstall{
		var cmd = "cp -r scaudk/%.component ~/Library/Audio/Plug-Ins/Components".format(name);
		this.makePlugin;
		cmd.systemCmd;
	}
}