

API {

  classvar <all, <>maxMsgSize=64512;
  var <name, functions;
  var oscResponders;

  *new { arg name;
    // get or create
    ^(all.at(name.asSymbol) ?? { this.prNew(name) })
  }
  *prNew { arg name;
    ^super.new.init(name.asSymbol)
  }
  *load { arg name;
    ^all.at(name.asSymbol)
        ?? { this.prLoadAPI(name) }
        ?? { Error("API" + name.asCompileString + "not found").throw; }
  }
  init { arg n;
    name = n;
    functions = Dictionary.new;
    all.put(name, this);
  }
  *initClass {
    all = IdentityDictionary.new;
  }

  // defining
  add { arg selector, func;
    functions.put(selector, func)
  }
  addAll { arg dict;
    functions.putAll(dict)
  }
  // add methods off an object as functions
  exposeMethods { arg obj, selectors;
    selectors.do({ arg m;
      this.add(m, { arg callback ... args;
        callback.value(obj.performList(m, args));
      })
    })
  }
  exposeAllExcept { arg obj, selectors=#[];
    obj.class.methods.do({ arg meth;
      if(selectors.includes(meth.name).not, {
        this.add(meth.name, { arg callback ... args;
          callback.value( obj.performList(meth.name, args) )
        })
      })
    })
  }

  // calling
  async { arg selector, args, callback, onError;
    // passes the result to the callback
    var m;
    m = this.prFindHandler(selector);
    if(onError.notNil, {
      {
          m.valueArray([callback] ++ args);
      }.try(onError);
    }, {
      m.valueArray([callback] ++ args);
    });
  }
  sync { arg selector, args, onError;
    // pauses thread if needed and returns
    // the result directly.
    // must be inside a Routine
    var result, c = Condition.new;
    c.test = false;
    this.async(selector, args, { arg r;
      result = r;
      c.test = true;
      c.signal;
    }, onError);
    c.wait;
    ^result
  }
  *async { arg path, args, callback, onError;
    var name, selector;
    # name, selector = path.asString.split($.);
    this.load(name).async(selector, args, callback, onError);
  }
  *sync { arg path, args, onError;
    var name, selector;
    # name, selector = path.asString.split($.);
    ^this.load(name).sync(selector, args, onError);
  }


  // returns immediately
  // no async, no Routine required
  // the handler must not fork or defer
  // before calling the callback
  // "apiname.cmdName", arg1, arg2
  *call { arg path ... args;
    var name, selector;
    # name, selector = path.asString.split($.);
    ^this.load(name).call(selector, *args);
  }
  call { arg selector ... args;
    var m = this.prFindHandler(selector), result;
    m.valueArray([{ arg r; result = r; }] ++ args);
    ^result
  }

  // querying
  *apis {
    var out = API.all.keys.as(Set);
    Main.packages.do({ arg assn;
      (assn.value +/+ "apis" +/+ "*.api.scd").pathMatch.do { arg path;
        out.add(this.prNameFromPath(path).asSymbol);
      };
    });
    ^out.as(Array);
  }
  *atPath { arg path;
    var name, selector;
    # name, selector = path.asString.split($.);
    ^[this.load(name), selector]
  }
  *findHandler { arg path;
    var api, selector;
    # api, selector = this.atPath(path);
    ^api.prFindHandler(selector)
  }
  selectors {
    ^functions.keys
  }
  *allPaths {
    var out = List.new;
    all.keysValuesDo({ arg name, api;
      api.selectors.do { arg selector;
        out.add( name.asString ++ "." ++ selector.asString );
      }
    });
    ^out
  }

  // for ease of scripting
  // respond as though declared functions were native methods to this object
  doesNotUnderstand { arg selector ... args;
    if(thisThread.class === Thread, {
      ^this.call(selector, *args)
    }, {
      ^this.sync(selector, args)
    })
  }

  *loadAll { arg forceReload=true;
    if(forceReload, {
      this.initClass;
    });
    Main.packages.do({ arg assn;
      (assn.value +/+ "apis" +/+ "*.api.scd").pathMatch.do({ arg path;
        var api, name;
        name = this.prNameFromPath(path);
        if(forceReload or: {all[name].isNil}, {
          {
            api = path.load;
            API.prNew(name).addAll(api);
          }.try({ arg err;
            ("While loading" + name).error;
            err.errorString.postln;
          });
        });
      });
    });
  }
  *prNameFromPath { arg path;
    ^path.split(Platform.pathSeparator).last.split($.).first
  }
  *prLoadAPI { arg name;
    Main.packages.do({ arg assn;
      (assn.value +/+ "apis" +/+ name.asString ++ ".api.scd").pathMatch.do { arg path;
        var api;
        {
          api = path.load;
        }.try({ arg err;
          ("While loading" + name).error;
          err.errorString.error;
          ^nil
        });
        ^API(name).addAll(api);
      };
    });
    ^nil
  }
  prFindHandler { arg path;
    ^functions[path.asSymbol] ?? {
      Error(path.asString.asCompileString
        + "not found in:" + name.asCompileString).throw
    }
  }


  mountOSC { arg baseCmdName, addr;
    // simply registers each function in this API as an OSC responder node
    // baseCmdName : defaults to this.name  ie.  /{this.name}/{path}
    // addr:  default is nil meaning accept message from anywhere
    this.unmountOSC;
    functions.keysValuesDo({ arg k, f;
      var r;
      r = OSCresponderNode(addr,
            ("/" ++ (baseCmdName ? name).asString ++ "/" ++ k.asString).asSymbol,
            { arg time, resp, message, addr;
                this.call(k, *message[1..]);
            }).add;
      oscResponders = oscResponders.add(r);
    });
    ^oscResponders
  }
  unmountOSC {
    oscResponders.do(_.remove);
    oscResponders = nil;
  }

  // duplex returns results of API calls as a reply OSC message
  *mountDuplexOSC { arg srcID, recvPort;
      /*
          Calling

          /API/call : client_id, request_id, fullpath ... args
              client_id and request_id : are used to identify return messages
                  and are up to the implementation of the api consumer
                  client_id would usually be a specific web browser,
                  program or other independent entity
                  request_id would be a unique id for that request for that client

                  for messages that would exceed UDP size limits
                  request_id may prefixed with page indexes:
                    1,3:abc123
                    2,3:abc123
                    3,3:abc123
                  and the arguments may be a JSON string split into
                  packets and passed each time as the one and only argument.
                  The api is called once all parts are received.

              fullpath:  apiname.methodKey
                  dot separated to make it clear that its not an OSC path

          Replies

          /API/reply : client_id, request_id, result
          /API/not_found : client_id, request_id, fullpath
          /API/error : client_id, request_id, errorString
      */
      var multiMessageRequests = IdentityDictionary.new;
      OSCdef('API_DUPLEX', { arg msg, time, addr, recvPort;
        var client_id, request_id, path, args, api, apiName, fullpath, m, ignore;
        var multiMatch, page, totalPages, callApi;

        callApi = { arg args;
          {
            api = this.load(apiName);
            m = api.prFindHandler(path);
          }.try({ arg error;
            addr.sendMsg('/API/not_found',
              client_id,
              request_id,
              error.errorString);
            error.reportError();
          });
          if(m.notNil, {
            api.async(path, args, { arg result;
                var response = JSON.stringify((result: result));
                if(response.size <= maxMsgSize, {
                  addr.sendMsg('/API/reply',
                    client_id,
                    request_id,
                    response);
                }, {
                  Error("OSC message is too big to send:"
                        + (response.size)).throw;
                });
            }, { arg error;
                addr.sendMsg('/API/error',
                  client_id,
                  request_id,
                  error.errorString());
                error.reportError();
            });
          });
        };

        # ignore, client_id, request_id, fullpath ... args = msg;
        # apiName, path = fullpath.asString.split($.);
        path = path.asSymbol;

        // args passed as JSON strings in multi-message requests
        multiMatch = request_id.asString
          .findRegexp("^([0-9]+)\,([0-9]+):([a-zA-Z0-9\-]+)");

        if(multiMatch.size > 0, {
          page = multiMatch[1][1].asInteger;
          totalPages = multiMatch[2][1].asInteger;
          request_id = multiMatch[3][1].asSymbol;
          if(multiMessageRequests[request_id].isNil, {
            multiMessageRequests[request_id] = Array.fill(totalPages);
          });
          multiMessageRequests[request_id][page - 1] = args[0].asString;
          if(multiMessageRequests[request_id].every(_.notNil), {
            args = JSON.parse(multiMessageRequests[request_id].join);
            multiMessageRequests.removeAt(request_id);
            callApi.value(args);
          });
        }, {
          // normal call
          callApi.value(args);
        });

      }, '/API/call', srcID, recvPort);
  }
  *unmountDuplexOSC {
    OSCdef('API_DUPLEX').free;
  }

  printOn { arg stream;
    stream << this.class.asString << "('" << name << "')"
  }
}
