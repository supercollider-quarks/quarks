GBlending : UGen
{
	*symbolToInt
	{
		arg symbol;

		^switch (symbol,
			\blendZero,             {0.0},
			\blendOne,              {1.0},
			\blendDstColor,         {2.0},
			\blendSrcColor,         {3.0},
			\blendOneMinusDstColor, {4.0},
			\blendOneMinusSrcColor, {5.0},
			\blendSrcAlpha,         {6.0},
			\blendOneMinusSrcAlpha, {7.0},
			\blendDstAlpha,         {8.0},
			\blendOneMinusDstAlpha, {9.0},
			\blendSrcAlphaSaturate, {10.0}
		);
	}
	*gr
	{
		arg
			on = 1,
			srcBlendFunction = \blendOne,
			dstBlendFunction = \blendZero;

		var srcbf, dstbf;

		srcbf = this.symbolToInt(srcBlendFunction);
		dstbf = this.symbolToInt(dstBlendFunction);

		^this.multiNew ('audio', on, srcbf, dstbf);
	}
}