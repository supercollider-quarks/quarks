// Possibly by Tom Hall and Julian Rohrhuber, might originally have appeared in chapter 23 of the MITP SuperCollider Book.

VagueList : List {
	at { |index| 
		^super.at((index + 1.rand2).clip(0, this.lastIndex))
	}
}
