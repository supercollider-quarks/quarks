/* ================== */
/* = Spectral Space = */
/* ================== */

SpecSpace : UGen{

    // a UGen for intuitive contol of spectral space. The 'space' of the sound is defined by
    // low and high limit and the movement happens in this space giving avalue from -1 to 1
    // as well as a bandwidth in octaves.

  *ar{ arg in, lowLmt, highLmt, center, width;  // the width is in octaves
     var freq;
    freq = center.linlin(-1, 1, lowLmt, highLmt); //make the -1 to 1 into the defined spectral teritorry
    freq = freq.clip(lowLmt, highLmt); //clip just in case
    ^BBandPass.ar(in, freq.clip(20,20000), width);
  }

}



