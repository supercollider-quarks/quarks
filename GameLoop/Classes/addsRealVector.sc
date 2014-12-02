//extra methods for RealVector2D

+ RealVector2D {

  //mag {^this[0].sumsqr(this[1]).sqrt} //is called norm in RealVector2D

  magSq {^this[0].sumsqr(this[1])} //thus skipping the sqrt

  perp { ^RealVector2D[(-1)*this[1], this[0]]}

  distanceSq{ |vec| var ySeparation, xSeparation;
    ySeparation = vec[1] - this[1];
    xSeparation = vec[0] - this[0];
    ^((ySeparation*ySeparation) + (xSeparation*xSeparation));
  }

}

+ AbstractVector {
  normalize {
    if(this[0] == 0 and:{this[1] == 0},
      { ^this },
      { ^this / this.norm }
    );
  }
}
/*
(
  a = RealVector2D[2,3];
  b = RealVector2D[5,8];
);

bench{a.magSq.debug("magSq")}
bench{(a<|>a).debug("a<|>a")}

bench{a.distanceSq(b).debug("distanceSq")}
bench{((a-b)<|>(a-b)).debug("(this-vect)<|>(this-vect)")}
*/
