// nameOfUnaryOpSpecialIndex.sc - (c) rohan drape, 2004-2007

// Return the <Symbol> naming the unary operator given by an <Integer>
// specialIndex value.

+ Integer {

  nameOfUnaryOpSpecialIndex {
  ^[
	'-',
	'!',
	\IsNil,
	\NotNil,
	\BitNot,
	\Abs,
	\AsFloat,
	\AsInt,
	\Ceil,
	\Floor,
	\Frac,
	\Sign,
	\Squared,
	\Cubed,
	\Sqrt,
	\Exp,
	\Recip,
	\MIDICPS,
	\CPSMIDI,

	\MIDIRatio,
	\RatioMIDI,
	\DbAmp,
	\AmpDb,
	\OctCPS,
	\CPSOct,
	\Log,
	\Log2,
	\Log10,
	\Sin,
	\Cos,
	\Tan,
	\ArcSin,
	\ArcCos,
	\ArcTan,
	\SinH,
	\CosH,
	\TanH,
	\Rand,
	\Rand2,
	\LinRand,
	\BiLinRand,

	\Sum3Rand,

	\Distort,
	\SoftClip,
	\Coin,

	\DigitValue,
	\Silence,
	\Thru,
	\RectWindow,
	\HanWindow,
	\WelchWindow,
	\TriWindow,

	\Ramp,
	\SCurve].at(this);
	}
}
