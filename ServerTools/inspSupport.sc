

+ Object {

	insp { arg  ... args;
		Insp(this,args);
	}
	// gui into the Insp tabbed browser
	// this means to front it right away
	// where insp just adds it to the sidebar
	ginsp { arg  ... args;
		Insp(this,args,true);
	}
}

+ Class {
	guiClass { ^ClassGui }
}

+ Method {
	guiClass { ^MethodGui }
}

