
+ Spec {

  asJSON {
    ^(
      rate: this.rate,
      class: this.class.name
    )
  }
}


+ ControlSpec {

  asJSON {
    ^(
        rate: this.rate,
        class: this.class.name,
        minval: this.minval,
        maxval: this.maxval,
        warp: this.warp.asSpecifier,
        step: this.step,
        default: this.default,
        units: this.units
      )
  }
}
