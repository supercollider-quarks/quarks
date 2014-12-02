DeferredView {

	var <view, <>action, <objects, <value;
	
	*new { |view| 
		^super.newCopyArgs( view ).init;
		}
		
	undefer { ^view.action_( action ); }
	defer { ^this }
		
	init {
		action = view.action;
		
		view.action = { |view ...args| 
				this.updateObjects;
				action.value( this, *args ); 
				};
				
		objects = [];
		this.updateObjects;
		value = view.value;
		}

	updateObjects {
		objects = [];
		objects = view.properties.collect({ |pr|
			if( view.respondsTo( pr ) )
				{ view.perform( pr ) }
				{ view.getProperty( _ ) };
			});
		value = view.value;
		}

	value_ {  |newValue|
		if( view.respondsTo( \value_ ) ) { 
			value = newValue;
			{ view.value_(newValue) }.defer; 
			} 
		}
		
	valueAction_ { |newValue|
		this.value_( newValue );
		this.doAction;
		}
		
	propertyPairs { 
		^[view.properties, objects ].flop.flatten(1);
		}
	
	doAction { action.value( this ); }
	
	getProperty { |key, value| ^objects[ view.properties.indexOf( key ) ] ? value; }
	
	setProperty { |key, value| 
		objects[ view.properties.indexOf( key ) ] = value; 
		{ view.setProperty( key, value ) }.defer  
		}
		
	setPropertyWithAction { |key, value|
		this.setProperty( key, value );
		this.doAction;
		}
	
		
	doesNotUnderstand { |selector ... args|	
		case { view.properties.includes( selector.asGetter ) }
			{ if( selector.isSetter )
				{ this.setProperty( selector.asGetter, args[0] );
				/* objects[ view.properties.indexOf( selector.asGetter ) ] = args[0]; 
					{ view.perform( selector, *args ) }.defer 
				*/ 
				}
				{ ^this.getProperty( selector, args[0] );
				/* ^objects[ view.properties.indexOf( selector ) ]; */
				} 
			}
			{ view.respondsTo( selector ) }
			{ { view.perform( selector, *args ) }.defer }
			{ true }
			{ DoesNotUnderstandError(this, selector, args).throw; }
		}
	
	performWithAction { |selector ...args|
		this.perform( selector, *args );
		action.value( this );
		}
		
	printOn { arg stream;
		stream << this.class.name << "(";
		view.printOn( stream );
		stream << ")";
	}
	storeOn { arg stream;
		stream << this.class.name << "(";
		view.printOn( stream );
		stream << ")";
	}
	
	}