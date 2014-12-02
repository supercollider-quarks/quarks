OSCtree : OSCresponder {
	var <>tree;
	classvar <>default;
	
	*new { |addr, cmd, tree|
		var resp, match;
		resp = super.new(addr, cmd);
		match = all.findMatch(resp);
		if(match.notNil) { resp = match };
		^resp.tree_(tree)
	}
	
	value { |time, msg, addr|
		var match = this.findMatch(tree.value, msg[1..]);
		if(match.notNil) { match.value(time, this, msg) };
	}

	findMatch { |subtree, msg|
		subtree.pairsDo { |key, val|
			var msgKey = msg[0];
			if(msgKey === '*' or: { key.matchItem(msgKey) }) {
				^if(val.isSequenceableCollection) {
					this.findMatch(val, msg[1..])
				} {
					val
				}
			}
		};
		^nil
	}

}