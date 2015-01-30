(
  apis: { arg reply;
    reply.value(API.apis());
  },
  paths: { arg reply, apiName;
    if(apiName.notNil, {
      reply.value(API.load(apiName).selectors.as(Array));
    }, {
      API.loadAll;
      reply.value(API.allPaths);
    })
  },
  info: { arg reply, path;
    var api, selector, handler, response;
    # api, selector = API.atPath(path);
    handler = API.findHandler(path);
    response = ();
    response['api'] = api.name;
    response['name'] = selector;
    // should use an object that accesses the handler
    response['argNames'] = handler.def.argNames.copyToEnd(1);
    reply.value(response);
  }
);
