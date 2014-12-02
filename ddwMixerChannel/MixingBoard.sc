
MixingBoard {
	// group a bunch of MixerChannels into a console
	// H. James Harkins - jamshark70@dewdrop-world.net
	
	classvar	<>defaultSkin,	// override MixerSkin's default
			<boards;			// collection of boards
							// so MixerChannel can update GUI
							
	var	<mixers,	// array of active MixerChannelGUI objects
		<w,		// gui window
		<skin,	// customize display
		<>name,	// window title bar
		<>onClose;	// standard in gui classes

	*initClass {
//		Class.initClassTree(MixerSkin);
		StartUp.add({
			defaultSkin = MixerSkin.new;
			boards = Array.new;
		});
	}
	
	*new { arg name = "Mixer", skin ... mixers;
		^super.new.init(name, skin, mixers);
	}
	
	*at { |i| ^boards[i] }

	init { arg name1, skin1 ... mixers1;

		mixers.do({ arg m; m.free; });	// free all mixers if they exist
		
		mixers = Array.new;

		name = name1;
		skin = skin1 ?? defaultSkin.copy;	// if no skin specified, use default
										// use copy so default is untouched
		
		boards = boards.add(this);	// save in collection
		^this.buildGUI(mixers1);	// draw it
	}
	
	free {
			// remove this mixing board and all channels
		onClose.value(this);
		w.isClosed.not.if({ w.onClose = nil; w.close; });
			// false says to skip gui updates (which would fail b/c window is gone)
		mixers.do({ arg m; m.mixer_(nil, false) });
		boards.remove(this);
	}
	
	add { arg ... mxs;
		// add mixers to console
		var oldsize;
		oldsize = mixers.size;	// save old size so we know where to
							// start making new views
		mxs = mxs.asFlatArray;
		mixers = mixers ++ mxs.collect({ 
			arg m, i;
			m.asMixerChannelGUI(this);
		});
		
			// fix window, draw controls, fix numbering and redraw
		this.sizeWindow;
		{ this.refresh; nil }.defer(0.1);
		^this
	}
	
		// remove & removeAt won't work yet
	remove { arg ... mxs;
		// remove mixer(s) from console
		// mxs may be a single mixerchannel, collection or chan, chan, chan...
		var ind, mxtemp;

		mxs.isMixerChannel.if({
			mxs = [ mxs ];	// turn into a collection
		}, {
			mxs = mxs.asFlatArray;	// else flatten
		});
		
			// extract mixerchannels from mcguis
		mxtemp = mixers.collect({ arg m; m.mixer });
		
			// get indices
		ind = mxs.collect({ arg m; 
			if(m.isKindOf(MixerChannel)) { mxtemp.indexOf(m) }
				{ m };
		});
		
		^this.removeAt(ind);
	}
	
	removeAt { arg ... ind;
		var freeIt;
		
		ind = ind.asFlatArray;
		
		freeIt = ind.last;		// last arg might be boolean
		freeIt.respondsTo(\binaryValue).if({
			ind.remove(freeIt);
		}, {
			freeIt = true;	// if not, assume yes, this should free the mc
		});
		
			// sort in descending order
			// descending b/c if we remove from the beginning,
			// later indexes will change and we'll remove the wrong channels
		
		ind = ind.sort({ arg a,b; a > b });

		ind.do({ arg i;	// loop over indexes
				// clear associated views
				// also frees mixerchannel if freeIt == true
			mixers.at(i).free(freeIt);
			
			mixers.removeAt(i);	// delete from collection
			
		});

		(mixers.size > 0).if({
			this.sizeWindow;	// resize window
			{ this.refresh; nil }.defer(0.1);	// redraw after size change takes
		}, {
			this.free		// no more mixers, clean up
		});

		^this
	
	}
	
	postSettings {	// list settings for all channels on this board
		mixers.do({ arg mm;
			mm.mixer.tryPerform(\postSettings);
		});
	}

		// GUI methods
	buildGUI {		// open a window for all channels
		arg mx;		// channels to add
		var newWindow = false;	// are we creating this new?

			// make window first		
		{ // for defer
			
			w.isNil.if({		// only build it if no window exists yet
				w = GUI.window.new(name);
				w.asView.background_(MixerChannelGUI.defaultDef.clearColor);
				w.onClose_({ this.free });	// if user closes window, clean up after yourself
				newWindow = true;
			});

			// mixer array pre-processing: collections (not just arrays) need to be flattened
			mx = mx.asFlatArray;

			mx.do({ arg mx, i;
				mixers = mixers.add(mx.asMixerChannelGUI(this));
			});

			{
				// hack: window resizing needs a little delay beforehand? ok.......
				0.25.wait;
				this.sizeWindow(newWindow);
				0.25.wait;
				this.refresh; 
				newWindow.if({ w.front });
			}.fork(AppClock);
			
			nil 
		}.defer;
	}
	
	sizeWindow {
		arg	isNew = false;		// set by buildGUI
		var	num, hSize = 0, vSize = 0, rowHeight = 0, x = 0, y = 0,
			guidef;
		var	columns = 0;

		num = mixers.size;

			// mixers may have different sizes, so I have to iterate
		mixers.do({ |mix|
			guidef = mix.tryPerform(\guidef) ?? { MixerChannelGUI.defaultSkin };
			rowHeight = max(rowHeight, guidef.channelSize.y);
			hSize = max(hSize, x + guidef.channelSize.x + skin.gap.x);
			(columns >= (skin.maxAcross-1) 
				or: { hSize + guidef.channelSize.x > skin.maxSize.x })
			.if({
				vSize = vSize + rowHeight + skin.gap.y;
				x = rowHeight = columns = 0;
			}, {
				x = x + guidef.channelSize.x + skin.gap.x;
				columns = columns + 1;
			});
		});
			// take into account height of last row
		vSize = vSize + rowHeight + skin.gap.y;

		if(w.isClosed.not) {
			{
			isNew.if({	// if new, assign origin
				w.bounds = Rect.new(5, 5, hSize + (2*skin.gap.x), vSize+20);
			}, {			// if old, keep existing origin
				w.bounds = Rect.new(w.bounds.left, w.bounds.top, hSize + (2*skin.gap.x), vSize+20);
			});
			nil }.defer;
		};
		^this
	}		
	
	*refresh {
		boards.do(_.refresh)
	}
	
	refresh {
		var	origin,	// of this mixerchannel
		sendItems,	// to build dest menu
		send,		// to fill in send menu values
		across = 0;	// to implement skin.maxAcross

		defer {
			// start here for first channel's group of controls
			origin = Point(skin.gap.x, skin.gap.y);

			// for each mixerchannel
			mixers.do({ arg m, i;
				m.origin = origin;		// reset origin
				m.refresh;		// redraw

				// move to next origin
				origin = origin + Point(m.guidef.channelSize.x + skin.gap.x);
				across = across + 1;

				// within horizontal bounds?
				// if not, wrap around
				((across >= skin.maxAcross)
					or: { origin.x + m.guidef.channelSize.x > w.bounds.width })
				.if({
					origin = Point(skin.gap.x,
						origin.y + m.guidef.channelSize.y + skin.gap.y);
					across = 0;
				});
			});
		}
	}
	

	// change appearance
	// to change one parameter, if b is the board, do
	// b.skin = b.skin.color1_(Color.rgb(....)); 
	skin_ { arg newskin;
		skin = newskin;
		this.sizeWindow;
		{ this.refresh; nil }.defer(0.1);	// redraw gui
		^this
	}
	
	setGuiDefs { |guidef ... mxs|
		mxs.isNil.if({ mxs = mixers }, { mxs = mxs.flat; });
		mxs.do({ |mixer|
			mixer.guidef_(guidef, false);	// changing en masse, don't refresh the whole board
		});
		this.sizeWindow;
		{ this.refresh; nil }.defer(0.1);	// redraw gui
	}		
}
