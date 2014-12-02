StatUGens are a set of PseudoUGens which provide statistical analysis on the server of data on a control bus.

They are based on the MathLibs statistics extensions, but written using UGens instead:

VarianceUGen - variance
StdDevUGen   - standard deviation
SkewUGen     - skew
KurtosisUGen - kurtosis

// additional measures:
FluctuationUGen - fluctuation between levels


// --- Experimental status ---
DynamicScaleUGen - dynamic scaling
MaxDynScaleUGen  - multi dynamic scaling according to largest value
MinDynScaleUGen  - multi dynamic scaling according to smallest value