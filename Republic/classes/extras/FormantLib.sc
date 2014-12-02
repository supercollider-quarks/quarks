FormantLib { 
	classvar lib;
	*lib { 
		if (lib.isNil, { this.initLib; });
		^lib;
	}
	*at { arg ... names; ^this.lib.at(*names); }
	
	*new { ^this.shouldNotImplement }
	
	*initLib {
			
		lib = Library.new;
		
		lib	.put( 'soprano', 'a', 'freq',[ 800, 1150, 2900, 3900, 4950 ])
			.put( 'soprano', 'a', 'amp',	[ 0, -6, -32, -20, -50 ])
			.put( 'soprano', 'a', 'bw',	[ 80, 90, 120, 130, 140 ])
		
			.put( 'soprano', 'e', 'freq',[ 350, 2000, 2800, 3600, 4950 ])
			.put( 'soprano', 'e', 'amp',	[ 0, -20, -15, -40, -56 ])
			.put( 'soprano', 'e', 'bw',	[ 60, 100, 120, 150, 200 ])
		
			.put( 'soprano', 'i', 'freq',[270, 2140, 2950, 3900, 4950])
			.put( 'soprano', 'i', 'amp',	[0, -12, -26, -26, -44])
			.put( 'soprano', 'i', 'bw',	[60, 90, 100, 120, 120])
		
			.put( 'soprano', 'o', 'freq',[450, 800, 2830, 3800, 4950])
			.put( 'soprano', 'o', 'amp',	[0, -11, -22, -22, -50])
			.put( 'soprano', 'o', 'bw',	[70, 80, 100, 130, 135])
		
			.put( 'soprano', 'u', 'freq',[325, 700, 2700, 3800, 4950])
			.put( 'soprano', 'u', 'amp',	[0, -16, -35, -40, -60])
			.put( 'soprano', 'u', 'bw',	[50, 60, 170, 180, 200]);
		
		
		lib.put( 'alto', 'a', 'freq',	[800, 1150, 2800, 3500, 4950])
			.put( 'alto', 'a', 'amp',	[0, -4, -20, -36, -60])
			.put( 'alto', 'a', 'bw',	[80, 90, 120, 130, 140])
		
			.put( 'alto', 'e', 'freq',	[400, 1600, 2700, 3300, 4950])
			.put( 'alto', 'e', 'amp',	[0, -24, -30, -35, -60])
			.put( 'alto', 'e', 'bw',	[60, 80, 120, 150, 200])
		
			.put( 'alto', 'i', 'freq',	[350, 1700, 2700, 3700, 4950])
			.put( 'alto', 'i', 'amp',	[0, -20, -30, -36, -60])
			.put( 'alto', 'i', 'bw',	[50, 100, 120, 150, 200])
		
			.put( 'alto', 'o', 'freq',	[450, 800, 2830, 3500, 4950])
			.put( 'alto', 'o', 'amp',	[0, -9, -16, -28, -55])
			.put( 'alto', 'o', 'bw',	[70, 80, 100, 130, 135])
		
			.put( 'alto', 'u', 'freq',	[325, 700, 2530, 3500, 4950])
			.put( 'alto', 'u', 'amp',	[0, -12, -30, -40, -64])
			.put( 'alto', 'u', 'bw',	[50, 60, 170, 180, 200]);
		
		
		lib.put( 'tenor', 'a', 'freq',	[650, 1080, 2650, 2900, 3250])
			.put( 'tenor', 'a', 'amp',	[0, -6, -7, -8, -22])
			.put( 'tenor', 'a', 'bw',	[80, 90, 120, 130, 140])
		
			.put( 'tenor', 'e', 'freq',	[400, 1700, 2600, 3200, 3580])
			.put( 'tenor', 'e', 'amp',	[0, -14, -12, -14, -20])
			.put( 'tenor', 'e', 'bw',	[70, 80, 100, 120, 120])
		
			.put( 'tenor', 'i', 'freq',	[290, 1870, 2800, 3250, 3540])
			.put( 'tenor', 'i', 'amp',	[0, -15, -18, -20, -30])
			.put( 'tenor', 'i', 'bw',	[40, 90, 100, 120, 120])
		
			.put( 'tenor', 'o', 'freq',	[400, 800, 2600, 2800, 3000])
			.put( 'tenor', 'o', 'amp',	[0, -10, -12, -12, -26])
			.put( 'tenor', 'o', 'bw',	[40, 80, 100, 120, 120])
		
			.put( 'tenor', 'u', 'freq',	[350, 600, 2700, 2900, 3300])
			.put( 'tenor', 'u', 'amp',	[0, -20, -17, -14, -26])
			.put( 'tenor', 'u', 'bw',	[40, 60, 100, 120, 120]);
			
		
		lib.put( 'bass', 'a', 'freq',	[600, 1040, 2250, 2450, 2750])
			.put( 'bass', 'a', 'amp',	[0, -7, -9, -9, -20])
			.put( 'bass', 'a', 'bw',	[60, 70, 110, 120, 130])
		
			.put( 'bass', 'e', 'freq',	[400, 1620, 2400, 2800, 3100])
			.put( 'bass', 'e', 'amp',	[0, -12, -9, -12, -18])
			.put( 'bass', 'e', 'bw',	[40, 80, 100, 120, 120])
		
			.put( 'bass', 'i', 'freq',	[250, 1750, 2600, 3050, 3340])
			.put( 'bass', 'i', 'amp',	[0, -30, -16, -22, -28])
			.put( 'bass', 'i', 'bw',	[60, 90, 100, 120, 120])
		
			.put( 'bass', 'o', 'freq',	[400, 750, 2400, 2600, 2900])
			.put( 'bass', 'o', 'amp',	[0, -11, -21, -20, -40])
			.put( 'bass', 'o', 'bw',	[40, 80, 100, 120, 120])
		
			.put( 'bass', 'u', 'freq',	[350, 600, 2400, 2675, 2950])
			.put( 'bass', 'u', 'amp',	[0, -20, -32, -28, -36])
			.put( 'bass', 'u', 'bw',	[40, 80, 100, 120, 120]);
		
		
		lib.put( 'counterTenor', 'a', 'freq',		[660, 1120, 2750, 3000, 3350])
			.put( 'counterTenor', 'a', 'amp',	[0, -6, -23, -24, -38])
			.put( 'counterTenor', 'a', 'bw',		[80, 90, 120, 130, 140])
		
			.put( 'counterTenor', 'e', 'freq',	[440, 1800, 2700, 3000, 3300])
			.put( 'counterTenor', 'e', 'amp',	[0, -14, -18, -20, -20])
			.put( 'counterTenor', 'e', 'bw',		[70, 80, 100, 120, 120])
		
			.put( 'counterTenor', 'i', 'freq',	[270, 1850, 2900, 3350, 3590])
			.put( 'counterTenor', 'i', 'amp',	[0, -24, -24, -36, -36])
			.put( 'counterTenor', 'i', 'bw',		[40, 90, 100, 120, 120])
		
			.put( 'counterTenor', 'o', 'freq',	[430, 820, 2700, 3000, 3300])
			.put( 'counterTenor', 'o', 'amp',	[0, -10, -26, -22, -34])
			.put( 'counterTenor', 'o', 'bw',		[40, 80, 100, 120, 120])
		
			.put( 'counterTenor', 'u', 'freq',	[370, 630, 2750, 3000, 3400])
			.put( 'counterTenor', 'u', 'amp',	[0, -20, -23, -30, -34])
			.put( 'counterTenor', 'u', 'bw',		[40, 60, 100, 120, 120]);
	}
}
