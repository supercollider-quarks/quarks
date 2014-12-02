// wslib 2006/2007
// requires DOMDocument classes from swiki

// SVG is a xml-based format for vector graphics
// SVG-files can be read and written with Adobe illustrator and many other
// graphic applications. There are also plugins for browser display.

// this class is based on the specification as found on the w3c site:
// http://www.w3.org/TR/SVG11/
// and checked with Adobe's SVG web-browser plugin and Illustrator 

// the Pen class methods in sc look a lot like the instructions found in 
// SVG files, so almost all vector elements can be drawn in SCWindows. (SVGXXX-plot methods) 

// the aim is to be able to display svg files and extract their contents
// for use in sc, and vice versa (encode and export). The export can be useful for
// scoring sc events and generating visual feedback from objects. The SVGObjects can also
// be used as placeholders for graphic events within sc. 




SVGFile {
	
	var <>path, <>objects;
	var <>width, <>height, <>userUnit = \px;
	var <domDocument, <domRoot;
	
	*new { |path, objects, width, height|
		path = (path ? "~/scwork/test.svg").standardizePath;
		path.replaceExtension( "svg" );
		objects = objects ? [];
		^super.newCopyArgs( path, objects, width ? 400, height ? 400 );
		}
		
	*read { |path| ^SVGFile( path ).read; }
	
	*parseXML { |xml = "", path| ^SVGFile( path ).parseXML( xml ); }
	
	*headerFilePath { ^this.class.filenameSymbol.asString.dirname ++ "/SVGHeader.txt"; }
		
	write { arg overwrite= false, ask= true, postFormat = true;
		var root, gTag, wfsPathTag;
		var d,f;
		
		d = DOMDocument.new; // create empty XML document
		root = d.createElement("svg");
		if( width.notNil ) { root.setAttribute( "width", width.asString ); };
		if( height.notNil ) { root.setAttribute( "height", height.asString ); };
		
		// xmlns="&ns_svg;" --> needed for FireFox 2.0 (and others?)
		root.setAttribute( "xmlns", "&ns_svg;" );
		
		d.appendChild(root);
		objects.do({ |object| object.asDOMElement( d, root ) });
		
		if(path.notNil)
			{ File.checkDo( 
				PathName(path.standardizePath).extension_("svg").fullPath, //force svg extension
				{ |f| var txt, header;
				txt = d.format;
				header = File.use( 
					this.class.headerFilePath, "r", 
						{|f| f.readAllString } );
				f.putString( header ++ "\n" ++ txt );
				 }, overwrite, ask) // output to file with default formatting
			};
		
		if( postFormat ) { d.format.postln; };	
		}
		
	format {  // returns svg code as string ( doesn't include header and xmlns )
		var root, gTag, wfsPathTag;
		var d,f;
		
		d = DOMDocument.new; // create empty XML document
		root = d.createElement("svg");
		if( width.notNil ) { root.setAttribute( "width", width.asString ); };
		if( height.notNil ) { root.setAttribute( "height", height.asString ); };
		d.appendChild(root);
		objects.do({ |object| object.asDOMElement( d, root ) });
		^d.format
		
		}
	
		
	read { |replaceObjects = true|
		var inXML, file;
		//var document, root, svgObjects, lastTime = 0;
		
		path = path.standardizePath;
		file = File(path, "r");
		inXML = String.readNew(file);
		file.close;
		this.parseXML( inXML, replaceObjects );
		
		}
	
	parseXML { |xml = "", replaceObjects = true|
		var document, root, svgObjects;
		
		// might take a while for large files...
		
		if( replaceObjects ) { svgObjects = []; } { svgObjects = objects; };
		document = DOMDocument.new;
		document.parseXML( xml );
		
		root = document.getDocumentElement;
		
		width = root.getAttribute( "width" );
		height = root.getAttribute( "height" );
		
		root.getChildNodes.do({ |node|
				svgObjects = svgObjects.add( SVGObject.prFromDOMElement( node ) );
				});
			
		objects = svgObjects;
		domDocument = document; 
		domRoot = root;
	
		}
		
	asRect {  
 		var xPoints, yPoints, xMin, xMax, yMin, yMax;
	 	var rectPoints;
	 	rectPoints = objects
	 		.collect({ |object| 
	 			var rect; rect = object.asRect;
	 			[ rect.leftTop, rect.rightBottom ] }).flat;
		xPoints = rectPoints.collect( _.x );
		yPoints = rectPoints.collect( _.y );
		#xMin, xMax = [ xPoints.minItem, xPoints.maxItem ];
		#yMin, yMax = [ yPoints.minItem, yPoints.maxItem ];
		^Rect.newSides( xMin, yMin, xMax, yMax );
		}
		
	bounds { 
		var rect;
		if( width.isNil or: height.isNil )
			{ rect = this.asRect };
		^Rect(0,0, width.interpretVal ?? { rect.width },  height.interpretVal ?? { rect.height });
		}
		
	at { |index| ^objects.at( index ); }
	copySeries { |first, second, last| ^objects.copySeries( first, second, last ); }
	
	put { |index, item| objects.put( index, item ); }
	
	add { |object| objects = objects.add( object ); }
	
	printOn { |stream| 
		var objectsSize, numGroups;
		
		numGroups = objects.count({ |object| object.elementName.asSymbol == \g; });
		objectsSize = objects.size - numGroups;
		stream << "SVGFile( " << path.basename.quote << ", " << objectsSize << " object(s)" <<
			( if( numGroups > 0 ) { " / " ++ numGroups + "group(s)"  } { "" } ) << " )";
		}
		
	postTree { |tabLevel = 0|
		var preTabs;
		preTabs = String.fill( tabLevel, { $\t } );
		(this.class.asString ++ " : ").postln;
		objects.do({ |object, i|
			(preTabs ++ "\t" ++ (i) + " : ").post;
			object.postTree( tabLevel + 1 );
			});
		}
		
	asPenFunction {  // returns a function
		var penFuncs;
		penFuncs = objects.collect({  |object| object.asPenFunction });
		^{ penFuncs.do( _.value ); };
		}
		
	plot { |canUpdate = false|
		var penFunc;
		if( canUpdate )
			{ ^GUI.window.new( path.basename, 
				this.bounds.moveTo( 128,64 )
					).drawHook_( { this.draw; } ).front; }
			{ penFunc = this.asPenFunction;
			^GUI.window.new( path.basename, 
				this.bounds.moveTo( 128,64 )
					).drawHook_( { penFunc.value } ).front; 
			}
		}
		
	draw { objects.do({ |object| object.draw }); }
	
	allObjects { ^objects.collect( _.allObjects ).flat; }
	
	hasCurves {	
		^this.allObjects
			.select({ |item| item.class == SVGPath })
			.any({ |svgPath| svgPath.segments
				.any({ |segment| 
					[ \curveTo, \sCurveTo ].includes( segment.type.firstToLower );
					})
				});
		}
	
	}
	
SVGObject {

	var <>name, <transform;
	
	elementName { ^"desc" }
	
	allObjects { ^this }
	
	asAttributesArray { ^[] }
	
	addDOMAttributes { |element|
   		this.asAttributesArray.do({ |item| 
   			if( item[1].notNil ) 
   			{ element.setAttribute( item[0], item[1].asString ); }
   		});
   		^element; 
   		}
   		
   	asRect { ^Rect(0,0,0,0) }
   	
   	interpretTransform {
   		case { transform.isNil or:  { transform.class == SVGTransform } }
   			{ }
   			{ transform == "" }
   			{ transform = nil }
   			{ true }
   			{ transform = SVGTransform( transform ) };

   		}
   	transform_ { |newTransform|	
   		transform = newTransform; this.interpretTransform;
   		/*
   		case { newTransform.isNil or: { newTransform == "" } }
   			{ transform = nil }
   			{ newTransform.class == SVGTransform }
   			{ transform = newTransform }
   			{ true }
   			{ transform = SVGTransform( newTransform ) };
   		*/
   		}
		
	addDOMElements { |element, domDocument, root| ^element }
	
	asDOMElement {  |domDocument, root| // always input DOMDocument and root
		var element;
		if( domDocument.notNil && root.notNil )
			{ 	element = domDocument.createElement( this.elementName );
				if( name.notNil ) { element.setAttribute( "id", name.asString ); };
				if( transform.notNil ) { 
					element.setAttribute( "transform", transform.asSVGTransform.string ); };
				this.addDOMAttributes( element );
				root.appendChild( element );
				this.addDOMElements( element, domDocument, root );
				}
			{ (this.class.asString ++ 
				"-asDOMElement: failed; no DOMDocument and/or root supplied").postln; };
		^element;
		}
		
	*classFromTagName { |tagName = 'desc'|
		^( 	desc: SVGObject, // descriptive object - contents will be lost
			g: SVGGroup, 
			polyline: SVGPolyLine, 
			polygon: SVGPolygon,
			rect: SVGRect,
			ellipse: SVGEllipse,
			circle: SVGCircle,
			line: SVGLine,
			text: SVGText,
			tspan: SVGTSpan,
			path: SVGPath
			)[ tagName.asSymbol ];
		}
		
	*prFromDOMElement { |element| // select a class for current element
		var theClass;
		
		theClass = this.classFromTagName( element.getTagName );
			
		if( theClass.notNil )
			{ ^theClass.fromDOMElement( element ) }
			{ ^SVGUnknown( element ); };
		}
		
		
	asPenFunction { ^nil }
	
	draw { ^this.asPenFunction.value }
	
	plot { ^GUI.window.new( name ? this.class ).drawHook_( this.asPenFunction ).front; }
	
	postTree { this.class.postln; }
	
	}
	
