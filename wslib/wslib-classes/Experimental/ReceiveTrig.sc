// wslib 2010
//
// the lang-side counterpart of SendTrig and SendReply
//

/*
// example 1:

x = { SendTrig.kr( Impulse.kr(1), 10, WhiteNoise.kr ) }.play;

ReceiveTrig( x, _.postln );

x.free; // automatic removal

// example 2:

r = ReceiveTrig( s, { |val, time, responder, msg| msg.postln } ); // receive any trig from s

x = { SendTrig.kr( Impulse.kr(1), 10, WhiteNoise.kr ) }.play;
y = { SendTrig.kr( Impulse.kr(1), 11, WhiteNoise.kr ) }.play;

r.id = 10; // only from x

r.remove; // stop receiving (or cmd-.)


// example 3: as a method for Node or Synth

y = { SendTrig.kr( Impulse.kr(1), 10, WhiteNoise.kr ) }.play;

y.onTrig_( _.postln ); // adds a ReceiveTrig to the Synth

y.free; // also removes the ReceiveTrig


// example 4: ReceiveReply

y = { SendReply.kr( Impulse.kr(1), 'noise', WhiteNoise.kr(1.dup) ) }.play;

y.onReply_( _.postln, 'noise' ); // adds a ReceiveReply to the Synth

y.free; // also removes the ReceiveReply


// example 5: even shorter

y = { SendTrig.kr( Impulse.kr(1), 10, WhiteNoise.kr(1) ) }.play.onTrig_( _.postln );

y.free;

*/

ReceiveReply {
	classvar <all;

	var <source;
	var <responder, <>action, <>id, <value = 0, <>removeAtReceive = false;
	var <endResponder;
	var <cmdName;

	*defaultCmdName { ^'/reply' }

	*initClass { CmdPeriod.add( this ); }

	*new { |source, action, cmdName, id|
		^super.new.init( source, cmdName )
			.id_( id )
			.action_( action ? { |value, time, responder, msg| value.postln; } )
			.addToAll
		}

	init { |inSource, inCmdName|
		source = inSource;
		cmdName = inCmdName ? this.class.defaultCmdName;
		this.startResponders;
	}

	startResponders {
		var addr;

		case { (source.class == Synth) or: { source.isNumber } } // Synth or nodeID
		{	
			addr = NetAddr( source.server.addr.hostname, source.server.addr.port );
			responder = OSCresponderNode( addr, cmdName,
				{ arg time, responder, msg;
					if( msg[1] == source.nodeID ) { this.doAction( time, responder, msg ) }
				}).add;

			 endResponder = OSCresponderNode( addr, '/n_end', // remove on end
			 	 { arg time, responder, msg;
				 	 if( msg[1] == source.nodeID ) { this.remove };
			 	 }).add;
			}
			{ source.respondsTo( \addr ) } // a Server
			{ 
				addr = NetAddr( source.addr.hostname, source.addr.port );
				responder = OSCresponderNode(addr, cmdName,
				{ arg time, responder, msg;
					this.doAction( time, responder, msg ) 				}).add;
			}
			{ source.class == NetAddr } // a NetAddr
			{ responder = OSCresponderNode(source, cmdName,
				{ arg time, responder, msg;
					this.doAction( time, responder, msg ) 				}).add;
			}
			{ true } // anything else (including nil)
			{ responder = OSCresponderNode( nil, cmdName,
				{ arg time, responder, msg;
					this.doAction( time, responder, msg )
				}).add;
			};
		}

	doAction { |time, responder, msg|
		if( id.isNil or: { msg[2] == id } )
			{ value =  msg[3..];
			if( value.size == 1 ) { value = value[0]; };
			action.value( value, time, responder, msg );
			if( removeAtReceive ) { this.remove };
		};
	}

	addToAll { all = all.asCollection ++ [ this ] }

	remove { responder.remove; endResponder.remove; all.remove( this ); }
	*remove { all.do({ |obj| obj.remove }); all = []; }

	*cmdPeriod { this.remove; }

	oneShot { removeAtReceive = true; }
	*oneShot { |source, action, cmdName, id| ^this.new( source, action, cmdName, id ).oneShot; }

	}

ReceiveTrig : ReceiveReply {

	*defaultCmdName { ^'/tr' }

	*new { |source, action, id|
		^super.new( source, action, this.defaultCmdName, id );
		}

	// more optimized; SendTrig doesn't do multichannel expansion
	doAction { |time, responder, msg|
		if( id.isNil or: { msg[2] == id } )
			{ value = msg[3];
			action.value( value, time, responder, msg );
			if( removeAtReceive ) { this.remove };
			};
	}

}

// Node support

+ Node {

	onReply_ { |action, cmdName, id|
			var rt;
			if( ( rt = this.onReply( cmdName ) ).notNil )
				{ rt.id = id; rt.action = action; }
				{ ReceiveReply( this, action, cmdName, id ); };
	}

	onReply { |cmdName|
		cmdName = cmdName ? '/reply';
		^ReceiveReply.all.detect({ |item|
			item.source == this && { item.cmdName == cmdName }; });
		}

	onTrig_ { |action, id|
			var rt;
			if( ( rt = this.onTrig ).notNil )
				{ rt.id = id; rt.action = action; }
				{ ReceiveTrig( this, action, id ); };
	}

	onTrig { ^ReceiveTrig.all.detect({ |item|
		 item.source == this && { item.cmdName == '/tr' }; });
		 }

}