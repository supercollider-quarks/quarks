+ ScaledUserView {

	doScale { |point|
		^point*this.scale*this.drawBounds.extent / this.fromBounds.extent
	}

	doReverseScale{ |point|
		^point / 	(this.drawBounds.extent / this.fromBounds.extent * this.scale )
	}
}