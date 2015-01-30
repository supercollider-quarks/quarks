UGlobalControlKeySpec : Spec { 
	
	*new {
		^super.newCopyArgs();
	}
	
	map { |in| ^in }
	unmap { |in| ^in }
	
	constrain { |key|
		key = key.asSymbol;
		if( key.notNil && { UGlobalControl.current.keys.any({ |item| item == key }).not }) {
			UGlobalControl.current.put( key, 0.5 );
			"added '%' to UGlobalControl\n".postf( key );
		};
		^key;
	}
	
	default { ^UGlobalControl.current.keys[0] }
	
	makeView { |parent, bounds, label, action, resize|
		var multipleActions = action.size > 0;
		var ctrl;
		var fillPopUp, keys;
		var views;
		var font;
		views = ();
		
		font = (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		views[ \composite ] = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = views[ \composite ].asView.bounds;
		views[ \menu ] = EZPopUpMenu( views[ \composite ] , bounds.insetAll(0,0,42,0), 
			label !? { label.asString ++ " " }
		).font_( font );
		views[ \button ] = SmoothButton( views[ \composite ], 40 @ (bounds.height) )
			.label_( "edit" )
			.font_( font )
			.border_( 1 )
			.radius_( 2 )
			.action_({
				UGlobalControlGUI();
			});
		
		fillPopUp = {
			if( keys != UGlobalControl.current.keys ) {				keys = UGlobalControl.current.keys.copy;
				views[ \menu ].items = UGlobalControl.current.keys.collect({ |key|
					key -> { |vw| action.value( views, key ) }
				}) ++ [
				     '' -> { },
					'add...' -> { |vw| 
						SCRequestString( "", "please enter key name:", { |string|
							action.value( views, this.constrain( string.asSymbol ) );
						})
					}
				];
			}
		};
		fillPopUp.value;
		ctrl = { { fillPopUp.value }.defer; };
		UGlobalControl.addDependant( ctrl );
		views[ \composite ].onClose_({ UGlobalControl.removeDependant( ctrl ); });
		views[ \menu ].labelWidth = 80; // same as EZSlider
		views[ \menu ].applySkin( RoundView.skin ); // compat with smooth views
		views[ \menu ].view.resize_(2);
		views[ \button ].resize_(3);
		if( resize.notNil ) { views[ \composite ].view.resize = resize };
		^views
	}
	
	setView { |view, value, active = false|
		{  // can call from fork
			value = this.constrain( value );
			view[ \menu ].value = view[ \menu ].items.collect(_.key).indexOf( value ) ? 0;
			if( active ) { view.doAction };
		}.defer;
	}
	
	mapSetView { |view, value, active = false|
		{  // can call from fork
			view[ \menu ].value = view[ \menu ].items.collect(_.key).indexOf( value ) ? 0;
			if( active ) { view.doAction };
		}.defer;
	}

	massEditSpec { |inArray|
		var default, newList;
		default = this.massEditValue(inArray);
		newList = UGlobalControl.current.keys ++ [ "mixed" ];
		default = newList.indexOfEqual( default ) ? (newList.size-1);
		^ListSpec( newList, default )
	}
	
	massEditValue { |inArray|
		var first;
		first = inArray.first;
		if( inArray.every(_ == first) ) {
			^first;
		} {
			^"mixed";
		};	
	}

	massEdit { |inArray, params|
		if( UGlobalControl.current.keys.includes( params ) ) {
			^params.dup(inArray.size);
		} {
			if( this.massEditValue( inArray ) != "mixed" ) {
				^{ UGlobalControl.current.keys.choose }.dup(inArray.size); // randomize
			} {
				^inArray
			};
		};
	}
	
}