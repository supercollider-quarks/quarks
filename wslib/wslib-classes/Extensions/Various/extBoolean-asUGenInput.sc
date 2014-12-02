// wslib 2012
// make true/false valid inputs for UGens

+ Boolean {
	writeInputSpec { |file, synth| this.binaryValue.writeInputSpec(file, synth) }	isValidUGenInput { ^true }
}
