
SoundRepresentation : EntityRepresentation {

  var >input, >release = 0.2;
  var encoderClass, <encoderProxy, summingProxy, <encoderProxyIndex;

  *new { arg  repManager, collisionFunc, input,
              release;
    ^super.new(repManager, collisionFunc)
          .input_(input)
          .release_(release);
  }

  init {
    super.init;
    release = release ?? {0.2};

    /* decoder init */
    this.initializeDecoder;

    /* make some sound */
    this.add;

  }

  /* public */

  remove{
     Routine{
      //clear everything with given realease time
      encoderProxy.clear(release);
      //wait for the release to finish
      release.wait;
      //remove the node from the summing bus
      summingProxy.removeAt(encoderProxyIndex);
      repManager.remove(this);
      attached = false;
    }.play(TempoClock.default);
  }

  add {
      this.addSource; //Using JitLib the source will be added after the Server's default latency
      this.addAll(delay: Server.default.latency);
  }

  /* private */

  initializeDecoder{
    encoderClass = GameLoopDecoder.getEncoderClass;
    /* get the right proxy from the GameLoopDecoder class */
    encoderProxy = GameLoopDecoder.getEncoderProxy;
    encoderProxy.clock = TempoClock.default;
    /* plug the proxy to the proxy acting as summing bus */
    summingProxy = GameLoopDecoder.summingProxy;
    /* Always put the new Node in an extra slot of the Summing nodeRpoxy */
    encoderProxyIndex = summingProxy.sources.size - 1;
    summingProxy.put(encoderProxyIndex, encoderProxy);
  }

  addSource{
      encoderProxy.source = { arg dt;
        var x , y;
        var rad, azim, elev, in, speedValue;

        dt = this.dt;

        /* Ramp is used to interpolate between updates */
        #x, y = Control.names(#[x, y]).kr([position[0], position[1]]);
        x = Ramp.kr(x, dt);
        y = Ramp.kr(y, dt);

        speedValue = Control.names(\speed).kr(speed);
        speedValue = Ramp.kr(speedValue, dt);

        /* play default if input is not supplied */
        if(input == nil,
          {
            in = Impulse.ar(speedValue.linlin(0,10, 5, rrand(50, 200.0)));
            in = BPF.ar(in, rrand(2000, 18000.0)*rrand(0.3, 2.0), 0.4);
          },
          {in = input.value(speedValue)}
        );

        /* calculate azimuth and radius */
        azim = atan2(y,x);
        rad = hypot(x,y);
        elev = 0;

        /* get and use the relevant encoder */
        encoderClass.ar(
          in,
          azim,
          rad,
          elev: elev,
          ampCenter: 0.9
        );
      };
  }

  preUpdate{ arg theChanged, transPosition;
    /* set the syth with the new position values */
    encoderProxy.set('speed', speed);
    encoderProxy.set('x', transPosition[0]);
    encoderProxy.set('y', transPosition[1]);
  }

}
