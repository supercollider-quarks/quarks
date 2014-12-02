// wslib 2007
// Pen function -> PostScript converter

// .ps files can be opened in OSX Preview and there converted to PDF
// writePDF only works if GhostScript unix executable is installed ( /usr/local/bin/gs )
// http://www.ghostscript.com/awki

+ ColPen {

	*asPostScript { |clear = true, pageSize, header|
		var string, convert;
		var font;
		
		font = Font.default ?? { Font( "Helvetica", 12 ) };
		
		pageSize = ( pageSize ?? { 400@400 } ).asPoint;
		
		string = (header ?? {"%!PS\n\n"}).asString; // PostScript header (in its simples form)
		
		string = string ++ "<</PageSize [% %]>> setpagedevice\n".format( pageSize.x, pageSize.y );
		string = string ++ "1 -1 scale 0 % translate\n\n".format( pageSize.y.neg );
		
		convert = { |cmd|	
			var array;
			var c, rx, ry, cx, cy;
			var arcfunc;
			var startPoint;
			if( cmd[0] == \use )
				{ convert.( [\push] ); 
					cmd[1..].do({ |item| convert.( item ) });
					convert.( [\pop] ); 
				}
				{ 
				// cmd[0].post; " converting now".postln;
				array =  switch ( cmd[0],
					
						\push, { [ "gsave" ] },
						\pop,  { [ "grestore" ] },
						
						\strokeRect, { cmd[1].asArray ++ [ "rectstroke" ] }, 
						\fillRect, { cmd[1].asArray ++ [ "rectfill" ] }, 
						
						// ovalstroke doesn't exist
						// probably need to use curves for this
						// just paints a rect for now
						/*
						\strokeOval, { cmd[1].asArray ++ [ "rectstroke" ] }, 
						\fillOval, { cmd[1].asArray ++ [ "rectfill" ] }, 
						*/
						\strokeOval, {
							c = 0.75 / ((1.9)**0.5 );
							#cx, cy = cmd[1].center.asArray;
							#rx, ry = cmd[1].extent.asArray / 2;
							[ cx, cy - ry, "moveto\n",
							 cx + (c * rx), cy - ry, cx + rx, cy - (c * ry), cx + rx, cy,
							 "curveto\n",
							 cx + rx, cy + (c * ry), cx + (c * rx), cy + ry, cx, cy + ry,
							 "curveto\n",
							 cx - (c * rx), cy + ry, cx - rx, cy + (c * ry), cx - rx, cy,
							 "curveto\n", 
							 cx - rx, cy - (c * ry), cx - (c * rx), cy - ry, cx, cy - ry,
							 "curveto\n",
							 "stroke" ]
							},
						\fillOval, {
							c = 0.75 / ((1.9)**0.5 );
							#cx, cy = cmd[1].center.asArray;
							#rx, ry = cmd[1].extent.asArray / 2;
							[ cx, cy - ry, "moveto\n",
							 cx + (c * rx), cy - ry, cx + rx, cy - (c * ry), cx + rx, cy,
							 "curveto\n",
							 cx + rx, cy + (c * ry), cx + (c * rx), cy + ry, cx, cy + ry,
							 "curveto\n",
							 cx - (c * rx), cy + ry, cx - rx, cy + (c * ry), cx - rx, cy,
							 "curveto\n", 
							 cx - rx, cy - (c * ry), cx - (c * rx), cy - ry, cx, cy - ry,
							 "curveto\n",
							 "fill" ]
							},
						
						// paths:
						\moveTo, { [ cmd[1].x, cmd[1].y, "moveto" ] },
						\lineTo, { [ cmd[1].x, cmd[1].y, "lineto"  ]},
						\curveTo,{ [
							cmd[2].x, cmd[2].y, 
							cmd[3].x, cmd[3].y,
							cmd[1].x, cmd[1].y, "curveto" ]},
							
						//quad curve doesn't exist in postscript
						// use line for now
						\quadCurveTo,{[ cmd[1].x, cmd[1].y, "lineto"  ]},
						
						\addArc, { [ cmd[1].x, cmd[1].y, cmd[2], 
							(cmd[3] / 2pi) * 360, 
							(cmd[4] / 2pi) * 360, "arc" ] }, // clockwise arc
						
						// add later
						\addWedge, {
							arcfunc = { |arccmd|
								[ arccmd[1].x, arccmd[1].y, arccmd[2], 
							(arccmd[3] / 2pi) * 360, 
							(arccmd[4] / 2pi) * 360, "arc\n" ]
								};
							[ cmd[1].x, cmd[1].y, "moveto\n" ] ++
							arcfunc.value( cmd ) ++
							[ cmd[1].x, cmd[1].y, "lineto\n" ]
							},
						\addAnnularWedge, {
							
							arcfunc = { |center, radius, startAngle, arcAngle, nn = ""|
								[ center.x, center.y, radius, 
							(startAngle / 2pi) * 360, 
							(arcAngle / 2pi) * 360, "arc" ++ nn ++ "\n" ]
								};
							startPoint = cmd[1] + ( ((cmd[4].cos)@(cmd[4].sin)) * cmd[2] );
								
							[ startPoint.x, startPoint.y, "moveto\n" ] ++							arcfunc.value( cmd[1], cmd[3], cmd[4], cmd[5] ) // outer
								++
							arcfunc.value( cmd[1], cmd[2], cmd[4] + cmd[5], cmd[4], "n" ) 							
							},
						
						\addRect, {[ 
							cmd[1].left, cmd[1].top, "moveto\n",
							cmd[1].right, cmd[1].top, "lineto\n",
							cmd[1].right, cmd[1].bottom, "lineto\n",
							cmd[1].left, cmd[1].bottom, "lineto\n",
							cmd[1].left, cmd[1].top, "lineto"]
							},
						
						\beginPath, {[ "newpath" ]},
						\stroke, {[ "stroke" ]},
						\fill, {[ "fill" ]},
						
						\clip, {[ "clip" ]},
						
						// settings
						\color_, {[ cmd[1].red, cmd[1].green, cmd[1].blue,
							"setrgbcolor" ]},
						
						// not supported by PostScript?
						\fillColor_, {[ cmd[1].red, cmd[1].green, cmd[1].blue,
							"setrgbcolor" ]},
						\strokeColor_, {[ cmd[1].red, cmd[1].green, cmd[1].blue,
							"setrgbcolor" ]},
						\width_, {[ cmd[1], "setlinewidth" ]},
						
						// transform
						\translate, {[ cmd[1], cmd[2] ? 0, "translate" ]},
						\scale, {[ cmd[1], cmd[2] ? 0, "scale" ]},
						\skew, {[]}, //doesn't exist (should probably be a matrix)
						\rotate, {[ (cmd[1] / 2pi) * 360, "rotate" ]}, // not correct
						\matrix_ , {[ cmd[1], "setmatrix"  ]},
						
						// strings
						\font_, { font = cmd[1]; nil }, // not set until \string is called
						\string, { 
							[ 0, 0, "moveto",
							 "\n/" ++ font.name, "findfont", 
							 font.size, "scalefont", "setfont",
							 "\ngsave\n1 -1 scale",
							 "\n(" ++ cmd[1].tr( $(, "\\(" ).tr( $), "\\)" ) ++
							 ")", "show\ngrestore" ]  },
						\stringAtPoint, { 
							[ cmd[2].x, cmd[2].y, "moveto",
							 "\n/" ++ font.name, "findfont", 
							 font.size, "scalefont", "setfont",
							 "\ngsave\n1 -1 scale", 
							 "\n(" ++ cmd[1].tr( $(, "\\(" ).tr( $), "\\)" ) ++
							 ")", "show\ngrestore" ] }
							 
						/* " (<- correct syntax colorization) 
							more string: to do */
					
					);
				if( array.notNil ) { string = string ++ array.join( " " ) ++ "\n" };
				};
			};
		
		cmds.do({ |item| convert.(item); });
		if( clear ) { this.clear };
		^string;
		}
	}
	
