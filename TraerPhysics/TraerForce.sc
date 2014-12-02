//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

//interface class

TraerForce {
	turnOn {^this.subclassResponsibility(thisMethod)}
	turnOff {^this.subclassResponsibility(thisMethod)}
	isOn {^this.subclassResponsibility(thisMethod)}
	isOff {^this.subclassResponsibility(thisMethod)}
	apply {^this.subclassResponsibility(thisMethod)}
}
