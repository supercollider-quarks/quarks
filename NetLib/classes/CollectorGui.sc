CollectorGui {
		var <collective;
		
		*new {|collective|
			^super.newCopyArgs(collective).init
		}
		
		init {
			var w, sb, width, dec, activeBut, nameBut, allView, updater, onoff;
			sb = GUI.window.screenBounds.extent.asArray;
			
			w = GUI.window.new("collective", Rect(1, sb[1] - 300, 220, 400));
			w.front;
			
			w.view.decorator = dec = FlowLayout(w.view.bounds, 10 @ 5, 2 @ 2);
			
			activeBut = GUI.button.new(w, Rect(0, 0, 50, 30));
			activeBut.states = 
				[["away", Color.black, Color.clear], ["here", Color.red, Color.clear]];
			activeBut.action = { |b, x, val|
				
				if(onoff == 0) {
					collective.start;
					collective.autoCollect(true);
					"starting ...".postln;
					onoff = 1;
				} {	
					collective.quit; // quit doesn't work yet properly.
					onoff = 0;
				};
			};
			onoff = collective.autoCollectIsActive.binaryValue;
			
			nameBut = GUI.staticText.new(w, Rect(0, 0, 150, 30));
			nameBut.string = collective.myName;
			
			dec.nextLine;
			
			allView = GUI.listView.new(w, Rect(0, 0, 200, 300));
			allView.hiliteColor = Color.grey;
			updater = SkipJack {  
					allView.items = collective.everybody.keys.asArray.sort;
					activeBut.value = collective.autoCollectIsActive.binaryValue;
				};
			updater.start;
			w.onClose = { collective.quit; onoff = 0; updater.stop; };
		}

}