+ Function {

	asPostScript { |pageSize ...args|  // pageSize should be point or array
	
		// works only for GUI.pen commands inside the function
		var currentScheme, file;
		currentScheme = GUI.current.id;
		GUI.fromID( \colpen );
		this.value( *args );
		GUI.fromID( currentScheme );
		^ColPen.asPostScript( true, pageSize );
		}
		
	writePostScript { |path, pageSize, args|
		path = path ? "~/Desktop/Function.ps";
		File.checkDo( path.standardizePath.replaceExtension( "ps" ), 
			this.asPostScript( pageSize, *args ) );
		}
		
	writePDF {  |path, pageSize, args|
		// requires GhostScript unix command (gs)
		// http://www.ghostscript.com/awki
		path = (path ? "~/Desktop/Function.pdf").replaceExtension( "pdf" );
		File.checkDo( "/tmp/" ++ path.basename.replaceExtension( "ps" ), 
			this.asPostScript( pageSize, *args ),
			true, // overwrite any existing 
			doneAction: { |file, pathName|
				File.checkDo( path, "", doneAction: { |file, finalPath|
					("/tmp/" ++ path.basename.replaceExtension( "ps" ))
						.ps2pdf( finalPath, true );
					});
				});
			}
	
	}
	
+ String {
	ps2pdf { |newPath, removePS = false| // always overwrites existing
		var path, gs, result;
		gs = "/usr/local/bin/gs"; // change if doesn't work
		if( gs.isFile ) {
			path = this.standardizePath;
			newPath = ( newPath ? path ).replaceExtension( "pdf" );
			if( path.isFile )
				{ result = ("PATH=$PATH:/usr/local/bin; gs -q -dNOPAUSE -dBATCH -sDEVICE=pdfwrite -sOutputFile="
					++ newPath.quote ++  " -c .setpdfwrite -f " ++  path.quote ).systemCmd;
				if( result != 0 )
					{ "String:ps2pdf failed:\n\t'%' might not have been written\n".postf( newPath );
					 if( removePS ) { (this ++ " was not removed").postln;  }; }
					{ this.removeFile( false, false ); };
				^newPath
				} {
				"String:ps2pdf '%' not found".warn;
				};
			} { ("Sorry, PDF conversion only works if GhostScript is installed." ++
			"\nit can be obtained at http://www.ghostscript.com/awki").postln;
			if( removePS )
				{ if( newPath.notNil )
					{ "\ttrying to move your .ps file to the desired location".postln;
					newPath = this.moveRename( newPath.dirname, this.basename );
					if( newPath != false )
						{ ^newPath }
						{ ^this }
					 };
				};
			};
		}
	}
	
+ SVGFile {

	asPostScript { 
	
		// works only for GUI.pen commands inside the function
		var currentScheme, file;
		currentScheme = GUI.current.id;
		GUI.fromID( \colpen );
		this.draw;
		GUI.fromID( currentScheme );
		^ColPen.asPostScript( true, (width.interpret)@(height.interpret) );
		}
		
	writePostScript { |argPath| 
		argPath = argPath ? path ? "~/Desktop/PostScript.ps";
		File.checkDo( path.standardizePath.replaceExtension( "ps" ), 
			this.asPostScript );
		}
		
	writePDF { |argPath| 
		// requires GhostScript unix command (gs)
		// http://www.ghostscript.com/awki
		argPath = (argPath ? path ? "~/Desktop/Function.pdf").replaceExtension( "pdf" );
		File.checkDo( "/tmp/" ++ argPath.basename.replaceExtension( "ps" ), 
			this.asPostScript,
			true, // overwrite any existing 
			doneAction: { |file, pathName|
				File.checkDo( argPath, "temp dummy file", doneAction: { |file, finalPath|
					("/tmp/" ++ argPath.basename.replaceExtension( "ps" ))
						.ps2pdf( finalPath, true );
					});
				});
			
		}
		
	
	}
	
	