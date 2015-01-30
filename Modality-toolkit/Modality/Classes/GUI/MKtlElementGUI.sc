MKtlElementGUI {

	classvar <>makeViewFuncDict;
	classvar <>labelWidth = 80;
	classvar <>smallLabelWidth = 20;

	var <element;
	var <>parent, <>views, <>values, <>getValueFuncs, <>skipJack;
	var <>subGUIs;

	*new { |parent, bounds, element|
		^this.newCopyArgs( element ).makeView( parent, bounds );
	}

	*initClass {
		makeViewFuncDict = (
			'button': { |parent, label|
				if( label.notNil ) {
					StaticText( parent, labelWidth@16 ).string_( label.asString ++ " " ).align_( \right );
				};
				Button( parent, if( label.notNil ) { labelWidth@16 } { 20@16 })
				.states_([[ label ? "" ],[ label ? "", Color.black, Color.gray(0.33) ]]);
			},
			'slider': { |parent, label|
				if( label.notNil ) {
					StaticText( parent, labelWidth@16 ).string_( label.asString ++ " " ).align_( \right );
				};
				Slider( parent, if( label.notNil ) { 80@20 } { 20@80 });
			},
			'knob': { |parent, label|
				if( label.notNil ) {
					StaticText( parent, labelWidth@16 ).string_( label.asString ++ " " ).align_( \right );
				};
				Knob( parent, 20@20 );
			},
			'pad': { |parent, label|
				if( label.notNil ) {
					StaticText( parent, labelWidth@16 ).string_( label.asString ++ " " ).align_( \right );
				};
				MPadView( parent, 20@20 )
					.useUpValue_( true )
					.autoUpTime_( 0.2 );
			},
			'unknown': { |parent, label|
				var vw;
				if( label.notNil ) {
					StaticText( parent, labelWidth@16 ).string_( label.asString ++ " " ).align_( \right );
				};
				vw = NumberBox( parent,
					if( label.notNil ) { 80@16 } { 30@16 }
				).clipLo_(0).clipHi_(1);
				if( vw.respondsTo( \maxDecimals_ ) ) {
					vw.maxDecimals = 4;
				};
				vw;
			},
			'midiBut': \button,
			'joyAxis': \slider,
			'springFader': \slider,
			'rumble':\slider,
			'ribbon': \slider,
			'hatSwitch': \knob,
			'encoder': \knob,
		);
	}

	makeView { |inParent, bounds|
		var createdWindow = false;
		var verboseButton;

		parent = inParent ? parent ?? {
			createdWindow = true;
			Window( element.source.name, bounds, scroll: true ).front;
		 };

		if( parent.asView.decorator.isNil ) { parent.addFlowLayout };
		
		if( createdWindow ) {
			verboseButton = Button( parent, labelWidth@16 )
				.states_([["trace"],["trace", Color.black, Color.green]])
				.action_({ |bt| element.source.trace( bt.value.booleanValue ) })
				.value_( element.source.traceRunning.binaryValue );
			parent.asView.decorator.nextLine;
			
			views = [ verboseButton ];
			values = [ element.source.traceRunning.binaryValue ];
			getValueFuncs = [ { element.source.traceRunning.binaryValue } ];
		} {
			views = [ ];
			values = [ ];
			getValueFuncs = [ ];
		};

		this.makeSubViews;

		if( createdWindow ) {
			skipJack = SkipJack( { this.updateGUI }, 0.2, { parent.isClosed } );
		};
	}

	getMakeViewFunc { |type|
		var func;
		func = makeViewFuncDict[ type ? element.type ] ?? { makeViewFuncDict[ \unknown ] };
		if( func.isKindOf( Symbol ) ) {
			func = makeViewFuncDict[ func ];
		};
		^func;
	}
	
	makeGetValueFunc { |element, view|
		var getValueFunc, value, ctrl, changed = true;
		
		ctrl = SimpleController( element )
			.put( \value, { |obj| changed = true });

		getValueFunc = { if( changed == true ) { changed = false; element.value; }; };
		value = getValueFunc.value;

		view.value_( value );
		view.onClose = view.onClose.addFunc( { ctrl.remove } );
		view.action_({ |vw|
			element.valueAction = vw.value;
			if( element.source.traceRunning == true ) {
				"% - % > % | via GUI\n".postf(
					element.source.name, element.name, element.value;
				);
			};
		});

		^getValueFunc;
	}

	makeSubViews {
		var view, getValueFunc;
		
		view = this.getMakeViewFunc( element.type ).value( parent, element.name );
		
		getValueFuncs = getValueFuncs.add( this.makeGetValueFunc( element, view ) );
		views = views.add( view );
	}

	updateGUI {
		views.do({ |view, i|
			var newValue;
			newValue = getValueFuncs[i].value;
			if( newValue.notNil ) {
				view.value = newValue
			};
		});
		subGUIs.do(_.updateGUI);
	}

}

