/* 
(
Tdef(\testShout, { 
	loop { r.shout("hey," + r.nameList.choose + "- watch out!"); rrand(2.0, 5.0).wait };
}).play;
)
*/

+ Republic { 
	shout { |str|
		str = str ? "";
		this.send(\all, '/hist', nickname, Shout.tag + str) 
	}
}