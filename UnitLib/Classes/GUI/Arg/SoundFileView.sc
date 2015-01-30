/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

BufSndFileView {
	
	classvar <timeMode = \frames; // or \seconds
	classvar <rateMode = \semitones; // or \ratio
	classvar <all; 
	
	var <sndFile;
	var <parent, <view, <views;
	var <>action;
	var <viewHeight = 14;
	var <>autoCreateSndFile = false;
	
	*new { |parent, bounds, action, sndFile|
		^super.new.init( parent, bounds, action ).value_( sndFile ).addToAll;
	}
	
	init { |parent, bounds, inAction|
		action = inAction;
		this.makeView( parent, bounds );
	}
	
	addToAll {
		all = all.add( this );
	}
	
	*timeMode_ { |new = \frames|
		timeMode = new.asSymbol;
		all.do( _.setTimeMode( timeMode ) );
	}
	
	*rateMode_ { |new = \semitones|
		rateMode = new.asSymbol;
		all.do( _.setRateMode( rateMode ) );
	}
	
	doAction { action.value( this ) }
	
	value { ^sndFile }
	value_ { |newSndFile|
		if( sndFile != newSndFile ) {
			sndFile.removeDependant( this );
			sndFile = newSndFile;
			sndFile.addDependant( this );
			this.update;
		};
	}
	
	update {
		if( sndFile.notNil ) { this.setViews( sndFile ) };
	}
	
	resize_ { |resize|
		view.resize = resize ? 5;
	}
	
	remove {
		if( sndFile.notNil ) { 
			sndFile.removeDependant( this );
		};
		all.remove( this );
	}
	
	setViews { |inSndFile|
		
		views[ \path ].value = inSndFile.path;
		views[ \path ].stringColor = if( inSndFile.exists ) { Color.black; } { Color.red(0.66); };
		
		views[ \startFrame ].value = inSndFile.startFrame;
		views[ \startFrame ].clipHi = inSndFile.numFrames ? inf;
		
		views[ \startSecond ].value = inSndFile.startSecond;
		views[ \startSecond ].clipHi = inSndFile.fileDuration ? inf;
		
		views[ \endFrame ].value = inSndFile.endFrame;
		views[ \endFrame ].clipHi = inSndFile.numFrames ? inf;
		
		views[ \endSecond ].value = inSndFile.endSecond;
		views[ \endSecond ].clipHi = inSndFile.fileDuration ? inf;
		
		views[ \loop ].value = inSndFile.loop.binaryValue;
		
		views[ \rateRatio ].value = inSndFile.rate;
		views[ \rateSemitones ].value = inSndFile.rate.ratiomidi.round( 1e-6);
		
		{ views[ \numChannels ].string = "% channel%".format( 
				inSndFile.numChannels, 
				if( inSndFile.numChannels == 1 ) { "" } { "s" }
			)
		}.defer;
		
	}
	
	setTimeMode { |mode = \frames|
		switch ( ( mode.asString[0] ? $f ).toLower,
			$s, { // \seconds
				views[ \startSecond ].visible_( true );
				views[ \startFrame ].visible_( false );
				views[ \endSecond ].visible_( true );
				views[ \endFrame ].visible_( false );
				{ views[ \timeMode ].value = 0 }.defer;
						
			}, 
			$f, { // \frames
				views[ \startSecond ].visible_( false );
				views[ \startFrame ].visible_( true );
				views[ \endSecond ].visible_( false );
				views[ \endFrame ].visible_( true );
				{ views[ \timeMode ].value = 1 }.defer;
			}
		);
	}
	
	setRateMode { |mode = \semitones|
		switch( ( mode.asString[0] ? $s ).toLower,
			$r, { // \ratio
				views[ \rateRatio ].visible_( true );
				views[ \rateSemitones ].visible_( false );
				{ views[ \rateMode ].value = 0 }.defer;
			}, 
			$s, { 
				views[ \rateRatio ].visible_( false );
				views[ \rateSemitones ].visible_( true );
				{ views[ \rateMode ].value = 1 }.defer;
			}
		);
	}
	
	setFont { |font|
		font = font ??
			{ RoundView.skin !? { RoundView.skin.font } } ?? 
			{ Font( Font.defaultSansFace, 10 ) };
		
		{
			views[ \startLabel ].font = font;
			views[ \timeMode ].font = font;
			views[ \endLabel ].font = font;
			views[ \rateLabel ].font = font;
			views[ \rateMode ].font = font;
		}.defer;
		
		views[ \path ].font = font;
		views[ \startFrame ].font = font;
		views[ \startSecond ].font = font;
		views[ \endFrame ].font = font;
		views[ \endSecond ].font = font;
		views[ \loop ].font = font;
		views[ \rateRatio ].font = font;
		views[ \rateSemitones ].font = font;

	}
	
	performSndFile { |selector ...args|
		if( sndFile.notNil ) {
			^sndFile.perform( selector, *args );
		} {
			if( autoCreateSndFile ) {
				this.value = BufSndFile.newBasic( );
				^sndFile.perform( selector, *args );
			} {
				^nil;
			};
		};
	}
	
	*viewNumLines { ^6 }
	
	makeView { |parent, bounds, resize|
		
		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
		
		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.onClose_({ this.remove; });
		view.resize_( resize ? 5 );
		views = ();
		
		views[ \path ] = FilePathView( view, bounds.width @ ( (viewHeight * 2) + 4) )
			.resize_( 2 )
			.action_({ |fv|
				this.performSndFile( \path_ , fv.value );
				this.performSndFile( \fromFile );
				action.value( this );
			});
			
		views[ \operations ] = PopUpMenu( view, 80 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.items_( [
				"operations",
				"",
				"reveal in Finder",
				"move to..",
				"copy to..",
				"save as.."
			] )
			.action_({ |pu|
				var pth, ext;
				switch( pu.value.asInt,
					2, {  // reveal in Finder
						pth = this.performSndFile( \path );
						if( pth.notNil ) {
							pth.getGPath.asPathFromServer.revealInFinder;
						};
					},
					3, { // move to..
						pth = this.performSndFile( \path );
						if( pth.notNil ) {
							pth = pth.getGPath;
							if( pth[..6] == "sounds/" ) {
								"can't move %, try copying instead\n".postf( pth.quote );
							};
							Dialog.savePanel({ |path|
								var res, newName;
								newName = path.dirname +/+ pth.basename;
								if( File.exists( newName ).not ) {
									res = pth.getGPath.asPathFromServer.moveTo( path.dirname ); 
									if( res ) {
										this.performSndFile( \path_ ,
											newName
										);
									};
								} {
									"file % already exists, changing url".postf( pth.quote );
									this.performSndFile( \path_ ,
										path.dirname +/+ pth.basename 
									);
								};
							});
						};
					},
					4, { // copy to..
						pth = this.performSndFile( \path );
						if( pth.notNil ) {
							Dialog.savePanel({ |path|
								var res, newName;
								newName = path.dirname +/+ pth.basename;
								if( File.exists( newName ).not ) {
									res = pth.getGPath.asPathFromServer.copyTo( path.dirname ); 
									if( res ) {
										this.performSndFile( \path_ ,
											path.dirname +/+ pth.basename 
										);
									};
								} {
									"file % already exists, changing url".postf( pth.quote );
									this.performSndFile( \path_ ,
										path.dirname +/+ pth.basename 
									);
								};
							});
						};
					},
					5, { // save as..
						pth = this.performSndFile( \path );
						if( pth.notNil ) {
							ext = pth.extension;
							Dialog.savePanel({ |path|
								var res;
								path =  path.replaceExtension( ext );
								res = pth.getGPath.asPathFromServer.copyFile( path ); 
								if( res ) {
									this.performSndFile( \path_ , path );
								};
							});
						};
					}
				);
				pu.value = 0;
			});
			
		views[ \plot ] = SmoothButton( view, 40 @ viewHeight )
			.radius_( 3 )
			.border_( 1 )
			.resize_( 3 )
			.label_( "plot" )
			.action_({ |bt|
				
				// this will have to go in a separate class
				var w, a, f, b, x;
				var closeFunc;
				
				x = sndFile;
				f = this.performSndFile( \asSoundFile );
				
				w = Window(f.path, Rect(200, 200, 850, 400), scroll: false);
				a = SoundFileView.new(w, w.view.bounds);
				a.resize_(5);
				a.soundfile = f;
				a.read(0, f.numFrames);
				a.elasticMode_(1);
				a.gridOn = true;
				a.gridColor_( Color.gray(0.5).alpha_(0.5) );
				a.waveColors = Color.gray(0.2)!16;
				w.front;
				a.background = Gradient( Color.white, Color.gray(0.7), \v );
				b = SmoothRangeSlider( w, a.bounds.insetAll(1,1,1,1) )
					.knobSize_(0)
					.resize_(5)
					.background_( nil )
					.hiliteColor_( Color.blue(0.2).alpha_(0.2) );
				b.action = { |sl|
					x.startFrame = (sl.lo * x.numFrames).round(1).asInteger;
					x.endFrame = (sl.hi * x.numFrames).round(1).asInteger;
				};
				b.lo = x.startFrame / x.numFrames;
				b.hi = x.endFrame / x.numFrames;
				
				closeFunc = { w.close; };
				
				w.onClose = { bt.onClose.removeFunc( closeFunc ) };
				
				bt.onClose = bt.onClose.addFunc( closeFunc );
					
			});
			
		views[ \numChannels ] = StaticText( view, 60 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.string_( "" );
			
		view.view.decorator.nextLine;
					
		views[ \startLabel ] = StaticText( view, 30 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.string_( "start" );
		
		views[ \startComp ] = CompositeView( view, (bounds.width - 78) @ viewHeight )
			.resize_( 2 );
		
		views[ \startSecond ] = SMPTEBox( views[ \startComp ], 
				views[ \startComp ].bounds.moveTo(0,0) )
			.applySmoothSkin
			.resize_( 5 )
			.clipLo_( 0 )
			.action_({ |nb|
				this.performSndFile( \startSecond_ , nb.value );
				action.value( this );
			});
			
		views[ \startFrame] = SmoothNumberBox( views[ \startComp ], 
				views[ \startComp ].bounds.moveTo(0,0) )
			.resize_( 5 )
			.clipLo_( 0 )
			.action_({ |nb|
				this.performSndFile( \startFrame_ , nb.value );
				action.value( this );
			})
			.visible_( false );
		
		views[ \timeMode ] = PopUpMenu( view, 40 @ viewHeight )
			.applySkin( RoundView.skin ? ())
			.items_( [ "s", "smp" ] )
			.resize_( 3 )
			.action_({ |pu|
				this.class.timeMode = [ \seconds, \frames ][ pu.value ];
			});
			
		views[ \endLabel ] = StaticText( view, 30 @ viewHeight )
			.applySkin( RoundView.skin )
			.string_( "end" );
		
		views[ \endComp ] = CompositeView( view, (bounds.width - 78) @ viewHeight )
			.resize_( 2 );
		
		views[ \endSecond ] = SMPTEBox( views[ \endComp ], 
				views[ \endComp ].bounds.moveTo(0,0) )
			.applySmoothSkin
			.resize_( 5 )
			.clipLo_( 0 )
			.action_({ |nb|
				this.performSndFile( \endSecond_ , nb.value );
				action.value( this );
			});
			
		views[ \endFrame] = SmoothNumberBox( views[ \endComp ], 
				views[ \endComp ].bounds.moveTo(0,0) )
			.resize_( 5 )
			.clipLo_( 0 )
			.action_({ |nb|
				this.performSndFile( \endFrame_ , nb.value );
				action.value( this );
			})
			.visible_( false );
		
		views[ \loop ] = SmoothButton( view, 40 @ viewHeight )
			.radius_( 3 )
			.border_( 1 )
			.resize_( 3 )
			.label_( [ "loop", "loop" ] )
			.hiliteColor_( Color.green )
			.action_({ |bt|
				this.performSndFile( \loop_ , bt.value.booleanValue );
				action.value( this );
			});
			
		views[ \rateLabel ] = StaticText( view, 30 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.string_( "rate" );
			
		views[ \rateComp ] = CompositeView( view, (bounds.width - 118) @ viewHeight )
			.resize_( 2 );
		
		views[ \rateRatio ] = SmoothNumberBox( views[ \rateComp ], 
				views[ \rateComp ].bounds.moveTo(0,0) )
			.resize_( 5 )
			.scroll_step_( 0.1 )
			.clipLo_( 0 )
			.value_( 1 )
			.action_({ |nb|
				this.performSndFile( \rate_ , nb.value );
				action.value( this );
			});
			
		views[ \rateSemitones ] = SmoothNumberBox( views[ \rateComp ], 
				views[ \rateComp ].bounds.moveTo(0,0) )
			.resize_( 5 )
			.action_({ |nb|
				this.performSndFile( \rate_ , nb.value.midiratio );
				action.value( this );
			})
			.visible_( false );
			
		views[ \rateMode ] = PopUpMenu( view, 80 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.items_( [ "ratio", "semitones" ] )
			.resize_( 3 )
			.action_({ |pu|
				this.class.rateMode = [ \ratio, \semitones ][ pu.value ];
			});
			
		this.setFont;
		this.setTimeMode( timeMode );
		this.setRateMode( rateMode );
	}
	
}

DiskSndFileView : BufSndFileView {
	
	performSndFile { |selector ...args|
		if( sndFile.notNil ) {
			^sndFile.perform( selector, *args );
		} {
			if( autoCreateSndFile ) {
				this.value = DiskSndFile.newBasic( );
				^sndFile.perform( selector, *args );
			} {
				^nil;
			};
		};
	}
}