MKtlElementGroupGUI : MKtlElementGUI {
	
	classvar <>makeSubViewsFuncDict;
	
	*initClass {
		makeSubViewsFuncDict = (
			\mixed: { |gui|
				var lastElement;
				gui.element.getElementsForGUI.pairsDo({ |key, item|
					if( item.size == 0 ) {
						if( lastElement.notNil && { lastElement.type != item.type }) {
							gui.parent.asView.decorator.nextLine;
						};
						lastElement = item;
					} {
						gui.parent.asView.decorator.nextLine;
					};
					gui.subGUIs = gui.subGUIs.add( item.gui( gui.parent ) );
				});
			},
			\pad: { |gui|
				var lastElement;
				var allElements, onElements, offElements;
				var offViews;
				var size, division;
				allElements = gui.element.flat;
				onElements = allElements.select({ |item| item.name.asString.find( "on" ).notNil });
				offElements = allElements.select({ |item| item.name.asString.find( "off" ).notNil });
				
				if( (onElements.size == 0) && (offElements.size == 0) ) {
					makeSubViewsFuncDict[ \mixed ].value( gui ); // fallback to normal behavior
				} {	
					
					size = onElements.size;
					
					if( gui.parent.asView.decorator.left != 4 ) {
						gui.parent.asView.decorator.nextLine;
					};
	
					StaticText( gui.parent, labelWidth@16 ).string_( gui.element.name.asString ++ " " ).align_( \right );
					
					if( size == 0 ) {
						onElements = offElements;
						offElements = [];
						size = offElements.size;
					};
					
					division = [8,9,10,6,7].detect({ |item|
							(size / item).frac == 0;
					}) ? 8;
					
					onElements.do({ |element, i|
						var view, getValueFunc;
	
						if( (i != 0) && ((i % division) == 0)) {
							gui.parent.asView.decorator.nextLine;
							gui.parent.asView.decorator.shift( labelWidth + 4, 0 );
						};
						
						view = MPadView( gui.parent, 20@20 ).useUpValue_(true);
						if( offElements.size == 0 ) {
							view.autoUpTime_(0.2);
						};
							
						offViews = offViews.add( MPadUpViewRedirect( view ) );
										
						gui.getValueFuncs = gui.getValueFuncs.add( gui.makeGetValueFunc( element, view ) );
						gui.views = gui.views.add( view );
					});
					
					offElements.do({ |element, i|
						var view, getValueFunc;
	
						view = offViews[ i ];
						
						if( offViews[i].notNil ) {
							gui.getValueFuncs = gui.getValueFuncs.add( gui.makeGetValueFunc( element, view ) );
							gui.views = gui.views.add( view );
						};
					});
				};
				
			},
		);
	}

	makeSubViews {
		var division, size, func;
		if( element.keys.any(_.isKindOf( Symbol )) ) {
			var func;
			func = makeSubViewsFuncDict[ element.type ] ?? { makeSubViewsFuncDict[ \mixed ] };
			func.value( this );
		} {	
			if( element.elements.any({ |item| item.size > 0 }) ) {
				subGUIs = element.elements.collect({ |element|
					element.gui( parent );
				});
			} {
				size = element.size;
	
				division = [8,9,10,6,7].detect({ |item|
						(size / item).frac == 0;
				}) ? 8;
	
				if( parent.asView.decorator.left != 4 ) {
					parent.asView.decorator.nextLine;
				};
	
				StaticText( parent, labelWidth@16 ).string_( element.name.asString ++ " " ).align_( \right );
	
				element.elements.do({ |element, i|
						var view, getValueFunc;
	
						if( (i != 0) && ((i % division) == 0)) {
							parent.asView.decorator.nextLine;
							parent.asView.decorator.shift( labelWidth + 4, 0 );
						};
	
						view = this.getMakeViewFunc( element.type ).value( parent );
											
						getValueFuncs = getValueFuncs.add( this.makeGetValueFunc( element, view ) );
						views = views.add( view );
				});
			};
		}
	}

}

+ MKtlElement {
	gui { |parent, bounds|
		^MKtlElementGUI( parent, bounds, this );
	}
}

+ MKtlElementGroup {
	gui { |parent, bounds|
		^MKtlElementGroupGUI( parent, bounds, this );
	}
}

+ MKtl {
	gui { |parent, bounds|
		^this.elements.gui( parent, bounds );
	}
}
