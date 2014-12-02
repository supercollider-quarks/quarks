GlobalsGui		a gui that shows all global variables in the interpreter.

	// make one
g = GlobalsGui.new; // sized for 12 items

g.close

	// make one for 8 items
g = GlobalsGui(8);

	// set some global variables - they should show up:
a = 12;
z = 8768768;
q = (a: 123, b: 234);
b = 1
c = 2
d = 4
f = 1234

// * try changing the window size - the textviews adjust. //

// * try writing into a textfield and evaluating 
// - this should set the global variables. To check, do:
this.dump


