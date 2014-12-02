////////// AllGui	- an overview for global vars and some classvars //////////

sclang has several places for keeping permanent objects:

	global variables in the interpreter (a-z)
	the currentEnvironment
	some classvars that keep all instances of that class:
		Pdef.all, Tdef.all, Pdefn.all,
		ProxySpace.all, Ndef.all,
	and potentially others, such as MKtl.

AllGui shows an overview of these: 
	the number global vars in use (a-z);
	the number of items in the currentEnvironment,
	the number of items in Tdef.all, Pdef.all, Pdefn.all, 
	and the number items in Ndef.all, and proxies in proxyspaces.


a = AllGui.new;

	// the "open" button opens the proper JITGui for each collection;
	// the number next to it sets the number of items each gui will 
	// be able to display without scrolling.

	// the numItems argument in AllGui.new sets the default numItems in the AllGui.
AllGui(8);

	// test adding things that should show up: 
a = 12;
z = 42;
~bar = 345;
~foo = 345;
Tdef(\a);	
Tdef(\b);	
Pdef(\a);
Pdef(\b);
Pdefn(\trala);
Pdefn(\hopsa);
Ndef(\a);
Ndef(\b);

p = ProxySpace(s, \p);
p[\a] = 12;
p[\kkk] = 12;


You can add and remove and other items: 

AllGui.remove(\Tdef);
AllGui.remove(\Pdef);
AllGui.remove(\Pdefn);

AllGui.new;

		// name, 	countFunc, 			openFunc
AllGui.add(\Karlo, { rrand(1, 5).postln }, { Window("Karlo!").front });

AllGui.new;



	// protect against closing
a.parent.userCanClose_(false);

	// osx: add a menu item to "Library" Menu:
CocoaMenuItem.add(["AllGui"], { AllGui.new });



	// the guis are reasonably non-overlapping / tiled:
a = AllGui(12);
AllGui.openFuncs.do(_.value(8)); // small screens
AllGui.openFuncs.do(_.value(12));




// questions
*	what other candidates that should be there?
/* 
(
Class.allClasses.do { |cl|
	cl.classVarNames.do { |cvn|
		if (cvn.asString.containsi("all")) { 
			[cl.asSymbol, cvn].postcs
		}
	}
}; "";
)
*/