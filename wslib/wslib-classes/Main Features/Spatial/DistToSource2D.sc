DistToSource2D {
	
	*new { |sourceX = 0, sourceY = 0, micX = 0, micY = 0|
		^Point( sourceX, sourceY ).dist( Point( micX, micY ) );
		}

	*stereo { |sourceX = 0, sourceY = 0, width = 0.3|
		^this.new( sourceX, sourceY, [ width.neg, width ] / 2, 0 );
		}

	*quad { |sourceX = 0, sourceY = 0, width = 0.3|
			//  1  2
			//  4  3
			// positive y = front
		^this.new( sourceX, sourceY, 
			[ width.neg, width, width, width.neg ] / 2,
			[ width, width, width.neg, width.neg ] / 2
			);
		}
	
	}