SVGUnknown : SVGObject {
	var <>element;
	
	// some elements are not supported ( "style", "defs" etc )
	// this class makes them not disappear, and even be written back to the file 
	
	*new { |element| ^super.new.element_( element ) }
	
	elementName { ^element.getNodeName }
	
	postTree { ("" ++ this.class ++ "( " ++ this.elementName ++ " )" ).postln; }
	
	asDOMElement {  |domDocument, root|
		root.appendChild( element );
		^element;
		}
	
	}
	
SVGGroup : SVGObject {
	
	var <>objects; 
	
	elementName { ^"g" }
	
	*new { | objects, name, transform |
		objects = objects ? [];
		^super.newCopyArgs( name, transform, objects ).interpretTransform;
		}
		
	allObjects { ^objects.collect( _.allObjects ).flat; }
		
	add { | object | objects = objects.add( object ); } // in place!!
		
	addAll { |objectArray| objects = objects.addAll( objectArray ); }
		
	++ { |svgGroup| ^SVGGroup( objects.addAll( svgGroup.objects ), name, transform); }
	
	at { |index| ^objects.at( index ); }
	copySeries { |first, second, last| ^objects.copySeries( first, second, last ); }
	
	put { |index, item| objects.put( index, item ); }
	
	asRect { 
 		var xPoints, yPoints, xMin, xMax, yMin, yMax;
	 	var rectPoints;
	 	rectPoints = objects
	 		.collect({ |object| 
	 			var rect; rect = object.asRect;
	 			[ rect.leftTop, rect.rightBottom ] }).flat;
		xPoints = rectPoints.collect( _.x );
		yPoints = rectPoints.collect( _.y );
		#xMin, xMax = [ xPoints.minItem, xPoints.maxItem ];
		#yMin, yMax = [ yPoints.minItem, yPoints.maxItem ];
		^Rect.newSides( xMin, yMin, xMax, yMax );
	}
	
	addDOMElements { |element, domDocument, root|
		objects.do({ |object| object.asDOMElement( domDocument, element ); });
		^element
		}
		
	*fromDOMElement {  arg tag;
			var newObjects = [], transformAttribute;
			
			tag.getChildNodes.do({ |node|
				newObjects = newObjects.add( SVGObject.prFromDOMElement( node ) );
					});

			^this.new(newObjects, 
					tag.getAttribute("id"), 
					SVGTransform.fromDOMElement( tag )
					);
			 }
			 
	asPenFunction {  // returns a function
		var penFuncs, transformFunc;
		penFuncs = objects.collect({  |object| object.asPenFunction });
		if(transform.notNil)
			{ transformFunc = transform.asPenFunction; };
		^{ GUI.pen.use({ 
				transformFunc.value;
				penFuncs.do( _.value );
				});
			};
		}
		
	opacity { ^1 }
	opacity_ { (this.class ++ ":opacity_ - opacity for SVGGroups not supported").postln; }
		
	printOn { |stream| 
		var objectsSize, numGroups;
		
		numGroups = objects.count({ |object| object.elementName.asSymbol == \g; });
		objectsSize = objects.size - numGroups;
		stream << "SVGGroup( " << (name ? "").asString.quote << ", " 
				<< objectsSize << " object(s)" <<
			( if( numGroups > 0 ) { " / " ++ numGroups + "group(s)"  } { "" } ) << " )";
		}
		
	postTree { |tabLevel = 0|
		var preTabs;
		preTabs = String.fill( tabLevel, { $\t } );
		(this.class.asString ++ " : ").postln;
		objects.do({ |object, i|
			(preTabs ++ "\t" ++ (i) ++ " : ").post;
			object.postTree( tabLevel + 1 );
			});
		}
	
	
	}

AvailableFonts {
	classvar <fonts;
	
	*initClass {
		StartUp.add( {
			this.getFonts;
		});
		// does this work in SwingOSC based sytems?
		// nescivi: the command is asynchronous in SwingOSC, and server may not be started yet.
		// Putting the command in the startup sequence ensures that GUI has been loaded
		// On SwingOSC though, the command may have to be called twice, 
		// hence the move into a separate function
		}

	*getFonts{
		fonts = if(GUI.current.isNil){
			[] // e.g. CLI-only usage
		}{
			GUI.font.availableFonts.sort.collect( _.asSymbol )
		};
	}
	
	*includesName { |fontName| ^fonts.includes( fontName.asSymbol ); }
	*includes { |font| 
		var fontName;
		if( font.class == Font )
			{ fontName = font.name }
			{ fontName = font };
		^fonts.includes( fontName.asSymbol );
		}
		
	}	
	
