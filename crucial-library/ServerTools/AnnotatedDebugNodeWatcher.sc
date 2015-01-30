

Annotations {
	
	classvar weakRefs,annotations;
	var weakRef;
	
	*put { arg things ... notes;
		^this.prAnnotate(things,notes)
	}
	*at { arg thing;
		var noteses;
		noteses = annotations[thing.hash] ?? {^nil };
		^noteses.collect(this.unpackNotes(_))
	}

	// to store a strong reference to an object that will be used later in notes
	// that will store only a weak reference
	*register { arg object;
		weakRefs[object.hash] = object;
	}
	// remove strong reference, replace by description
	*unregister { arg object;
		weakRefs.put(object.hash,object.asString + "[removed]")
	}

	*find { arg ... clauses;
		var found = [];
		annotations.keysValuesDo { arg hash,noteses;
			var thing;
			thing = weakRefs[hash];
			if(thing.notNil,{
				if(clauses.every(_.value(thing)),{
					noteses.do { arg notes;
						found = found.add( this.unpackNotes(notes) )
					}
				})
			})
		};
		^found
	}
	*findBus { arg index,rate;
		^this.find({arg b;b.isKindOf(Bus)},{arg b;b.index == index},{arg b; b.rate == rate})
	}
	*findNode { arg nodeID;
		^this.find({arg n;n.isKindOf(Node)},{ arg n; n.nodeID == nodeID })
	}
	*guiFindNode { arg nodeID,layout;
		this.findNode(nodeID).do { arg notes;
			notes.do({ arg n;
				if(n.isString,{
					n.gui(layout);
				},{
					InspButton(n,layout)
				})
			});
			layout.startRow;
		}
	}
	*guiFindBus { arg index,rate,layout;
		this.findBus(index,rate).do { arg notes;
			notes.do({ arg n;
				if(n.isString,{
					n.gui(layout);
				},{
					InspButton(n,layout)
				})
			});
			layout.startRow;
		}
	}
		
	*prAnnotate { arg thing, notes;
		var hash;
		notes = this.packNotes(notes ? []);
		hash = thing.hash;
		weakRefs[hash] = thing;
		annotations[hash] = annotations[hash].add( notes );
	}
	// any registered objects will be stored as weak references
	*packNotes { arg notes;
		^notes.collect({ arg n; weakRefs.at(n.hash) ? n })
	}		
	*unpackNotes { arg notes;
		^notes.collect(_.dereference)
	}
	*initClass {
		weakRefs = Dictionary.new;
		annotations = Dictionary.new;
	}
	*new { arg weakRef;
		^super.newCopyArgs(weakRef)
	}
	dereference {
		^weakRefs[weakRef]
	}
}				


AnnotatedDebugNodeWatcher : DebugNodeWatcher {

	getAnnotation { arg server, nodeID;
		var a;
		a = Annotations.findNode(nodeID);
		if(a.notNil,{ ^"("++a++")" },{ ^"" });
	}

	doPost { arg action, nodeID, groupID, prevID, nextID;
		Post << (server.name.asString + ":" +
		action + nodeID + this.getAnnotation(server, nodeID) + Char.nl
		+ "         " + "group:" + groupID + this.getAnnotation(server,groupID)
		/*+ "[" + prevID + this.getAnnotation(server,prevID) + "<->" + nextID + this.getAnnotation(server,nextID) + "]"*/
		) << Char.nl
	}

	n_go { arg nodeID, groupID, prevID, nextID;
		this.doPost("GO ", nodeID, groupID, prevID, nextID);
		nodes.add(nodeID);
	}

	n_end { arg nodeID, groupID, prevID, nextID;
		nodes.remove(nodeID);
		this.doPost("END", nodeID, groupID, prevID, nextID);
	}

	n_off { arg nodeID, groupID, prevID, nextID;
		this.doPost("OFF", nodeID, groupID, prevID, nextID);
	}

	n_on { arg nodeID, groupID, prevID, nextID;
		this.doPost("ON ", nodeID, prevID, nextID);	}
}

