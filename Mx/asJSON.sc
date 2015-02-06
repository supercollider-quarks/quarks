

+ MxApp {

  asJSON {
    ^(
        units: this.units.collect(_.asJSON),
        inlets: this.inlets.collect(_.asJSON),
        outlets: this.outlets.collect(_.asJSON)
      )
  }
}


+ MxUnitApp {

  asJSON {
    ^(
        name: this.name,
        id: this.id,
        point: this.point.asArray,
        spec: this.spec.asJSON,
        inlets: this.inlets.collect(_.asJSON),
        outlets: this.outlets.collect(_.asJSON)
      )
  }
}


+ MxInletApp {

  asJSON {
    ^(
        name: this.name,
        index: this.index,
        spec: this.spec.asJSON
      )
  }
}


+ MxOutletApp {

  asJSON {
    ^(
        name: this.name,
        index: this.index,
        spec: this.spec.asJSON
      )
  }
}
