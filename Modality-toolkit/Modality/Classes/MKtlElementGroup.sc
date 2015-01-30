MKtlElementGroup : MAbstractElement {

	var <elements;
	var <dict;

	*new { |name, elements|
		^super.newCopyArgs( nil, name ).elements_(elements);
	}

	init {		
		var array;
		dict = dict ? ();
		elements = elements ?? { Array.new };
		case { elements.isKindOf( Dictionary ) } {
			elements.sortedKeysValuesDo({ |key, value|
				dict.put( key, value );
				array = array.add( value );
			});
			elements = array;
			this.sortElementsByType;
		} { elements.isKindOf( Array ) } {
			elements = elements.collect({ |item|
				if( item.isKindOf( Association ) ) {
					dict.put( item.key, item.value );
					item.value;
				} {
					item;
				};
			});
		};
		dict.values.do({ |item|
			if( elements.includes( item ).not ) {
				dict.remove( item );
			};
		});
		if( elements.size > 0 ) {
			type = elements.first.type;
			elements.do({ |item|
				if( addGroupsAsParent ) { item.parent = this };
				if( item.type != type ) {
					type = 'mixed';
				};
			});
		};
	}
	
	source { ^elements.first.source }
	
	sortElementsByType {
		var order;
		order = [ MKtlElement, MKtlElementGroup ];
		elements = elements.sort({ |a,b|
				(order.indexOf( a.class ) ? -1) <= (order.indexOf( b.class ) ? -1);
			}).separate({ |a,b|
				a.class != b.class
			})
			.flatten(1);
	}

	elements_ { |newElements|
		elements = newElements;
		this.init;
	}

	// array / dict manipulation support

	at { |index|
		if( index.size > 0 ) {
			^index.collect({ |item| this.at( item ) });
		} {
			^elements.detect({ |item| item.key === index }) ?? { elements[ index ]; }
		};
	}

	put { |index, element|
		this.elements = this.elements.put( index, element );
	}

	add { |element|
		this.elements = this.elements.add( element );
	}

	size { ^elements.size }

	select { |function| ^elements.select( function ) }

	collect { |function| ^elements.collect( function ) }

	inject { |thisValue, function|
		^elements.inject(thisValue, function)
	}

	asBaseClass {|recursive = true|
		^recursive.if({
			this.elements.collect{|el| el.asBaseClass(recursive)};
		},{
			elements;
		})
	}

	makePlain {
	}

	removeAll {
		elements.do(_.prRemoveGroup( this ));
		this.elements = nil;
	}

	remove { |element|
		element.prRemoveGroup( this );
		 ^this.elements.remove( element );
	}

	indexOf { |item| ^dict.findKeyForValue( item ) ?? { this.elements.indexOf( item ); } }

	do { |function| elements.do( function ); }

	flat {
		^this.elements.flat;
	}

	prFlat { |list|
		this.do({ arg item, i;
			if (item.respondsTo('prFlat'), {
				list = item.prFlat(list);
			},{
				list = list.add(item);
			});
		});
		^list
	}
	
	asArray {
		^elements.collect({ |item|
			if( item.isKindOf( MKtlElementGroup ) ) {
				item.asArray;
			} {
				item;
			};
		});
	}

	value { ^elements.collect(_.value) }
	
	keys { ^elements.collect({ |item| dict.findKeyForValue( item ) }) }
	
	shape { ^elements.shape }
	
	flop { ^elements.flopTogether } /// a bit dirty but it works

	attachChildren {
		elements.do(_.prAddGroup(this));
	}

	detachChildren { |ignoreAction = false|
		if( ignoreAction or: { action.isNil } ) {
			elements.do({ |element|
				element.prRemoveGroup( this );
				if( element.respondsTo( \detachChildren ) ) {
					element.detachChildren( ignoreAction );
				};
			});
		};
	}

	prAddGroup { |group|
		if( ( parent != group ) && { groups.isNil or: { groups.includes( group ).not } }) {
			groups = groups.add( group );
			elements.do(_.prAddGroup(this));
		};
	}

	prRemoveGroup { |group|
		if( groups.notNil ) {
			groups.remove( group );
		};
	}

	// action support
	action_ { |func|
		action = func;
		if( action.notNil ) {
			this.attachChildren;
		} {
			this.detachChildren;
		};
	}

	addAction { |argAction|
		this.action = action.addFunc(argAction);
	}

	removeAction { |argAction|
		this.action = action.removeFunc(argAction);
	}

	reset {
		this.action = nil
	}

	doAction { |...children|
		children = children.add( this );
		action.value( *children );
		parent !? _.doAction( *children );
		groups.do( _.doAction( *children ) );
		this.changed( \doAction, *children );
	}

	// tagging support
	addTag {|... newTags|
		this.collect{|elem|
			elem.addTag(*newTags);
		}
	}

	removeTag {|... newTags|
		this.collect{|elem|
			elem.removeTag(*newTags);
		}
	}

	tags {
		^this.inject(Set[], {|all, item|
			all.union(item.tags)
		})
	}

	elementsForTag {|... tag|
		^this.flat.select{|el|
			el.tags.includes(*tag)
		};
	}

	doesNotUnderstand { |selector ...args|
		var res;
		if( elements.respondsTo( selector ) ) {
			res = elements.perform( selector, *args );
			"performing %.%(%)\n".format( elements.cs, selector, args.collect(_.cs).join(", ") );
			if( res !== elements ) {
				^res;
			}
		} {
			^super.doesNotUnderstand( selector, *args );
		};
	}
	
	getElementsForGUI { ^elements.collect({ |item| [ item.key, item ] }).flatten(1); }
}