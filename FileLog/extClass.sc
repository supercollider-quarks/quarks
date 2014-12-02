+ Class{

	isKindOfClass{ |otherClass|
		if ( this.asClass == otherClass ){ ^true };
		this.superclasses.do{ |it| if ( it == otherClass ){ ^true } };
		^false;
	}

}