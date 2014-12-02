+ ColPen {

	*asSVGGroup { |clear = true, inCmds, index = 0, inTransform|
		var group, currentColor = Color.black, currentFillColor, currentWidth = 1;
		var currentPath, currentTransform, np, nt, cStart, cEnd;
		var font;
		font = Font.default ?? { Font( "Helvetica", 12 ) };
		inCmds = inCmds ?? { this.collectUse };
		group = SVGGroup( [], "group" + index, inTransform );
		inCmds.do({ |cmd|
			if( cmd[0] == \use )
				{ group.add( this.asSVGGroup( false, cmd[1..], (index = index + 1), 
					currentTransform ) ); }
				{
				np = { currentPath = currentPath ?? { SVGPath( [], "path " ++ 
						(index = index + 1) ); }; };
				nt = { currentTransform = currentTransform ?? 
						{ SVGTransform( "" ); }; };
				switch ( cmd[0],
					
					// rects and ovals
					\strokeRect, { group.add( 
						SVGRect.fromRect( cmd[1], "strokeRect " ++ (index = index + 1),  
								currentColor, "none", currentWidth, currentTransform ) ) 
							}, 
					\fillRect, { group.add( 
						SVGRect.fromRect( cmd[1], "fillRect " ++ (index = index + 1),  
								"none", currentFillColor ? currentColor,
								nil, currentTransform ) ) 
							}, 
					\strokeOval, { group.add( 
						SVGEllipse.fromRect( cmd[1], "strokeOval " ++ (index = index + 1),  
								currentColor, "none", currentWidth, currentTransform ) ) 
							}, 
					\fillOval, { group.add( 
						SVGEllipse.fromRect( cmd[1], "fillOval " ++ (index = index + 1),  
								"none", currentFillColor ? currentColor,
								nil, currentTransform ) ) 
							}, 
					
					// paths:
					\moveTo, { np.value; currentPath.add( [ \M, cmd[1].x, cmd[1].y ] ) },
					\lineTo, { np.value; currentPath.add( [ \L, cmd[1].x, cmd[1].y ] ) },
					\curveTo,{ np.value; currentPath.add( 
						[ \C ] ++ cmd[[3,1,2]].collect(_.asArray).flat )  },
					\quadCurveTo,{ np.value; currentPath.add( 
						[ \Q ] ++ cmd[[2,1]].collect(_.asArray).flat )  },
						
					\addArc, { // will not draw within SC, but should in other apps
						np.value;  
						cStart = cmd[1] + ((cmd[3].cos * cmd[2])@(cmd[3].sin * cmd[2]));
						cEnd = cmd[1] + (((cmd[3] + cmd[4].min(1.9999pi).max(-1.9999pi)).cos * cmd[2])@
							((cmd[3] + cmd[4].min(1.9999pi).max(-1.9999pi)).sin * cmd[2]));
						if( currentPath.segments.size == 0 )
							{ currentPath.add( [\M, cStart.x, cStart.y] ) }
							{ currentPath.add( [\L, cStart.x, cStart.y] ) };
						currentPath.add( [\A, cmd[2], cmd[2], 0, 
							(cmd[4].abs > pi).binaryValue, 
							cmd[4].isPositive.binaryValue, cEnd.x, cEnd.y ] );
						},
						
					\addWedge, { // will not draw within SC, but should in other apps
						np.value;   
						cStart = cmd[1] + ((cmd[3].cos * cmd[2])@(cmd[3].sin * cmd[2]));
						cEnd =  cmd[1] + (((cmd[3] + cmd[4].min(1.9999pi).max(-1.9999pi)).cos * cmd[2])@
							((cmd[3] + cmd[4].min(1.9999pi).max(-1.9999pi)).sin * cmd[2]));
						currentPath.addAll( [
							[\M, cmd[1].x, cmd[1].y ],
							[\L, cStart.x, cStart.y ],
							[\A, cmd[2], cmd[2], 0, (cmd[4] > pi).binaryValue, cmd[4].isPositive.binaryValue, cEnd.x, cEnd.y ],
							[\L, cmd[1].x, cmd[1].y ]
							] );
						},
						
					\addAnnularWedge, { np.value;   
						np.value;   
						cStart = ((cmd[4].cos)@(cmd[4].sin));
						cEnd = (((cmd[4] + cmd[5]).cos)@((cmd[4] + cmd[5]).sin));
						currentPath.addAll( [
							// move to start inner radius
							[\M, (cStart.x * cmd[2]) + cmd[1].x, (cStart.y * cmd[2]) + cmd[1].y ],
							// line to start outer radius
							[\L, (cStart.x * cmd[3]) + cmd[1].x, (cStart.y * cmd[3]) + cmd[1].y ],
							// outer arc
							[\A, cmd[3], cmd[3], 0, (cmd[5] > pi).binaryValue, 1, 
								(cEnd.x * cmd[3]) + cmd[1].x, (cEnd.y * cmd[3]) + cmd[1].y ],
							// line to end inner arc
							[\L, (cEnd.x * cmd[2]) + cmd[1].x, (cEnd.y * cmd[2]) + cmd[1].y ],
							// reversed inner arc
							[\A, cmd[2], cmd[2], 0, (cmd[5] > pi).binaryValue, 0, 
								(cStart.x * cmd[2]) + cmd[1].x, (cStart.y * cmd[2]) + cmd[1].y ]
							] );
						 },
					
					\addRect, { np.value; currentPath.addAll( [ 
						[\M, cmd[1].left, cmd[1].top],
						[\H, cmd[1].right],
						[\V, cmd[1].bottom],
						[\H, cmd[1].left],
						[\V, cmd[1].top],
						]);
						},
						
					\beginPath, { np.value; currentPath.segments = []; },
					
					\stroke, { if( currentPath.notNil ) { group.add( currentPath
							.strokeColor_( currentColor )
							.fillColor_( nil )
							.strokeWidth_( currentWidth )
							.transform_( currentTransform )
							.strokeOpacity_( currentColor.alpha.postln ) );
						currentPath = nil; };
						},
					\fill, { if( currentPath.notNil ) { group.add( currentPath
							.fillColor_( currentFillColor ? currentColor )
							.strokeColor_( nil )
							.transform_( currentTransform )
							.fillOpacity_( (currentFillColor ? currentColor).alpha ) ); 
						currentPath = nil; };
						},
					
					\clip, { currentPath = nil; /* not supported yet in SVGFile */ },
					
					// settings
					\color_, { currentColor = cmd[1]; currentFillColor = cmd[1] },
					\fillColor_, { currentFillColor = cmd[1] },
					\strokeColor_, { currentColor = cmd[1] },
					\width_, { currentWidth = cmd[1] },
					
					// transform
					\translate, { nt.value; currentTransform.addToDict( 
						 \translate, [cmd[1], cmd[2] ? 0]  ); },
					\scale, { nt.value; currentTransform.addToDict( 
						 \scale,  [cmd[1], cmd[2] ? 0]  ); },
					\skew, { nt.value; 
						if( cmd[1] != 0 )
							{ currentTransform.addToDict( \skewX, [ cmd[1] ]  ); };
						if( cmd[2].notNil && { cmd[2] != 0 } )
							{ currentTransform.addToDict( \skewY, [ cmd[2] ]  ); };
						 },
					\rotate, { nt.value; 
						currentTransform.addToDict( 
						 \rotate, [(cmd[1] / 2pi) * 360, cmd[2] ? 0, cmd[3] ? 0 ] ); },
					\matrix_, { nt.value; 
						currentTransform.addToDict(  \matrix, cmds[1] );  },
					
					// strings
					\font_, { font = cmd[1]; }, // not set until \string is called
					\string, { group.add( 
						SVGText( cmd[1], 0, 0,
							font.name.asString, font.size,
							 currentColor,
								"string " ++ (index = index + 1), currentTransform ) ) 
							},
					\stringAtPoint, { group.add( 
						SVGText( cmd[1], cmd[2].x, cmd[2].y,
							font.name.asString, font.size,
							 currentColor,
								"string " ++ (index = index + 1), currentTransform ) ) 
							}

					/* more string: to do */
					
					)
					
				
				}
			});
		if( clear ) { this.clear };
		^group;
		}
		
	*asSVGFile { |path, clear = true|	
		^SVGFile( path ? "~/Desktop/ColPen.svg", [ this.asSVGGroup( clear ) ] );
		}
}


+ Function {

	asSVGFile { |path|
		// works only for GUI.pen commands inside the function
		var currentScheme, file;
		currentScheme = GUI.current.id;
		GUI.fromID( \colpen );
		this.value;
		GUI.fromID( currentScheme );
		^ColPen.asSVGFile( path );
		}
		
	asGUICode { |penClass, indent = 0 ...args|
		// works only for GUI.pen commands inside the function
		var currentScheme, file;
		currentScheme = GUI.current.id;
		GUI.fromID( \colpen );
		this.value( *args );
		GUI.fromID( currentScheme );
		^ColPen.asCode( penClass, indent: indent);
		
		}
	}
	
+ SVGFile {
	asGUICode { |penClass| ^this.asPenFunction.asGUICode( penClass ); }
	}
	
+ SVGObject {
	asGUICode { |penClass| ^this.asPenFunction.asGUICode( penClass ); }
	}