SVGText : SVGObject {
	
	var <>string, <>x, <>y, <>fontName, <>fontSize, <>fillColor, <>dx, <>dy ;
	var <>anchor = 'start', <fontFound = true; 
	
	// please note: string is not a String but an array of Strings and SVGTSpans
	
	elementName { ^"text" }
	
	*new { | string, x, y, fontName, fontSize, fillColor, dx, dy, name, transform |
		string = string ? "";
		if( string.class == String ) { string = [string]; };
		fillColor = fillColor ? Color.black;
		if( fontName.asSymbol == \nil ) { fontName = nil };
		^super.newCopyArgs( name, transform, 
			string, x, y, fontName, fontSize, fillColor, dx, dy )
			.interpretTransform
			.checkFont;
		}
		
	
	asRect { ^Rect( (x ? 0).asCollection.first,(y ? 0).asCollection.first,0,0 ) } //incorrect
	
	checkFont {
		if( fontName.notNil )
			{ 	fontFound = AvailableFonts.includesName( fontName );
				if( fontFound.not ) 
					{ ("SVGText-new: Font" + fontName.quote + "not available").postln; };
			};
		}
		
	getText { ^string.collect({ |item| if( item.class == String )
				{ item } { item.getText }; }).join( " " ); 
					// spaces are added between tspan objects :: might not always be right..
			}
			
	fullString { ^this.getText }
	fullString_ { |newString| string = [ newString ]; }
	
	font { if( fontName.isNil )
			{ ^nil }
			{ ^GUI.font.new( fontName, fontSize ? 12 ); }
		}
		
	font_ { |aFont| fontName = aFont.name; fontSize = aFont.size;  }
		
	++ { |svgText| 
		if( svgText.class == SVGText ) 
			{^this.copy.string_( string ++  svgText.string ) }
			{^this.copy.string_( string ++ [ svgText.asString ] ) };
		}
	
	at { |index| ^string.at( index ); }
	copySeries { |first, second, last| ^string.copySeries( first, second, last ); }
	
	asAttributesArray {
		^( if( [x,y,dx,dy].every({ |item| (item == 0) or: (item.isNil ) }).not )
			 {[ 	[ "x",	x !? { x.asCollection.join( " " ) } ], 
   				[ "y",	y !? { y.asCollection.join( " " ) } ],
   				[ "dx",	dx !? { dx.asCollection.join( " " ) } ], 
   				[ "dy",	dy !? { dy.asCollection.join( " " ) } ] ]} ) ++
   				
   		[ 	[ "font-family", fontName ],
   			[ "font-size", fontSize ],
   			[ "text-anchor", anchor],
   			[ "fill", fillColor.asWebColorString( "none" ) ] ] ++
	   		(if( fillColor.asColor.alpha != 1 ) { [[ "opacity", fillColor.asColor.alpha ]] })
	   }
	
	addDOMAttributes { |element| // also add children
   		this.asAttributesArray.do({ |item|
   			if( item[1].notNil ) 
   				{ element.setAttribute( item[0], item[1].asString ); }
   			});
   		
   		string.do({ |subString|
   			var textElement;
   			if( subString.class == String )
   				{ textElement = DOMText( element.getOwnerDocument, subString ); }
		   		{ if( subString.class != DOMElement ) 
		   			{ textElement = 
		   				subString.asDOMElement( element.getOwnerDocument, element ); }
		   			{ textElement = subString; };
		   		};
		   		
		   	element.appendChild( textElement ); 
		   	
   			});
   		
   		^element; 
   		}
	
	*fromDOMElement {  arg tag;
			var anchor, children, string = [], font_family;
			var xx, yy, dx, dy;
			anchor = tag.getAttribute("text-anchor");
			
			if( anchor.isNil )
				{ //"SVGText: no text-anchor element found; set to 'start'".postln;
				 
				anchor = \start;
				} {
				anchor = anchor.asSymbol;
				if( anchor != \start )
					{  "SVGText: text-anchor != 'start'".postln;
					 "\tString-draw might not display correct position".postln;
					 };
				};
				
			children = tag.getChildNodes;
			if( children.notNil )
				{ children.do({ |node|
            			if ( node.getNodeType == DOMNode.node_TEXT ) 
	            			{ string = string.add( node.getText ); }
	            			{ string = string.add( SVGObject.prFromDOMElement( node ) ) };
	            		})
	            	}; 
	            	
	         font_family = tag.getAttribute("font-family");
	         if( font_family.notNil )
	         		{  font_family.asString;
	         		if( ( font_family[0] == $' ) && ( font_family.last == $' ) )
	         			{ font_family = font_family[1..(font_family.size-2)] };
	         		};
			
			xx = (tag.getAttribute("x") ? "0").asNumberIfPossible;
			yy = (tag.getAttribute("y") ? "0").asNumberIfPossible;
			if( xx.class == String ) { xx = xx.extractNumbers };
			if( yy.class == String ) { yy = yy.extractNumbers };
			
			dx = (tag.getAttribute("dx") ? "0").asNumberIfPossible;
			dy = (tag.getAttribute("dy") ? "0").asNumberIfPossible;
			if( dx.class == String ) { dx = dx.extractNumbers };
			if( dy.class == String ) { dy = dy.extractNumbers };
			
			if( dx == 0 ) { dx = nil };
			if( dy == 0 ) { dy = nil };
			
			//  string, x, y, fontName, fontSize, fillColor, dx, dy, name, transform
			
			^this.new(string, 
					xx,
					yy,
					font_family,
					tag.getAttribute("font-size").interpret,
					tag.getAttribute("fill"),
					dx, 
					dy,
					tag.getAttribute("id"),  
					SVGTransform.fromDOMElement( tag )
					).anchor_( anchor )
					.opacity_( ( tag.getAttribute("opacity") ? "1" ).interpret );
			 }
			 
	opacity { 
   		// can only be the same for stroke and fill
   		// opacity for SVGGroups not supported (yet)
   		var colortocheck;
   		colortocheck = fillColor ? 'none';
   		if( colortocheck.asSymbol == 'none' )
   			{ ^1 }
   			{^colortocheck.asColor.alpha; };
   		}
   		
   	opacity_ { |newOpacity = 1|
   		if( newOpacity == 0 )
   			{ this.class.name ++ 
   			":opacity should not be 0;\n\tplease set colors to nil or 'none' instead ".postln; }
   			{
   			if( this.shouldFill )
	   			{ this.fillColor = this.fillColor.asColor.alpha_( newOpacity ); };
	   		};
   		}
   		
   	shouldFill { ^this.fillColor.notNil && 
   			{ this.fillColor.asSymbol != 'none' } &&
   			{ this.fillColor.asColor.alpha != 0 }
   		}
   		
   	shouldStroke { ^false }
   	
   	buildPenFunction { |text, inX = 0, inY = 0, inFont, inFontSize, xOffset = 0|
   		// to do : tspan support; nested text
   		  // returns a function
		var strDict, xCol, yCol, xWidths, maxSize, drawFont, xx = 0;
		drawFont =  ( if( fontFound ) { this.font } { nil } ) ?
			inFont ?? {
			GUI.font.new( "Helvetica", fontSize ? inFontSize ? 12 );
				};
				
		text = text ?? { this.getText; };
		xCol = ((x ? inX ? 0) + xOffset + (dx ? 0)).asCollection;
		yCol = ((y ? inY ? 0) + (dy ? 0)).asCollection;
		maxSize = xCol.size.max( yCol.size ).min( text.size );
		if( thisProcess.platform.name == \osx )
			{ xWidths = [0] ++ Array.fill(maxSize - 1, { |i| 
				text[i].asString.bounds(  Font( drawFont.name, drawFont.size ) ).width });
			 xCol = Array.fill( maxSize, { |i|
			 	var xxx, dxx;
			 	xxx = ((x ? inX ? 0) + xOffset).asCollection[i];
			 	dxx = (dx ? 0).asCollection[i];
			 	if( xxx.isNil )
			 		{ xx = xx + xWidths[i]; }
			 		{ xx = xxx + (dxx ? 0) };
			 	});
			};
			
		strDict = Array.fill( maxSize, { |i|
			var outStr;
			if( i == (maxSize - 1) )
				{ outStr =  text[i..].asString }
				{ outStr =  text[i].asString };
			[ outStr, xCol.clipAt(i)@(yCol.clipAt(i)) ]
			});

		^{ 	if( GUI.pen.respondsTo( \stringAtPoint ) )
					{ GUI.pen.color = fillColor.asColor( this.opacity );
					  GUI.pen.font = drawFont;
					  strDict.do({ |item|
					  	GUI.pen.stringAtPoint( item[0], item[1].x@(item[1].y - drawFont.size) );
					  	});
					  }
					{
					strDict.do({ |item|
					item[0].drawAtPoint( 
						item[1].x@(item[1].y - drawFont.size), 
						drawFont,
						fillColor.asColor( this.opacity ) );
						});
					}
			};
		
   		
   		}
   		
   	asPenFunction { |inX = 0, inY = 0, inFont, inFontSize|
   		var outFuncs, widthMap, objectsToDraw;
   		widthMap = [0] ++ string.collect({ |item|
   			case { item.class == String }
   				{ if( thisProcess.platform.name == \osx ) // how to get stringbounds in non osx?
   					{ item.bounds( this.font ? Font( "Helvetica", fontSize ? 12 ) ).width }
   					{ 0 }; }
   				{ item.class == SVGTSpan }
   				{ if( thisProcess.platform.name == \osx ) // how to get stringbounds in non osx?
   					{ item.getText.bounds(  this.font ? Font( "Helvetica", fontSize ? 12 ) ).width }
   					{ 0 }; }
   				{ true } { 0 };
   				});
  		widthMap.postln;
   		outFuncs = string.collect({ |item,i|
   			case { item.class == String }
   				{ this.buildPenFunction( item, inX, inY, inFont, inFontSize, widthMap[..i].sum.postln ) }
   				{ item.class == SVGTSpan }
   				{ item.asPenFunction( widthMap[..i].sum + inX + x, y + inY, this.font, fontSize ) }
   				{ true } { nil };
   			});
   			
   		^{ GUI.pen.use({ 
				transform.asPenFunction.value;
				outFuncs.do( _.value )
			})
		 }
   		}

	/*
	asPenFunction {  // returns a function
		var strDict, text, xCol, yCol, xWidths, maxSize, drawFont, xx = 0;
		drawFont =  ( if( fontFound ) { this.font } { nil } ) ?
			GUI.font.new( "Helvetica", fontSize ? 12 );
		text = this.getText;
		xCol = ((x ? 0) + (dx ? 0)).asCollection;
		yCol = ((y ? 0) + (dy ? 0)).asCollection;
		maxSize = xCol.size.max( yCol.size ).min( text.size );
		if( thisProcess.platform.name == \osx )
			{ xWidths = [0] ++ Array.fill(maxSize - 1, { |i| 
				text[i].asString.bounds(  Font( drawFont.name, drawFont.size ) ).width });
			 xCol = Array.fill( maxSize, { |i|
			 	var xxx, dxx;
			 	xxx = (x ? 0).asCollection[i];
			 	dxx = (dx ? 0).asCollection[i];
			 	if( xxx.isNil )
			 		{ xx = xx + xWidths[i]; }
			 		{ xx = xxx + (dxx ? 0) };
			 	});
			};
			
		strDict = Array.fill( maxSize, { |i|
			var outStr;
			if( i == (maxSize - 1) )
				{ outStr =  text[i..].asString }
				{ outStr =  text[i].asString };
			[ outStr, xCol.clipAt(i)@(yCol.clipAt(i)) ]
			});

		^{ GUI.pen.use({ 
				transform.asPenFunction.value;
				if( GUI.pen.respondsTo( \stringAtPoint ) )
					{ GUI.pen.color = fillColor.asColor( this.opacity );
					  GUI.pen.font = drawFont;
					  strDict.do({ |item|
					  	GUI.pen.stringAtPoint( item[0], item[1].x@(item[1].y - drawFont.size) );
					  	});
					  }
					{
					strDict.do({ |item|
					item[0].drawAtPoint( 
						item[1].x@(item[1].y - drawFont.size), 
						drawFont,
						fillColor.asColor( this.opacity ) );
						});
					}
				});
			};
		}
		*/
		
	printOn { |stream| 
		if( fontName.isNil )
			{ stream << this.class.asString << "( " << string.asString << " )"; }
			{ stream << this.class.asString << "( " << string.asString << ", " <<
				fontName.asString.pad( $' ) <<" )";  }
		}
		
	postTree { |tabLevel = 0|
		var preTabs;
		preTabs = String.fill( tabLevel, { $\t } );
		(this.class.asString ++ " : ").postln;
		string.do({ |object, i|
			(preTabs ++ "\t" ++ (i) ++ " : ").post;
			if( object.isString )
				{ object.quote.postln }
				{ object.postTree( tabLevel + 1 ) }
				});
		}	
	
	}

SVGTSpan : SVGText { 
	
	// only appears within SVGText objects
	// not displayed correctly with plot
	
	elementName { ^"tspan" }
	
	}
	
SVGGraphicObject : SVGObject {
	// contains things which apply for all graphic objects, not text
	
	initColors {
		if( this.strokeColor.isNil and: this.fillColor.isNil )
			{ this.fillColor = Color.black };
		this.strokeWidth = this.strokeWidth ?? {1};
		}
	
	*newCopyArgs { |...args|
		^super.newCopyArgs( *args ).initColors.interpretTransform;
		}
	
	asAttributesArray {
		var opacity;
		opacity = this.allOpacity; 
		^[ 	[ "stroke-width",	this.strokeWidth ],
   			[ "stroke", 		this.strokeColor.asWebColorString( "none" )],
   			[ "fill", 		this.fillColor.asWebColorString( "none" )] ] ++
   		(if ( opacity.mean != 1 ) 
   			{ if( opacity[0] != opacity[1] )
   				{ [[ "fill-opacity", opacity[0] ],["stroke-opacity", opacity[1]]]
   					.select({ |item| item[1] != 1 });
   					 }
   				{ [[ "opacity", opacity[0] ]] }
   			}) 
   		}
   	
   	opacity { 
   		// returns 1 if stroke and fill opacity are different
   		var fillOp, strokeOp;
   		fillOp = this.fillOpacity;
   		strokeOp = this.strokeOpacity;
   		if( fillOp != strokeOp )
   			{ ^1 } // use fillOpacity and strokeOpacity separately to get opacity
   			{ ^fillOp }; 
   		}
   		
   	allOpacity { ^[ this.fillOpacity, this.strokeOpacity ] }
   		
   	fillOpacity {
   		if( this.fillColor.notNil && { this.fillColor.asSymbol != 'none' } )
   			{ ^this.fillColor.asColor.alpha }
   			{ ^1 };
   		}
   		
   	strokeOpacity {
   		if( this.strokeColor.notNil && { this.strokeColor.asSymbol != 'none' } )
   			{ ^this.strokeColor.asColor.alpha }
   			{ ^1 };
   		}
   	
   	fillOpacity_ { |newOpacity = 1|
   		if( this.shouldFill )
   			{ this.fillColor = this.fillColor.asColor.alpha_( newOpacity ) }
   		}
   		
   	strokeOpacity_ { |newOpacity = 1|
   		if( this.shouldStroke )
   			{ this.strokeColor = this.strokeColor.asColor.alpha_( newOpacity ) }
   		}
   		
   	opacity_ { |newOpacity = 1|
   		if( newOpacity == 0 )
   			{ this.class.name ++ 
   			":opacity should not be 0;\n\tplease set colors to nil or 'none' instead ".postln; }
   			{ this.fillOpacity = newOpacity; this.strokeOpacity = newOpacity;
	   		};
   		}
   	
   	opacityFromDOMElement { |tag|
  		var mainOp, fillOp, strokeOp;
  		mainOp = ( tag.getAttribute("opacity") ? "1").interpret;
  		fillOp = ( tag.getAttribute("fill-opacity") ? "1").interpret;
  		strokeOp = ( tag.getAttribute("stroke-opacity") ? "1").interpret;
  		case { fillOp == strokeOp }
  			{ if( ( mainOp * fillOp ) != 1 ) { this.opacity = mainOp * fillOp } }
  			{ if( ( mainOp * fillOp ) != 1 ) { this.fillOpacity = mainOp * fillOp };
  			  if( ( mainOp * strokeOp ) != 1 ) { this.strokeOpacity = mainOp * strokeOp };
  			};
   		}
   	
   	
   		
   	shouldFill { ^this.fillColor.notNil && 
   			{ this.fillColor.asSymbol != 'none' } &&
   			{ this.fillColor.asColor.alpha != 0 }
   		}
   		
   	shouldStroke { ^this.strokeColor.notNil && 
   			{ this.strokeColor.asSymbol != 'none' } &&
   			{ this.strokeColor.asColor.alpha != 0 }
   		}
   	
   	asPenLinesFunc { ^nil } // called by asPenFunction
   	
   	fillFunc { ^{ GUI.pen.fill } } // different for some
   	strokeFunc { ^{ GUI.pen.stroke } }
   	clipFunc { ^{ GUI.pen.clip } } // not used yet
   		
   	asPenFunction {
   		var penfuncs, linesfunc, transformfunc;
   		var fc, sc;
   		linesfunc = this.asPenLinesFunc;
   		 
   		penfuncs = [];
   		
   		if( this.shouldFill ) {
   			fc = this.fillColor.asColor;
   			penfuncs = penfuncs.add(
   				{  if( GUI.pen.respondsTo( \color_ ) )
   				  	{ GUI.pen.color_( fc )  }
   				  	{ fc.set; };
   				  linesfunc.value; 
   				  this.fillFunc.value; }) 
   			};
   				  
		if( this.shouldStroke ) {
			sc = this.strokeColor.asColor;
			penfuncs = penfuncs.add(
   				{if( GUI.pen.respondsTo( \color_ ); )
   				  	{ GUI.pen.color_( sc )  }
   				  	{ fc.set; }; 
				  GUI.pen.width = this.strokeWidth;
				  linesfunc.value; 
				  this.strokeFunc.value; }) 
			};
			
		

		if( transform.notNil )
			{ 
			transformfunc = transform.asPenFunction;
			^{ GUI.pen.use({ 
				transformfunc.value;
				penfuncs.do(_.value);
				}); };
			} 
			{ ^{ penfuncs.do(_.value); };
			}
   		}
   	
	}  

	
SVGPolyLine : SVGGraphicObject {
	
	var <>points, <>strokeColor, <>fillColor, <>strokeWidth;
	
	// strokeColor and fillColor can be Color, string with color name, "none" or nil
	
	elementName { ^"polyline" }
	
	*new { |points, name, strokeColor, fillColor, strokeWidth, transform|
		var tempPoints = [];
		  points = points ? [];
		  if( points[0].notNil && { points[0].class != Point } )
		  	{ if( points[0].size == 0 )
		  		{ points.pairsDo({ |x,y| tempPoints = tempPoints.add( Point(x,y) ) });
		  			points = tempPoints;  }
		  		{ points = points.collect( _.asPoint ); };
		  	};
		  //fillColor = fillColor ? Color.black;
		 // fillColor = fillColor.asColor;
		 // strokeWidth = strokeWidth ? 1;
		  ^super.newCopyArgs( name, transform, points, strokeColor, fillColor, strokeWidth );
		  }
	
	add { |point|
		 if( point.notNil && { point.class != Point } )
		  	{  point = point.asPoint };
		  points = points.add( point );
		}
		
	addAll { |pointArray|
		var tempPoints;
		if( pointArray[0].notNil && { pointArray[0].class != Point } )
		  	{ if( pointArray[0].size == 0 )
		  		{ pointArray.pairsDo({ |x,y| tempPoints = tempPoints.add( Point(x,y) ) });
		  			pointArray = tempPoints;  }
		  		{ pointArray = pointArray.collect( _.asPoint ); };
		  	};
		 points = points.addAll( pointArray );
		 }
	
	++ { |polyLine| points = points.addAll( polyLine.points ); }
	
	at { |index| ^points.at(index) }
	copySeries { |first, second, last| ^points.copySeries( first, second, last ); }
	
	put { |index, item| points.put( index, item ) }
	
	asXYArray { ^points.collect({ |point| [point.x, point.y] }) }
	
	asRect { var xPoints, yPoints, xMin, xMax, yMin, yMax;
		xPoints = points.collect( _.x );
		yPoints = points.collect( _.y );
		#xMin, xMax = [ xPoints.minItem, xPoints.maxItem ];
		#yMin, yMax = [ yPoints.minItem, yPoints.maxItem ];
		^Rect.newSides( xMin, yMin, xMax, yMax );
		}
		
	asSVGPath { |relative = true|
		var segs, currentPoint;
		if( relative )
			{ currentPoint = points[0];
			segs = [[\M, points[0].x, points[0].y ]] ++
				points[1..].collect({ |point|
					var seg;
					seg = [\l, point.x - currentPoint.x, point.y - currentPoint.y ];
					currentPoint = point;
					seg;
					}); 
				}
			{ 
			segs = [[\M, points[0].x, points[0].y ]] ++
				points[1..].collect({ |point| [\L, point.x, point.y]; }); 
				};
		^SVGPath( segs, name, strokeColor, fillColor, strokeWidth, transform )
		}
	
	asAttributesArray {
		var polylinePoints = "", rawValues;
		 
		rawValues = this.asXYArray;
    		rawValues.do({ arg line;
			polylinePoints = polylinePoints ++ line[0] ++ 
				", " ++ line[1] ++ " "; });  
				 		
   		^[ [ "points",		polylinePoints ] ] ++ super.asAttributesArray;
   		}
	
	*fromDOMElement {  arg tag;
			var points, newObject, transformAttribute;
			points = tag.getAttribute("points");
				
			points = points    // ( more then one space after comma goes wrong )
				.tr($\n, $ ).tr($\t, $ )  // replace returns and tabs with spaces
				.replace( ", ", "@" )  // replace comma's with @
				.replace( ",", "@" )   // replace 
				.split( $ )
				.collect( _.interpret )
				.select({ |item| item.notNil });
				
			^this.new(points, 
					tag.getAttribute("id"), 
					tag.getAttribute("stroke"),
					tag.getAttribute("fill"),
					tag.getAttribute("stroke-width").interpret,
					SVGTransform.fromDOMElement( tag )
					).opacityFromDOMElement( tag );
			 }
			 
	asPenLinesFunc {
		^{ GUI.pen.moveTo( points.first );
			points[1..].do({ |point| GUI.pen.lineTo( point ); }); }
		}
				
	}
	
SVGPolygon : SVGPolyLine {

	elementName { ^"polygon" }
	
	asSVGPath { |relative = true|
		^super.asSVGPath.add( [\z] );
		}
		
	asPenLinesFunc { ^{
			GUI.pen.moveTo( points.first );
			points[1..].do({ |point| GUI.pen.lineTo( point ); });
			GUI.pen.lineTo( points.first ); // the only difference to polyline
			};
		}
		
	}
	
SVGPathSegment {
	classvar <typeDict, <propertiesDict, <>curveRes = 16;
	var <type = \closePath, <args, <event;
	
	*initClass {
		typeDict = (
			a: \arc, 
			c: \curveTo, 
			h: \hLineTo, 
			l: \lineTo, 
			m: \moveTo, 
			q: \qCurveTo, 
			s: \sCurveTo, 
			t: \sQCurveTo, 
			v: \vLineTo, 
			z: \closePath,
			unknown: \unknown );
			
		propertiesDict = (
			arc: [\rx, \ry, \xAxisRotation, \largeArcFlag, \sweepFlag, \x, \y ], 
			closePath: [], 
			curveTo: [\x1, \y1, \x2, \y2, \x, \y ], 
			hLineTo: [\x ], 
			lineTo: [\x, \y ], 
			moveTo: [\x, \y ], 
			qCurveTo: [\x1, \y1, \x, \y ], 
			sCurveTo: [\x2, \y2, \x, \y ], 
			sQCurveTo: [\x, \y ], 
			vLineTo: [\y ], 
			vLineTo: [\y ],
			unknown: [] );
		}
	
	*new { |type ... args|
		if( type.class != Symbol )
			{ ^super.newCopyArgs( \LineTo, [type] ++ args).init; }
			{ ^super.newCopyArgs( type, args).init; };
		}
	
	init { 
		type = this.getType;
		args = args.collect( _.asArray ).flat; // collect points and arrays
		
		this.asEvent;
		
		if( this.propertyArray.any( _.isNil ) )
			{ ("SVGPathSegment-init: too few properties supplied for" 
				+ type.asString.pad( $' ) + "segment").postln; };
		
		}
	
	*getType { |type|
		var isAbs, outType;
		isAbs = type.asString.firstIsUpper;
		if( type.asString.size == 1 )
			{ outType = typeDict[ type.asSymbol.firstToLower ] ? \unknown;
				if( isAbs ) { outType = outType.firstToUpper }; 
			^outType }
			{ if( typeDict.values.any({ |item| item == type.asSymbol.firstToLower }) ) 
				{ ^type  }
				{ if( isAbs ) { ^\Unknown;  } { ^\unknown; } }
				  };
		}
		
	getType { ^this.class.getType( type ); }
	
	*getPropertiesFor { |type| ^this.propertiesDict[ this.getType( type ).firstToLower ] }
		
	properties { 
		^propertiesDict[ type.firstToLower ];
		}
	
	asEvent { 
		event = ();
		event[ \type ] = type;
		event[ \typeChar ] = this.typeChar;
		event[ \isAbsolute ] = this.isAbsolute;
		
		this.properties.do({ |pr, i| event[ pr ] = args[i]; });
		^event;
		}
	
	type_ { |newType|
		newType = newType ? type;
		type = newType;
		this.init;
		}
		
	args_ { |newArgs|
		newArgs = newArgs ? args;
		args = newArgs;
		this.init;
		}
	
	x { ^event[ \x ] }
	y { ^event[ \y ] }
	
	x_ { |newX| args[ this.properties.indexOf( \x ) ] = newX ? this.x; this.init }
	y_ { |newY| args[ this.properties.indexOf( \y ) ] = newY ? this.y; this.init }
	
	xy { ^[this.x, this.y] }
	xy_ { |xy| xy = xy.asArray; this.x = xy[0]; this.y = xy[1] ? xy[0]; }
	
	xyProperties { ^this.properties.select({ |pr|
		[$x, $y].includes( pr.asString.first ) }); }
	
	getProperty { | pr = \x | 
		if( pr.size == 0 )
			{ ^event[ pr ]; }
			{ ^pr.collect( this.getProperty( _ ) ) };
		}
			
	setProperty { | pr = \x, value, acceptNil = false |
		if( pr.size == 0 )
			{ 
			if( this.properties.includes( pr ) )
				{ event[ pr ] = value ?? { 
					if( acceptNil ) 
						{ nil }
						{ event[ pr ] };
					}; 
				};
			}
			{ 	value = value.asCollection;
				pr.collect({ |spr, i| 
					this.setProperty( spr, value.asCollection[i], acceptNil); }); 
			};
		}
		
	doesNotUnderstand { arg selector ... args;
		var func;
		func = this.getProperty(selector);
		if (func.notNil) {
			^func.functionPerformList(\value, this, args);
			};
		if (selector.isSetter) {
				selector = selector.asGetter;
				^this.setProperty( selector, args[0] );
			};
		//("SVGPathElement: property" + selector + "not found in this element").postln;
		^nil;
		//^this.superPerformList(\doesNotUnderstand, selector, args);
	}

		
	point { var x, y;
		x = this.x; y = this.y; 
		if( [x,y].every( _.isNil ) )
			{ ^nil }
			{ ^Point(x ? 0, y ? 0) };
		}
		
	c2 { var x, y;
		x = event[ \x2 ]; y = event[ \y2 ]; 
		if( [x,y].every( _.isNil ) )
			{ ^nil }
			{ ^Point(x ? 0, y ? 0) };
		}
	
	point_ { |point|
		point = point.asPoint;
		this.setProperty( [\x, \y], point.asArray );
		}
	
	asPoint { ^this.point;}
		
	allPoints { var out;
		out = [];
		(this.getProperty( this.xyProperties ) ? []).pairsDo({ |x,y| 
			out = out.add( Point(x ? 0, y ? 0 ) ) });
		^out;
		}
		
	allPoints_ { |newPoints|
		var xyProperties;
		xyProperties = this.xyProperties;
		newPoints = newPoints.asCollection;
		this.xyProperties.pairsDo({ |x,y,i|
			this.setProperty( [x,y], newPoints[i/2].asArray );
			});
		}
			
	propertyArray { ^this.properties.collect( this.getProperty( _ ) );  }
	
	isRelative { ^type.asString.first.isLower; }
	isAbsolute { ^this.isRelative.not; }
	
	asSVGPathSegment { ^this }
	
	isSVGPathSegment { ^true }
	
	makeAbsolute { |prevSegment, warn = false| // prevSegment should be absolute!!
		var prevPoint;
		if( this.isRelative )
			{	prevSegment = prevSegment ?? { SVGPathSegment( \MoveTo, 0, 0 ) };
				prevSegment = prevSegment.asSVGPathSegment;
				prevPoint = prevSegment.asPoint;
				this.allPoints = this.allPoints.collect( _ + prevPoint ); 
				if(warn) { ("SVGPathSegment-makeAbsolute: changed" + type.asString.pad( $' ) 
					+ " element to absolute").postln; };
					
				type = type.firstToUpper;
			};
		}
	
	makeRelative { |prevSegment, warn = false| // prevSegment should be absolute!!
		var prevPoint;
		if( this.isAbsolute )
			{	prevSegment = prevSegment ?? { SVGPathSegment( \MoveTo, 0, 0 ) };
				prevSegment = prevSegment.asSVGPathSegment;
				prevPoint = prevSegment.asPoint;
				this.allPoints = this.allPoints.collect( _ - prevPoint ); 
				if(warn) { ("SVGPathSegment-makeRelative: changed" + type.asString.pad( $' ) 
					+ " element to absolute").postln; };
					
				type = type.firstToLower;
			};
		}
		
	copy { ^this.class.new( *this.asArray ) }
	asAbsolute { |prev| ^this.copy.makeAbsolute( prev ); }
	asRelative { |prev| ^this.copy.makeRelative( prev ); }
	
	typeChar { 
		var out;
		out = typeDict.findKeyForValue( type.firstToLower );
		if( this.isAbsolute ) { ^out.toUpper } { ^out };
		}
	
	asArray { ^[ type ] ++ this.propertyArray; }
	asTypeCharArray { ^([ \typeChar ] ++ this.properties).collect( this.getProperty( _ ) );  }
	
	printOn { |stream| 
		stream << "SVGPathSegment( " << type.asString.pad($') << " )";
		}
		
	
	asPenFunction { |prev, first, parent| // supply previous absolute object for relative segments
		var absObject, point, lType, lastPoint;
		
		// "%: %\n".postf(type, parent.currentPoint); // debug relative
				
		// Arc, QCurve not yet supported (will draw as straight lines)
		// sCurve might display wrong if prev element is relative. Illegal if prev point is
		// not a curve or scurve
		
		if( this.isRelative )
			{
			// relative
			switch( type,
			
				\closePath,	{ 
					point = parent.firstPoint ? (0@0);
					parent.currentPoint = point;
					^{ GUI.pen.lineTo( point ) }; },
					
				\moveTo,		{ 
					point = this.point + parent.currentPoint; 
					parent.currentPoint = point;
					parent.firstPoint = point;
					^{ GUI.pen.moveTo( this.point ) }; },
					
				\curveTo,	  	{ 
					point = this.allPoints.collect( _ + parent.currentPoint );
					lastPoint = parent.currentPoint;
					parent.currentC2 = point[1];
					parent.currentPoint = point[2];
					//^{ GUI.pen.addCurve( lastPoint, point[0], point[1], point[2], curveRes) };
					^{ GUI.pen.curveTo(  point[2], point[0], point[1], lastPoint, curveRes) };
					 },
					
				\sCurveTo,	{ 
					// prev.c2 not always correct! 
					point = [parent.currentC2.mirrorTo( parent.currentPoint )] 
						++ this.allPoints.collect( _ + parent.currentPoint );
					//point = point.collect( _ + parent.currentPoint );
					lastPoint = parent.currentPoint;
					parent.currentC2 = point[1];
					parent.currentPoint = point[2];
					//^{ GUI.pen.addCurve( lastPoint, point[0], point[1], point[2], curveRes) };
					^{ GUI.pen.curveTo( point[2], point[0], point[1], lastPoint, curveRes) };
					},
					
				\qCurveTo,	  	{ 
					point = this.allPoints.collect( _ + parent.currentPoint );
					lastPoint = parent.currentPoint;
					parent.currentC2 = point[0];
					parent.currentPoint = point[1];
					point = [ 
						point[0].blend( lastPoint, 1/3 ), 
						point[0].blend( point[1], 1/3 ),
						point[1] ];
					//^{ GUI.pen.addCurve( lastPoint, point[0], point[1], point[2], curveRes) }; 
					^{ GUI.pen.curveTo( point[2], point[0], point[1], lastPoint, curveRes) };
					},
					
				\sQCurveTo,	{ 
					// prev.c2 not always correct! 
					point = [parent.currentC2.mirrorTo( parent.currentPoint )] 
						++ this.allPoints.collect( _ + parent.currentPoint );
					//point = point.collect( _ + parent.currentPoint );
					lastPoint = parent.currentPoint;
					parent.currentC2 = point[0];
					parent.currentPoint = point[1];
					point = [ 
						point[0].blend( lastPoint, 1/3 ), // from Illustrator
						point[0].blend( point[1], 1/3 ),
						point[1] ];
					//^{ GUI.pen.addCurve( lastPoint, point[0], point[1], point[2], curveRes) };
					^{ GUI.pen.curveTo( point[2], point[0], point[1], lastPoint, curveRes) };
					},

				
				\hLineTo, {
					point = parent.currentPoint + (this.x@0);
					parent.currentPoint = point;
					^{ GUI.pen.lineTo( point ) }; },
					
				\vLineTo, {
					point = parent.currentPoint + (0@this.y);
					parent.currentPoint = point;
					^{ GUI.pen.lineTo( point ) }; }
				
				);
					
			point = this.point + parent.currentPoint;
			parent.currentPoint = point;
			^{ GUI.pen.lineTo( point ) };
			
			}
			{
			// absolute
			switch( type, 
			
				\ClosePath, 	{ 
					point = parent.firstPoint ? (0@0);
					parent.currentPoint = point;
					^{ GUI.pen.lineTo( point ) }; },
				
				\MoveTo,		{ 
					point = this.point; 
					parent.currentPoint = point;
					parent.firstPoint = point;
					^{ GUI.pen.moveTo( point ) }; },
				
				\CurveTo,		{ 
					point = this.allPoints;
					lastPoint = parent.currentPoint;
					parent.currentC2 = point[1];
					parent.currentPoint = point[2];
					//^{ GUI.pen.addCurve( lastPoint, point[0], point[1], point[2], curveRes)}; 
					^{ GUI.pen.curveTo( point[2], point[0], point[1], lastPoint, curveRes) };
					},
					
				\SCurveTo,	{
					// prev.c2 not always correct! 
					point = [parent.currentC2.mirrorTo( parent.currentPoint )] ++ this.allPoints;
					lastPoint = parent.currentPoint;
					parent.currentC2 = point[1];
					parent.currentPoint = point[2];
					//^{ GUI.pen.addCurve ( lastPoint, point[0], point[1], point[2], curveRes)}; 
					^{ GUI.pen.curveTo( point[2], point[0], point[1], lastPoint, curveRes) };
					},
				
				\QCurveTo,		{ 
					point = this.allPoints;
					lastPoint = parent.currentPoint;
					parent.currentC2 = point[0];
					parent.currentPoint = point[1];
					point = [ 
						point[0].blend( lastPoint, 1/3 ), 
						point[0].blend( point[1], 1/3 ),
						point[1] ];
					//^{ GUI.pen.addCurve( lastPoint, point[0], point[1], point[2], curveRes)}; 
					^{ GUI.pen.curveTo( point[2], point[0], point[1], lastPoint, curveRes) };
					},
					
				\SQCurveTo,	{
					// prev.c2 not always correct! 
					point = [parent.currentC2.mirrorTo( parent.currentPoint )] ++ this.allPoints;
					lastPoint = parent.currentPoint;
					parent.currentC2 = point[0];
					parent.currentPoint = point[1];
					point = [ 
						point[0].blend( lastPoint, 1/3 ), 
						point[0].blend( point[1], 1/3 ),
						point[1] ];			
					//^{ GUI.pen.addCurve ( lastPoint, point[0], point[1], point[2], curveRes)}; 
					^{ GUI.pen.curveTo( point[2], point[0], point[1], lastPoint, curveRes) };
					},
				
				\HLineTo, {
					point = (this.x)@(parent.currentPoint.y);
					parent.currentPoint = point;
					^{ GUI.pen.lineTo( point ) }; },
					
				\VLineTo, {
					point = (parent.currentPoint.x)@(this.y);
					parent.currentPoint = point;
					^{ GUI.pen.lineTo( point ) }; }
				
				);
				
			point = this.point;
			parent.currentPoint = point;
			^{ GUI.pen.lineTo( point )  };
			
			};
		}
	
	}
	
	
SVGPath : SVGGraphicObject { 
	
	var <>segments, <>strokeColor, <>fillColor, <>strokeWidth;
	
	var >currentPoint, >currentC2, <>firstPoint; // for asPenFunction
	
	// strokeColor and fillColor can be Color, string with color name, "none" or nil
	
	elementName { ^"path" }
	
	*new { |segments, name, strokeColor, fillColor, strokeWidth, transform, cleanSegments = true|
			
		  segments = segments ? [ [ \M, 0, 0 ], [ \z ] ];
		  if( segments.class == String )
		  	{ segments = this.convertString( segments ); }
		  	{ if( cleanSegments ) { segments = this.cleanSegments( segments ); }; };
		  segments = segments.collect( _.asSVGPathSegment );
		  //strokeColor = strokeColor ? Color.black;
		  //strokeColor = strokeColor.asColor;
		  //strokeWidth = strokeWidth ? 1;
		  ^super.newCopyArgs( name, transform, segments, strokeColor, fillColor, strokeWidth );
		  }
		  
	*fromSVGPolyLine { |aSVGPolyLine, curve = true, close = false|
		var x,y,pts; // controlpoints
		if( curve == true ) { curve = 1/3 }; // default to hermite curve
		^this.new( 
			aSVGPolyLine.points.collect({ |point, i|
				if( i==0 )
					{ SVGPathSegment(\M, point.x, point.y); }
					{ if( curve != false )
						{
						if( close )
							{ pts = aSVGPolyLine.points.wrapAt( i + [-2,-1,0,1]) }
							{ pts = aSVGPolyLine.points.clipAt( i + [-2,-1,0,1]) };
								
						#x,y = pts.collect( _.asArray )
							.flop.collect( _.splineIntControls( curve ) );
							
						SVGPathSegment(\C, x[0], y[0], x[1], y[1], point.x, point.y);
						}
						{ SVGPathSegment(\L, point.x, point.y); };  
					};
				}), 
			aSVGPolyLine.name, 
			aSVGPolyLine.strokeColor,
			aSVGPolyLine.fillColor,
			aSVGPolyLine.strokeWidth,
			aSVGPolyLine.transform );
		}
	
	asSVGPolyLine { ^SVGPolyLine( this.asPoints, name, 
		strokeColor, fillColor, strokeWidth, transform); }
		
	asPolyLine { ^PolyLine( *this.asPoints ); }
	
	asPolySpline {
		var plSegments = [], index = 0;
		var absoluteSegments;
		absoluteSegments = this.absoluteSegments;
		
		absoluteSegments.do({ |segment, i|
			var lType, lastSegment;
			lType = segment.type.firstToLower;
			lastSegment = absoluteSegments[i-1] ?? 
				{ SVGPathSegment( \M, 0, 0 ) };
			
			case { lType == \curveTo }
				{ plSegments = plSegments ++ 
					[ [ lastSegment.point, segment.allPoints[0], segment.allPoints[1] ] ] }
				{ lType == \sCurveTo }
				{ plSegments = plSegments ++ 
					[ [ lastSegment.point, 
						plSegments.last[2].mirrorTo( lastSegment.point ),
						segment.allPoints[0] ] ] }
				{ lType == \lineTo }
				{ plSegments = plSegments ++
					[ [ lastSegment.point, lastSegment.point, segment.point ] ] 
					//[ [ lastSegment.point ] ] 
					}
				{ lType == \moveTo }
				{ } // do nothing
				{ lType == \closePath } 
				{   plSegments = plSegments ++
					[ [lastSegment.point, lastSegment.point, 
						absoluteSegments.first.point  ], [ absoluteSegments.first.point ] ] 
					//[ [ lastSegment.point ], [ absoluteSegments.first.point ] ] 
					}
				{ true }
				{  plSegments = plSegments ++
					[ [ lastSegment.point, lastSegment.point, segment.point ] ] };
			});
		
		if(	absoluteSegments.last.type.firstToLower != \closePath )
			{ plSegments = plSegments ++ [ [ absoluteSegments.last.point ] ]  };
			
		^PolySpline.new.curveSegments_( plSegments );
		}
		
	add { |segment|
		  segments = segments.add( segment.asSVGPathSegment );
		}
		
	allPoints { ^segments.collect( _.allPoints ).flat; }
	
	
	asRect { var xPoints, yPoints, xMin, xMax, yMin, yMax;
		var allPoints;
		allPoints = this.absoluteSegments.collect( _.allPoints ).flat;
		xPoints = allPoints.collect( _.x );
		yPoints = allPoints.collect( _.y );
		#xMin, xMax = [ xPoints.minItem, xPoints.maxItem ];
		#yMin, yMax = [ yPoints.minItem, yPoints.maxItem ];
		^Rect.newSides( xMin, yMin, xMax, yMax );
		}
	
		
	*convertString { |string = ""|
		 var pos = 0, nodes = [], sortedNodes = [];
		while { pos < string.size }
			{ case	{ string[pos].isNil } { pos = inf } // case of empty string
					{ string[pos].isAlpha } { nodes = nodes.add( string[pos].asSymbol ); 
						pos = pos + 1 }
					{ string[pos].isSpace or:  { string[pos] == $, }  } { pos = pos + 1 }
					{ string[pos].isDecDigit or: { string[pos] == $- }  } { 
						nodes = nodes.add( string[pos].asString );
						pos = pos + 1;
						while { string[pos].notNil && 
							{ string[pos].isDecDigit or: 
								{ string[pos] == $. } } }
							 { nodes[nodes.size-1] = nodes[nodes.size-1] ++ string[pos];
							 	pos = pos + 1 };
					}
			};
			
		nodes = nodes.collect({ |node| if( node.isString ) { node.interpret } { node }; });
		
		pos = 0;
		while { pos < nodes.size }
				{ if( nodes[pos].class == Symbol )
					{ sortedNodes = sortedNodes.add( [ nodes[pos] ] ); pos = pos+1; }
					{ sortedNodes[sortedNodes.size-1] = sortedNodes[sortedNodes.size-1]
						.add( nodes[pos] ); 
						pos = pos+1;   }   };
						
		^sortedNodes.collect( _.asSVGPathSegment );
		}
	
	*convertToString { |segmentsArray|
		^segmentsArray.collect( _.asTypeCharArray).flatten(1).join( " " );
		}
	
	*cleanSegments {  |segmentsArray|
		var out, cpa, cpc = 0; // current properties amount, count
		out = [];
		segmentsArray !? { 
			segmentsArray.flat.do({ |item|
				case { item.class == Symbol }
					{ 	out = out ++ [ [ item ] ]; 
						cpa = SVGPathSegment.getPropertiesFor( item ).size; 
						cpc = 0;  } 
					{ item.class == SVGPathSegment }
					{ out = out ++ [ item ]; cpa = nil; }
					{ true } 
					{ 
					if( cpa.isNil or: { cpc >= cpa } )
						{ out = out.add( [ item ] ); 
						  cpa = SVGPathSegment.getPropertiesFor( \l ).size; 
					       cpc = item.asArray.size;	
						}
						{
						
						out = out.add( out.pop ++ item ); 
						cpc = cpc + item.asArray.size;	
						}
					};
				}); 
			};
		
		^out;
		}
				
	addAll { |segmentsArray|
		 segments = segments.addAll( segmentsArray.collect( _.asSVGPathSegment ) );
		 }
	
	++ { |path| segments = segments.addAll( path.segments ); }
	
	at { |index| ^segments.at( index ) }
	
	copySeries { |first, second, last| ^segments.copySeries( first, second, last ); }
	
	put { |index, item| segments.put( index, item.asSVGPathSegment ); }

	
	
	asAttributesArray {
		var d;
		d = SVGPath.convertToString( segments );
				 		
   		^[[ "d",		d ]] ++ super.asAttributesArray;
   		}
			
	*fromDOMElement {  arg tag;
			var segments, newObject, transformAttribute;
			
			//segments = tag.getAttribute("d");
				
			//segments = SVGPath.convertString( segments );
				
			^this.new(tag.getAttribute("d"), 
					tag.getAttribute("id"), 
					tag.getAttribute("stroke"),
					tag.getAttribute("fill"),
					tag.getAttribute("stroke-width").interpret,
					SVGTransform.fromDOMElement( tag )
					).opacityFromDOMElement( tag );
			 }
			 
	absoluteSegments {
		// incorrect in some cases
		var lastSegment;
		^segments.collect({ |segment, i|
			var out;
			out = segment.asAbsolute( lastSegment );
			lastSegment = out;
			out;
			})
		}
		
	asArray { |short = true|
		if( short )
			{ ^segments.collect( _.asTypeCharArray ); }
			{ ^segments.collect( _.asArray ); };
		}
	
	asPoints { 
		var absoluteSegments;
		absoluteSegments = this.absoluteSegments;
		^absoluteSegments.collect({ |p| if( p.type.firstToLower != \closePath )
				{ p.point }
				{ absoluteSegments[0].point }; 
			}); 
		}
		
		
	currentPoint { // return and set to 0@0 when nil
		^currentPoint ?? { currentPoint = 0@0 } 
		}
	
	currentC2 { ^currentC2 ?? { this.currentPoint }; }
	
	asPenLinesFunc { 
		 var segmentFunctions, first;
		 if( segments.collect( _.type ).collect( _.firstToLower ).includes( \arc ) )
		 	{ "Please note SVGPath:draw - arc segments will draw incorrectly as straight lines".postln; };
		currentPoint = 0@0; // reset currentpoint for relative segments
		currentC2 = nil;
		firstPoint = (0@0);
		segmentFunctions = Array.fill( segments.size,
			{ |i| segments[i].asPenFunction( segments[i-1], segments[0], this ); });
		if( segments[0].type.firstToLower != \moveTo )
			{ segmentFunctions = [ { GUI.pen.moveTo( 0@0 ) } ] ++ segmentFunctions };
				
		^{ segmentFunctions.do( _.value ); }	
		}
		
	}

	
SVGRect : SVGGraphicObject {  
	
	var <>x, <>y, <>width, <>height, <>rx, <>ry, <>strokeColor, <>fillColor, <>strokeWidth;
	
	// strokeColor and fillColor can be Color, string with color name, "none" or nil
	// rx & ry will not plot yet
	
	elementName { ^"rect" }
	
	*new { |x = 0, y = 0, width = 0, height = 0, rx = 0, ry = 0, name, 
			strokeColor, fillColor, strokeWidth, transform|
		  //strokeColor = strokeColor ? Color.black;
		  //strokeColor = strokeColor.asColor;
		  //strokeWidth = strokeWidth ? 1;
		  ^super.newCopyArgs( name, transform, 
		  	x, y, width, height, rx, ry, strokeColor, fillColor, strokeWidth );
		  }
		
	asRect { ^Rect( x ? 0, y ? 0, width ? 0, height ? 0 ); }
	
	*fromRect { |rect, name, strokeColor, fillColor, strokeWidth, transform|
		rect = rect ? Rect(0,0,100,200);
		^this.new( rect.left, rect.top, rect.width, rect.height, 0, 0, 
			name, strokeColor, fillColor, strokeWidth, transform );
		}
		
	asSVGPath { |relative = true|
		var segs, c, trx, try;
		if( ( (rx ? 0) == 0) && ((ry ? 0) == 0) )
			{ if( relative )
				{ segs = [
					[\M, x, y ], 
					[\h, width ], 
					[\v, height], 
					[\h, width.neg],
					[\z ]
					]; }
				{ segs = [
					[\M, x, y ], 
					[\L, x + width, y ], 
					[\L, x + width, y + height ], 
					[\L, x, y + height ], 
					[\L, x, y ]
					]; 
				};
			} { 
			// always relative for now
			if( true )
				{ 
				c = 0.75 / ((1.9)**0.5 ); // curve circle approx.
				trx = (rx ? 0).min( width/2);
				try = (ry ? 0).min( height/2);
				segs = [
					[\M, x + trx, y  ], 
					[\h, width - (2 * trx) ], 
					[\c, c * trx, 0, trx,  (1-c) * try, trx, try ],
					[\v, height - (2 * try)], 
					[\c, 0, c * try, (1-c) * trx.neg, try,  trx.neg, try ],
					[\h, width.neg + (2 * trx) ],
					[\c, c * trx.neg, 0, trx.neg, (1-c) * try.neg, trx.neg, try.neg ],
					[\v, height.neg + (2 * try)],
					[\c, 0, c * try.neg, (1-c) * trx,  try.neg, trx, try.neg ]
					];
				}
				{ };
			}
		^SVGPath( segs, name, strokeColor, fillColor, strokeWidth, transform )
		}

		
	asAttributesArray {
		^[	[ "x",		x ], 
   			[ "y",		y ], 
   			[ "width",	width ], 
   			[ "height", 	height ],
   			[ "rx",		rx ], 
   			[ "ry",		ry ] ]
   			 ++ super.asAttributesArray;
   		   }
			
	*fromDOMElement {  arg tag;
			^this.new(tag.getAttribute( "x").interpret,
	   				tag.getAttribute( "y").interpret,
	   				tag.getAttribute( "width").interpret, 
	   				tag.getAttribute( "height").interpret, 
	   				tag.getAttribute( "rx").interpret,
	   				tag.getAttribute( "ry").interpret,
					tag.getAttribute("id"), 
					tag.getAttribute("stroke"),
					tag.getAttribute("fill"),
					tag.getAttribute("stroke-width").interpret,
					SVGTransform.fromDOMElement( tag )
					).opacityFromDOMElement( tag );
			 }
			 
	asPenLinesFunc {
		var rect;
		 if(  ((rx ? 0) == 0) && ((ry ? 0) == 0) )
			{ rect = this.asRect;
				^{ GUI.pen.addRect( rect ); }; }
			{ ^this.asSVGPath( true ).asPenLinesFunc; };
		}
		
	}
	
SVGCircle : SVGGraphicObject {
	  
	var <>cx, <>cy, <>r, <>strokeColor, <>fillColor, <>strokeWidth;
	
	// strokeColor and fillColor can be Color, string with color name, "none" or nil
	
	elementName { ^"circle" }
	
	*new { |cx = 0, cy = 0, r = 0, name, strokeColor, fillColor, strokeWidth, transform|
		  //strokeColor = strokeColor ? Color.black;
		  //strokeColor = strokeColor.asColor;
		  //strokeWidth = strokeWidth ? 1;
		  ^super.newCopyArgs( name, transform, cx, cy, r, strokeColor, fillColor, strokeWidth );
		  /*
		  ^super.newFromDict( 
		  	( 	name: name, transform: transform, 
		  		cx: cx, cy: cy, r: r, 
		  		strokeColor: strokeColor, fillColor: fillColor, strokeWidth: strokeWidth )
		  		 );
		  */
		}
		  
	asRect { ^Rect.aboutPoint( cx@cy, r, r ); }
	
	asSVGEllipse { ^SVGEllipse( cx, cy, r, r,  name, 
			strokeColor, fillColor, strokeWidth, transform ) }
	
	asSVGPath { |relative = true| ^this.asSVGEllipse.asSVGPath( relative ); }
			
	asAttributesArray {
		^[	[ "cx",		cx ], 
   			[ "cy",		cy ], 
   			[ "r",		r ] ]
   			++ super.asAttributesArray;
   	   }
			
	*fromDOMElement {  arg tag;
			^this.new(tag.getAttribute( "cx").interpret,
	   				tag.getAttribute( "cy").interpret,
	   				tag.getAttribute( "r").interpret,
					tag.getAttribute("id"), 
					tag.getAttribute("stroke"),
					tag.getAttribute("fill"),
					tag.getAttribute("stroke-width").interpret,
					SVGTransform.fromDOMElement( tag )
					).opacityFromDOMElement( tag );
			 }
			 
	center { ^cx@cy }
			 
	fillFunc { ^{ GUI.pen.fillOval( this.asRect ); }; }
	strokeFunc { ^{ GUI.pen.addArc( this.center, this.r, 0, 2pi ).stroke; }; }
	
	}
	
SVGEllipse : SVGGraphicObject {
	  
	
	var <>cx, <>cy, <>rx, <>ry, <>strokeColor, <>fillColor, <>strokeWidth;
	
	// strokeColor and fillColor can be Color, string with color name, "none" or nil
	// rx *& ry not yet implemented
	
	elementName { ^"ellipse" }
	
	*new { |cx = 0, cy = 0, rx = 0, ry = 0, name, 
			strokeColor, fillColor, strokeWidth, transform|
		  //strokeColor = strokeColor ? Color.black;
		  //strokeColor = strokeColor.asColor;
		  //strokeWidth = strokeWidth ? 1;
		  ^super.newCopyArgs( name, transform, 
		  	cx, cy, rx, ry, strokeColor, fillColor, strokeWidth );
		  }
		
	asRect { ^Rect.aboutPoint( cx@cy, rx, ry ); }
	
	*fromRect { |rect, name, strokeColor, fillColor, strokeWidth, transform|
		rect = rect ? Rect(0,0,100,200);
		^this.new( rect.center.x, rect.center.y, rect.width / 2, rect.height / 2, 
			name, strokeColor, fillColor, strokeWidth, transform );
		}
		
	asSVGPath { |relative = true|
		// convert to curve segments
		var c, segs;
		c = 0.75 / ((1.9)**0.5 );  // curve amount for circle approximation
		if( relative )
			{ segs = [
				[\M, cx, cy - ry ],
				[\c, c * rx, 0, rx,  (1-c) * ry, rx, ry ],
				[\c, 0, c * ry, (1-c) * rx.neg, ry,  rx.neg, ry ],
				[\c, c * rx.neg, 0, rx.neg, (1-c) * ry.neg, rx.neg, ry.neg ],
				[\c, 0, c * ry.neg, (1-c) * rx,  ry.neg, rx, ry.neg ]
				// , [\z]  // needed?
				]; } 
			{ segs = [
				[\M, cx, cy - ry ],
				[\C, cx + (c * rx), cy - ry , cx + rx, cy - (c * ry), cx + rx, cy ],
				[\C, cx + rx, cy + (c * ry), cx + (c * rx), cy + ry,  cx, cy + ry ],
				[\C, cx - (c * rx), cy + ry, cx - rx, cy + (c * ry), cx - rx, cy ],
				[\C, cx - rx, cy - (c * ry),  cx - (c * rx),  cy - ry, cx, cy - ry ]
				// , [\z]  // needed?
				];  
			};
		^SVGPath( segs, name, strokeColor, fillColor, strokeWidth, transform );
		}
		
	asAttributesArray {
		^[	[ "cx",		cx ], 
   			[ "cy",		cy ], 
   			[ "rx",		rx ], 
   			[ "ry", 		ry ]
   		] ++ super.asAttributesArray;
	   }
	
			
	*fromDOMElement {  arg tag;
			^this.new(tag.getAttribute( "cx").interpret,
	   				tag.getAttribute( "cy").interpret,
	   				tag.getAttribute( "rx").interpret, 
	   				tag.getAttribute( "ry").interpret, 
					tag.getAttribute("id"), 
					tag.getAttribute("stroke"),
					tag.getAttribute("fill"),
					tag.getAttribute("stroke-width").interpret,
					SVGTransform.fromDOMElement( tag )
					).opacityFromDOMElement( tag );
			 }
			 
	fillFunc { ^{ GUI.pen.fillOval( this.asRect ); }; }
	strokeFunc { ^{ GUI.pen.strokeOval( this.asRect ); }; }
	
	}

  
SVGLine : SVGGraphicObject {
	
	var <>x1, <>y1, <>x2, <>y2, <>strokeColor, <>fillColor, <>strokeWidth;
	
	// strokeColor and fillColor can be Color, string with color name, "none" or nil
	// fillColor might be obsolete..
	
	elementName { ^"line" }
	
	*new { |x1 = 0, y1 = 0, x2 = 0, y2 = 0, name, 
			strokeColor, fillColor, strokeWidth, transform|
		  //strokeColor = strokeColor ? Color.black;
		  //strokeColor = strokeColor.asColor;
		  //strokeWidth = strokeWidth ? 1;
		  ^super.newCopyArgs( name, transform, 
		  	x1, y1, x2, y2, strokeColor, fillColor, strokeWidth );
		  }
		
	asRect { ^Rect.fromPoints( x1@y1, x2@y2 ); }
	
	asPoints { ^[x1@y1, x2@y2] }
	
	asSVGPath { |relative = true|
		var segs;
		if( relative )
			{ segs = [[ \M, x1, y1 ], [\l, x2 - x1, y2 - y1 ]]; }
			{ segs = [[ \M, x1, y1 ], [\L, x2, y2 ]]; };
		^SVGPath( segs, name, strokeColor, fillColor, strokeWidth, transform )
		}
	
	*fromPoints { |points, name, strokeColor, fillColor, strokeWidth, transform|
		points = points ? [ 0@0, 0@0 ];
		^this.new( points[0].x, points[0].y, points[1].x, points[1].y,
			name, strokeColor, fillColor, strokeWidth, transform );
		}
		
	asAttributesArray {
		^[	[ "x1",		x1 ], 
   			[ "y1",		y1 ], 
   			[ "x2",		x2 ], 
   			[ "y2", 		y2 ]
   			] ++ super.asAttributesArray
	   }
	
			
	*fromDOMElement {  arg tag;
			^this.new(tag.getAttribute( "x1").interpret,
	   				tag.getAttribute( "y1").interpret,
	   				tag.getAttribute( "x2").interpret, 
	   				tag.getAttribute( "y2").interpret, 
					tag.getAttribute("id"), 
					tag.getAttribute("stroke"),
					tag.getAttribute("fill"),
					tag.getAttribute("stroke-width").interpret,
					SVGTransform.fromDOMElement( tag )
					).opacityFromDOMElement( tag );
			 }
			 
	asPenLinesFunc { 
			var points;
			points = this.asPoints;
			^{ GUI.pen.moveTo( points[0] );
				GUI.pen.lineTo( points[1] ); }
		}
		
	}
	



SVGTransform {
	var <string, <dict; // dict format: [ \type, [ ... arguments ] ]
	
	// matrix(n,n,n,n,n,n), translate(n,n), scale(n, [n] ), 
	// rotate(n, [n,n]), skewX(n), skewY(n)
	
	*new { |string|
		case { string.class == String }
			{ ^super.newCopyArgs( string, [] ).stringToDict; }
			{ string.isArray }
			{ ^super.newCopyArgs( "", string ).dictToString;  }
			{ true }
			{ ^super.newCopyArgs( "", [] ) };
		}
		
	addToString { |addString|
		string = string + addString.asString;
		this.stringToDict;
		}
		
	addToDict { |key, args |
		dict = dict ++ [ [ key.asSymbol, args.asCollection ] ];
		this.dictToString;
		}
		
	string_ { |newString|	
		string = newString.asString;
		this.stringToDict;
		}
		
	dict_ { |newDict|	
		dict = newDict;
		this.dictToString;
		}
		
	asSVGTransform { ^this }
	
	stringToDict {
		var array;
		array = (string ? "").split( $) /*(*/ ).collect({ |item| var out;
			out = item.split( $( /*)*/ );
		if( out.size == 2 )
			{ out[0] = out[0].trimLeft.asSymbol;
				if( out[1].any( _ == $, ) )
			 		{ out[1] = ("[" ++ out[1].split( $, )
			 			.select({ |item| item.size != 0 })
			 			.collect({ |item|
			 				if( item.trim[0] == $. )
			 					{ "0" ++ item }
			 					{ item }; }).join( ", " ) ++ "]").interpret }
			 		{ out[1] = ("[" ++ out[1].split( $ )
			 			.select({ |item| item.size != 0 }).join( ", " ) ++ "]").interpret };
			  out }
			{ nil } }).select( _.notNil );
		dict = array;
		//array.do({ |item| dict[ item[0] ] = item[1] });
		}
		
	dictToString {  
		var newString = "";
		dict.do({ |item|
			var key, value;
			#key, value = item;
			newString = newString ++ key ++ "( "; /*)*/
			value.do({ |subValue, i| 
				newString = newString ++ subValue ++
					( if( i < ( value.size - 1) ) { ", " } { /*(*/ ") " } )
				});
			});
		string = newString;
		}
		
	asPenFunction { 
		^{ dict.do({ |item|
		
			case { item[0] == \rotate } // degrees to radians
				{ GUI.pen.perform( *([ item[0] ] ++ (item[1] * [(1/360) * 2pi, 1, 1]) ) ); }
				{ (item[0] == \scale) && { item[1].size == 1 } }
				{ GUI.pen.perform( *([ item[0] ] ++ item[1] ++ item[1]) ); }
				{ item[0] == \matrix }
				{ GUI.pen.perform( *([ \matrix_ ] ++ [ item[1] ]) ); }
				{ item[0] == \skewX }
				{ GUI.pen.perform( *([ \skew ] ++ [ item[1][0], 0 ]) ); }
				{ item[0] == \skewY }
				{ GUI.pen.perform( *([ \skew ] ++ [ 0, item[1][0] ]) ); }
				{ true }
				{ GUI.pen.perform( *([ item[0] ] ++ item[1]) ); };
				
			}); }
		}
		
	asGUICode { |penClass| ^{ this.asPenFunction.value }.asGUICode( penClass ) }
	
	*fromDOMElement { |tag|
		var transformAttribute;
		transformAttribute = tag.getAttribute("transform");
			if( transformAttribute.notNil ) 
				{ ^SVGTransform( transformAttribute ); }
				{ ^nil };
		}
		
	printOn { |stream| 
		stream << "SVGTransform( " << string.quote << " )";
		}
	
	 }


