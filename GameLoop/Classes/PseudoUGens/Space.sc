

SpacePolarAmbIEM : UGen{

  *ar {
    arg in, azimuth, radius,  elev = 0, ampCenter = 1;
    var amp;
    // ** VSpace ** style
    amp = (1/radius.squared).clip(0, 1);
    //here we scale and filter like in ** VSpace **
    in = amp*MoogVCF.ar(in*ampCenter, (100000/radius.clip(0.01, inf)).clip(20, (SampleRate.ir*0.5) - 100), 0);
    //encode the signal
    ^PanAmbi3O.ar(in, 0.5pi - azimuth, DC.kr(elev));
  }

}

SpacePolarAmbIEMDp : UGen {

  *ar {
    arg in, azimuth, radius, elev = 0, ampCenter = 1;
    var amp;
    // ** VSpace ** style
    amp = (1/radius.squared).clip(0, 1);
    //here we scale and filter like in ** VSpace **
    in = DelayL.ar(in, 1, radius/343); //for doppler
    in = amp*MoogVCF.ar(in*ampCenter, (100000/radius.clip(0.01, inf)).clip(20, (SampleRate.ir*0.5) - 100), 0);
    //encode the signal
    ^PanAmbi3O.ar(in, 0.5pi - azimuth, DC.kr(elev)) 
  }

}

SpacePolarATK : UGen{

  *ar {
    arg in, azimuth, radius,  elev = 0, ampCenter = 1;
    var amp, foa;
    // ** VSpace ** style
    amp = (1/radius.squared).clip(0, 1);
    //here we scale and filter like in ** VSpace **
    in = amp*MoogVCF.ar(in*ampCenter, (100000/radius.clip(0.01, inf)).clip(20, (SampleRate.ir*0.5) - 100), 0);
    //encode the signal
    in = HPF.ar(in, 20.0);    // precondition signal for proximity
    // Encode into our foa converting signal azimuth to pi to -pi for ATK
    foa = FoaPanB.ar(in, (azimuth + 0.5pi) * -1 , elev);
    //foa = FoaRotate.ar(foa, azimuth);
    ^FoaProximity.ar(foa, radius.clip(0.0001, 30.0));
  }

}

SpacePolarATKDp : UGen {

  *ar {
    arg in, azimuth, radius, elev = 0, ampCenter = 1;
    var amp, foa;
    // ** VSpace ** style
    amp = (1/radius.squared).clip(0, 1);
    //here we scale and filter like in ** VSpace **
    in = DelayL.ar(in, 1, radius/343); //for doppler
    in = amp*MoogVCF.ar(in*ampCenter, (100000/radius.clip(0.01, inf)).clip(20, (SampleRate.ir*0.5) - 100), 0);
    //encode the signal
    in = HPF.ar(in, 20.0);    // precondition signal for proximity
    // Encode into our foa converting signal azimuth to pi to -pi for ATK
    foa = FoaPanB.ar(in, (azimuth + 0.5pi) * -1 , elev);
    //foa = FoaRotate.ar(foa, azimuth);
    ^FoaProximity.ar(foa, radius.clip(0.0001, 30.0));
  }

}

SpacePolarB2 : UGen{

  *ar {
    arg in, azimuth, radius,  ampCenter, speakerRho = 2;
    var amp;
    // ** VSpace ** style
    amp = (1/radius.squared).clip(0, 1);
    //here we scale and filter like in ** VSpace **
    in = amp*MoogVCF.ar(in*ampCenter, (100000/radius.clip(0.01, inf)).clip(20, (SampleRate.ir*0.5) - 100), 0);
    //encode the signal (the radius code is used in order to take advantage of the rho argument of BFEncode1)
    ^FMHEncode1.ar(in,  azimuth - 0.5pi, 0, radius.clip(0, speakerRho).linlin(0, speakerRho, 0, 1)); //
  }
}

SpacePolarB2Dp : UGen {

  *ar {
    arg in, azimuth, radius,  ampCenter, speakerRho = 2;
    var amp;
    // ** VSpace ** style
    amp = (1/radius.squared).clip(0, 1);
    //here we scale and filter like in ** VSpace **
    in = DelayL.ar(in, 1, radius/343); //for doppler
    in = amp*MoogVCF.ar(in*ampCenter, (100000/radius.clip(0.01, inf)).clip(20, (SampleRate.ir*0.5) - 100), 0);
    //encode the signal (the radius code is used in order to take advantage of the rho argument of BFEncode1)
    ^FMHEncode1.ar(in,  azimuth - 0.5pi, 0, radius.clip(0, speakerRho).linlin(0, speakerRho, 0, 1)); //
  }

}
