

(
	apis: { arg reply;
		reply.value(API.apis());
	},
	paths: { arg reply, apiName;
		if(apiName.notNil, {
			reply.value( API.load(apiName).selectors )
		},{
			API.loadAll;
			reply.value( API.allPaths )
		})
	}
);

