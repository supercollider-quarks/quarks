OptionView {
	var <composite, <views;
	var <name = "", <>type, <>specs, <>action;
	var <font, <height = 20, <labelWidth = 100;
	
	*new { |parent, bounds|	
		^super.new.init( parent, bounds );
		}
	
	init { |parent, bounds|
		bounds = bounds ? Rect( 0, 0, labelWidth + 100, height );
		composite = GUI.compositeView.new( parent, bounds );
		views = ();
		views[ \label ] = GUI.staticText.new( composite,
			Rect( composite.bounds.left, composite.bounds.top, 
			  	labelWidth, height );
			  ).string_( name.asString );
		action = { |view, value| "%:\t%\n".postf( view.name, value ); };
		}
		
	name_ { |newName| name = newName; views[ \label ].string = name; }
	font_ { |newFont| font = newFont;
		views.values.select( _.respondsTo( \font_ ) ).do( _.font_( font ) );
		}
		
	createViewsForType {
		specs = specs ? ();
		switch( type.asSymbol,
		
			'number', { 
				views[ \nbox ] = RoundNumberBox( composite, Rect( composite.bounds.left + labelWidth,
						composite.bounds.top, 50, height ) );
				views[ \nbox ].value = specs[ \current ] ? 0;
				views[ \nbox ].clipLo = specs[ \minVal ] ? -inf;
				views[ \nbox ].clipHi = specs[ \maxVal ] ? inf;
				views[ \nbox ].step = specs[ \step ] ? 1;
				views[ \nbox ].action = { |nb| action.value( this, nb.value ) };
				},
				
			'menu', { 
				views[ \popup ] = GUI.popUpMenu.new( composite, 
					Rect( composite.bounds.left + labelWidth,
						composite.bounds.top, 50.max(
							(specs[ \items ] ? ["off"])
								.collect({ |item| 
									item.asString.bounds( font ).width + 20; }).maxItem)
							, height ) );
				views[ \popup ].items = specs[ \items ] ? [ "on", "off" ];
				views[ \popup ].value = 
					views[ \popup ].items.detectIndex({ |item|
						item.asSymbol == specs[ \current ].asSymbol; })
						? 0;
				views[ \popup ].action = { |nb| action.value( this, nb.items[ nb.value ] ) };
				});
			
		}
	
	
	}