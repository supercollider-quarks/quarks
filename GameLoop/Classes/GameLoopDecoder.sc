
GameLoopDecoder {

    classvar <active = false;
    classvar <decoderProxy, <summingProxy, <encoderClass,
    encoderChannels, decoderChannels, order, kernel;

  *new{
        "You can not have an instance of GameLoopDecoder".error;
  }

  /* Public Methods */

  *getEncoderProxy{
    ^NodeProxy(Server.default, 'audio', encoderChannels);
  }

  *getEncoderClass{
    ^encoderClass;
  }

  *clear{
    Routine{
      decoderProxy.source = nil;
      1.wait;
      kernel.free;
      active = false;
    }.play;
  }

  /* Private Methods */

  *calculateEncoderChannelsForOrder{ arg order = 1;
    switch (order)
    {1}{ encoderChannels = 4 }
    {2}{ encoderChannels = 9 }
    {3}{ encoderChannels = 16};
  }

  *addDecoderSource{ arg decoderClass, kernel;
      Routine{
        1.wait;
        decoderProxy.source = {
          var in, out;
          in = \in.ar(0!encoderChannels);
          if (decoderClass == BinAmbi3O,
            { out = decoderClass.ar(in); },
            { out = decoderClass.ar(in, kernel); }
          );
          Out.ar(0, out);
        };
        this.readyMsg;
      }.play;
  }

  *createDecoderProxy{ arg numChannels = 2;
      decoderProxy = NodeProxy(Server.default, 'audio', numChannels).fadeTime_(0.5)
  }

  *binauralAmbIEM{ arg doppler = true;
    if(active == false,
    {
      active = true;
      this.createDecoderProxy;
      this.calculateEncoderChannelsForOrder(3);
      this.setEncoderClassForBinauralAmbIEM(doppler);
      BinAmbi3O.init('1_4_7_4');
      this.addDecoderSource(BinAmbi3O);
      this.createSumBus;
    },
    {"You have to clear the current decoder first (GameLoopDecoder.clear)".error;}
    );
  }

  *newStereo{ arg doppler = true;
    if(active == false,
    {
      active = true;
      this.createDecoderProxy;
      this.calculateEncoderChannelsForOrder(1);
      this.setEncoderClassForATK(doppler);
      kernel = FoaDecoderMatrix.newStereo(131/2 * pi/180, 0.5); // Cardioids at 131 deg
      this.addDecoderSource(FoaDecode, kernel);
      this.createSumBus;
    },
    {"You have to clear the current decoder first (GameLoopDecoder.clear)".error;}
    );
  }

  *newListen{ arg doppler = true;
    if(active == false,
    {
      active = true;
      this.createDecoderProxy;
      this.calculateEncoderChannelsForOrder(1);
      this.setEncoderClassForATK(doppler);
      //check here for auto choosing correct decoder: chttp://www.ambisonictoolkit.net/Help/Guides/Intro-to-the-ATK.html
      kernel = FoaDecoderKernel.newListen(1013);
      this.addDecoderSource(FoaDecode, kernel);
      this.createSumBus;
    },
    {"You have to clear the current decoder first (GameLoopDecoder.clear)".error;}
    );
  }

  *setEncoderClassForBinauralAmbIEM{ arg doppler;
      if (doppler == true,
        {encoderClass = SpacePolarAmbIEMDp},
        {encoderClass = SpacePolarAmbIEM}
      );
  }

  *setEncoderClassForATK{ arg doppler;
      if (doppler == true,
        {encoderClass = SpacePolarATKDp},
        {encoderClass = SpacePolarATK}
      );
  }

  *createSumBus{
    // create the summing NodeProxy that will act as the summation bus
    // see http://new-supercollider-mailing-lists-forums-use-these.2681727.n2.nabble.com/Many-to-One-Audio-Routing-in-Jitlib-td7594874.html
    summingProxy = NodeProxy(Server.default, 'audio', encoderChannels);
    //route the summation bus to the decoder
    summingProxy <>> decoderProxy;
  }

  *readyMsg{
    "A decoder was created through GameLoopDecoder".postln;
  }

}
