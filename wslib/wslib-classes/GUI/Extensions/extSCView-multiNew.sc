+ Rect {
	*multiNew { |...args|
			args = args ? [0,0,0,0];
			args = args.flop;
			^Array.fill(args.size, { |i| Rect(*args[i]) } );
		}		
	}

