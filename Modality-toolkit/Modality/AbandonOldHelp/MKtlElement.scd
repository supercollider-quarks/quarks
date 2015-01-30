MKtlElement




MIDIMKtl.find

b = MIDIMKtl('launchpad1', 1258389554);  // Launchpad
b = MIDIMKtl('launchpad1', 593081660);  // Launchpad

b.verbose = true
a = MKtlElement(b, \bu0);
b.verbose = false


a.addFuncAfter(\test, {|... args| args.postln}, nil)
a.value_(12)

HIDMKtl.find;

HIDMKtl('ferrari', 102760448);  // Run'N' Drive

	//trivial case:
	// make one-dimensional elements, fron devSpecs: 
	// lookup specs etc by name used in devSpec

	// fails if nanoKONTROL not present;
a = MKtlElement(MKtl(\nk1), \sl1);

x = MKtlElement(MKtl(\ferrari), \joyLX,   \joyAxis);
x.devSpec.spec.postcs;
x.spec.default;
x.dump

y = MKtlElement(MKtl(\ferrari), \joyLY,   \joyAxis);
y.dump

h = MKtlElement(MKtl(\ferrari), \joyLHat, \button );
h.dump




	// simple tests
x.addFunc(\postX, { |...args| args.postln; });
x.funcChain.add(\postX, { |...args| args.postln; });

y.addFunc(\postX, { |...args| args.postln; });
h.addFunc(\postX, { |...args| args.postln; });

x.removeFunc(\postX);
y.removeFunc(\postX);
h.removeFunc(\postX);


	// composite elements: 	
a = MKtlElement(MKtl(\ferrari), \joyL, \thumbstick -> [\joyLX, \joyLY, \joyLHat]);

a.value(\x)
	// current x-axis value
	
a.value(\y)
	// current y-axis value

a.value(\angle)
	// current angle (calculated)

a.value(\radius)
	// current radius (calculated)

a.value(\polar)
	// current angle + radius (calculated)

a.value
	// current values (only raw, non-calculated ones)




a.addFunc(\polar, {|arg| arg.postln})

a.addFunc(\x, {|arg| arg.postln})
