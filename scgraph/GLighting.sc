GLighting : UGen
{
	*gr
	{
		arg on = 1;
		^this.multiNew ('audio', on);
	}
}