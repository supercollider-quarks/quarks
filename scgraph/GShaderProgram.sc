GShaderProgram : UGen
{
	*gr
	{
		// Specify the index of a shaderprogram to use. Specify on = 0 to
		// have no shader program used regardless of the index.
		arg index = 0, on = 1;
		^this.multiNew ('audio', index, on);
	}
}

GShaderUniform : UGen
{
	*gr
	{
		// Specify the index of a shaderprogram to use. The number of values in the 
		// values array must match the referenced attribute. Max size is 4.
		arg index = 0, values = [0];
		^this.multiNewList (['audio'] ++ index ++ values.size ++ values.flat);
	}
}
