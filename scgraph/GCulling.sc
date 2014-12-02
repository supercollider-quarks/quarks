GCulling : UGen
{
	*gr
	{
		arg mode = 1; // 0 = off, 1 = front, 2 = back, 3 = front_and_back
		^this.multiNew ('audio', mode);
	}
}