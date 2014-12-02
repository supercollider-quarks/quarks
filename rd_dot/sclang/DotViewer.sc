//  DotViewer.sc - (c) rohan drape, 2004-2007

// Class to store the location to write the dot file to and the name
// of the viewer invoked to see the file.  The draw method requires a
// <SynthDef> object as its only argument.

DotViewer {
	classvar <>directory = "/tmp", <>viewer = nil, <>drawInputName = false;

	*draw {
		arg synthDef;
		var name, file, cmd, def;
		def = (osx: "open", linux: "dotty", windows: "/usr/local/bin/dotty");
		cmd = viewer;
		if(cmd.isNil, {cmd = def.at(thisProcess.platform.name)});
		name = directory.standardizePath ++ "/" ++ synthDef.name.asString ++ ".dot";
		file=File(name, "w");
		synthDef.dot(file);
		file.close;
		(cmd ++ " " ++ name ++ " &").systemCmd;
	}
}
