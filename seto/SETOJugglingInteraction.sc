SETOJugglingInteraction : SETOIDistance {

	/**
		action evaluated if a head and a club are involved in the action.
		@example {|distance, isValid, head, club| distance.postln;}
	*/
	classvar <>headClubAction;
	/**
		action evaluated if only clubs are involved in the action.
		@example {|distance, isValid, clubs| distance.postln;}
	*/
	classvar <>clubsAction;
	
	interaction {|isValid|
		var clubs, club, head, distance, posA, posB;
		var m1, m2t, out;
		
		// get heads
		head = parts.select{|item|item.classID == 1}.first;
		// get clubs
		clubs   = parts.select{|item|item.classID == 0};
		
		// compute distance
		# posA, posB = parts.collect{|p| p.pos[0..2].reject(_.isNil)};		distance = posA.collect{|val, i| (val - posB[i]).squared}.sum.sqrt;
		
		head.isNil.if({
		// interaction between clubs
			
			clubsAction.value(distance, isValid, clubs);
		},{
		// interaction between club and head
			
			club = clubs.first;
			// club.posRelHead = club.pos - head.pos;
			
			// compute club's origin with respect to the head's state 
			club.posRelHead = head.transformPoint(club.pos);
			club.posRelGroundPoint = head.transformPoint2Ground(club.pos);
			
			headClubAction.value(distance, isValid, head, club);
		})
	}
}
