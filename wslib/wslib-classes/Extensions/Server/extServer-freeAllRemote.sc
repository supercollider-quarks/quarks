+ Server {
	*freeAllRemote  { |includeLocal = true|
		if( includeLocal )
			{ Server.set.do( _.freeAll ) }
			{ Server.set.do({ |server|
				if( server.isLocal.not )
					{ server.freeAll; }
				})
			};
		}
	}