//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

//interface class

TraerIntegrator {
	step {|t| ^this.subclassResponsibility(thisMethod)}
}
