+ Color {
	*newHex { arg hexName = "000000", alpha = 255;
		// convert web colors (removes '#' sign automatically)
		// hexName can be number, symbol or string
		// colors are 24 bits (each channel 00 - FF)
		var r, g, b, number;
		if(hexName.isNumber)
			{ number = hexName; }
			{ 	hexName = hexName.asString.removeItems("#");
				number = hexName.asHexIfPossible;  };
		if(alpha.isNumber.not)
			{ alpha = ("16x" ++ alpha).interpret;  }; 
		if(number.isNumber)
			{	b = number % 256;
				g = ((number / 256) % 256).floor;
				r = ((number / 65536) % 256).floor; 
				^Color.new255(r,g,b, alpha) }
			{ ^nil }
		}
		
	hex { ^(
		(red * 255).asInt.asHexString(2) ++ 
		(green * 255).asInt.asHexString(2) ++ 
		(blue  * 255).asInt.asHexString(2)); }
	
	webHex { ^"#" ++ this.hex; }
	
	asWebColorString { 
		var webColor;
		if( alpha == 0 ) { ^"none" };
		webColor = this.roundHex.findWebColor;
		if( webColor.size == 0 )
			{ ^this.webHex; }
			{ ^webColor[0].asString; } 
		}
		
	hexValue { ^("16x" ++ (
		(red * 255).asInt.asHexString(2) ++ 
		(green * 255).asInt.asHexString(2) ++ 
		(blue  * 255).asInt.asHexString(2))).interpret; }
	
	*newName { arg name, table, includeXWindows = false;
		// test all possible inputs..
		var out;
		table = table ?? { Color.web }; // only web colors
		if(includeXWindows) { table.putAll(Color.xwindows(includeGrayShades: true)) };
		if(name.isNumber)
			{^Color.newHex(name)}
			{out = 
				((table.at(name.asSymbol) ?
				table.at(name.asString.capsToSpaces("_").asSymbol)) ?
				table.at(name.asString.replaceSpaces("_").asSymbol)) ??
				{ { Color.newHex(name) }.try };
			^out;
			}
		}
		
	roundHex { ^Color.newHex( this.hex ) }
	
	findWebColor { arg table;
		var array = [];
		table = table ?? { Color.web };
		table.keysValuesDo({ |key, value|
			if( value == this )
				{ array = array.add(key) };
			});
		^array.sort;
		}
	
	*webNameIndex { arg name = 'white', table;
		table = table ?? { Color.web };
		^table.keys.asArray.sort.indexOf(name.asSymbol);
		}
		
	*sortedWebColors { arg index, table;
		var sortedTable = [];
		table = table ?? { Color.web };
		table.sortedKeysValuesDo({
			|key, value|
			sortedTable = sortedTable.add(value); });
 		if(index.notNil)
			{ ^sortedTable.wrapAt(index); }
			{ ^sortedTable }
		}
	
	differenceIndex { |aColor, type| // not very good yet..
		var index = [];
		if(type == 'hsv')
			{index = (this.asHSV.max(0).min(1) - aColor.asHSV.max(0).min(1)).abs;
			}
			{ index = [	(red - aColor.red).abs,
				(green - aColor.green).abs,
				(blue - aColor.blue).abs ]; };
			
		^index.sum;
		}
	
	sortWebNames { arg method, webColors;
		webColors = webColors ?? { Color.web };
		^webColors.keys.asArray.sort({ |a,b|
			webColors.at(a).differenceIndex(this, method)			<= webColors.at(b).differenceIndex(this, method) });
		}
		
	closestWebColor { arg table, method; ^Color.web.at(this.sortWebNames(method, table)[0]); }
	closestWebName { arg table, method; ^this.sortWebNames(method, table)[0]; }
	
	hue { ^this.asHSV[0].max(0).min(1); }
	sat { ^this.asHSV[1].max(0).min(1); }
	val { ^this.asHSV[2].max(0).min(1); }
	
	hue_ { arg newHue; var tempColor, hsv;
		hsv = this.asHSV.max(0).min(1);
		tempColor = Color.hsv(newHue, hsv[1], hsv[2]);
		red = tempColor.red;
		green = tempColor.green;
		blue = tempColor.blue;
		^this; }
		
	sat_ { arg newSat; var tempColor, hsv;
		hsv = this.asHSV.max(0).min(1);
		tempColor = Color.hsv(hsv[0], newSat, hsv[2]);
		red = tempColor.red;
		green = tempColor.green;
		blue = tempColor.blue;
		^this; }
		
	val_ { arg newVal; var tempColor, hsv;
		hsv = this.asHSV.max(0).min(1);
		tempColor = Color.hsv(hsv[0], hsv[1], newVal);
		red = tempColor.red;
		green = tempColor.green;
		blue = tempColor.blue;
		^this; }
	
	lightness { ^[red, green, blue].mean; }
	
	findSameHueNames { |table|
		var array = [];
		(table ?? { Color.web }).keysValuesDo({ |key, value|
			if( value.hue == this.hue )
				{ array = array.add(key) };
			});
		^array.sort;
		}
		
	desaturate { // destructive!!
		var mean = [red, green, blue].mean;
		red = green = blue = mean;
		^this;
		}
		
	clip { |min=0, max=1| // destructive
		red = red.max(min).min(max);
		green = green.max(min).min(max);
		blue = blue.max(min).min(max);
		^this;
		}
		
	// -> remove for 3.3
	/*
	round { |quant| // destructive
		quant = quant ? 1/255;
		red = red.round(quant);
		green = green.round(quant);
		blue = blue.round(quant);
		^this;
		}
	*/
	
	saturate { arg amount = 0; ^this.blend( this.copy.desaturate, amount.neg ).clip; }
	
	hueAdd { arg amount = 0; ^this.copy.hue_((this.hue + amount) % 1).clip }
	
	blendVal { arg amount = 0, toVal = 0.5; 
		^this.copy.val_(this.val.blend( toVal, amount).clip(0,1)).clip 
		}
	
	contrast { arg amount = 0;
		^this.blend( Color.new255(127.5,127.5,127.5,alpha*255), amount.neg ).clip;
		}
	
	brightness { arg amount = 0;
		^Color(red + amount, green + amount, blue + amount, alpha).clip;
		}
		
	*web16 { arg name, alpha = 1.0;
		// 16 most common web colors
		// supported by the W3C HTML 4.0 standard
		//
		// use like this:
		//	Color.web.olive
		// this:
		//  Color.web['olive']
		// or this:
		//	Color.web('olive')

		var table;
		table = (
			yellow:	Color.new255(255,255,0),
			fuchsia:	Color.new255(255,0,255),
			red:		Color.new255(255,0,0),
			aqua:	Color.new255(0,255,255),
			lime:	Color.new255(0,255,0),
			blue:	Color.new255(0,0,255),
			
			olive:	Color.new255(128,128,0),
			purple:	Color.new255(128,0,128),
			maroon:	Color.new255(128,0,0),
			teal:	Color.new255(0,128,128),
			green:	Color.new255(0,128,0),
			navy:	Color.new255(0,0,128),
			
			white:	Color.new255(255,255,255),
			black:	Color.new255(0,0,0),
			gray:	Color.new255(128,128,128),
			silver:	Color.new255(192,192,192)
			);
			
		if(name.notNil)
			{ ^table.at(name.asSymbol).alpha_(alpha); }
			{ ^table }
		}
		
	*web	{ arg name, alpha = 1.0;
		// 151 named web colors
		// source: http://www.w3schools.com/html/html_colornames.asp
		// cleaned and completed:
		// * gray/grey interchangeable
		// * added "light-goldenrod"
		// * goldenrod is always one word
		// * "navy_blue" added (equal to "navy")
		var table;
		table = (			
			alice_blue: Color.newHex('F0F8FF'),
			antique_white: Color.newHex('FAEBD7'),
			aqua: Color.newHex('00FFFF'),
			aquamarine: Color.newHex('7FFFD4'),
			azure: Color.newHex('F0FFFF'),
			beige: Color.newHex('F5F5DC'),
			bisque: Color.newHex('FFE4C4'),
			black: Color.newHex('000000'),
			blanched_almond: Color.newHex('FFEBCD'),
			blue: Color.newHex('0000FF'),
			blue_violet: Color.newHex('8A2BE2'),
			brown: Color.newHex('A52A2A'),
			burlywood: Color.newHex('DEB887'),
			cadet_blue: Color.newHex('5F9EA0'),
			chartreuse: Color.newHex('7FFF00'),
			chocolate: Color.newHex('D2691E'),
			coral: Color.newHex('FF7F50'),
			cornflower_blue: Color.newHex('6495ED'),
			cornsilk: Color.newHex('FFF8DC'),
			crimson: Color.newHex('DC143C'),
			cyan: Color.newHex('00FFFF'),
			dark_blue: Color.newHex('00008B'),
			dark_cyan: Color.newHex('008B8B'),
			dark_goldenrod: Color.newHex('B8860B'),
			dark_gray: Color.newHex('A9A9A9'),
			dark_green: Color.newHex('006400'),
			dark_khaki: Color.newHex('BDB76B'),
			dark_magenta: Color.newHex('8B008B'),
			dark_olive_green: Color.newHex('556B2F'),
			dark_orchid: Color.newHex('9932CC'),
			dark_red: Color.newHex('8B0000'),
			dark_salmon: Color.newHex('E9967A'),
			dark_sea_green: Color.newHex('8FBC8F'),
			dark_slate_blue: Color.newHex('483D8B'),
			dark_slate_gray: Color.newHex('2F4F4F'),
			dark_slate_grey: Color.newHex('2F4F4F'),
			dark_turquoise: Color.newHex('00CED1'),
			dark_violet: Color.newHex('9400D3'),
			dark_orange: Color.newHex('FF8C00'),
			deep_pink: Color.newHex('FF1493'),
			deep_sky_blue: Color.newHex('00BFFF'),
			dim_gray: Color.newHex('696969'),
			dim_grey: Color.newHex('696969'),
			dodger_blue: Color.newHex('1E90FF'),
			feldspar: Color.newHex('D19275'),
			firebrick: Color.newHex('B22222'),
			floral_white: Color.newHex('FFFAF0'),
			forest_green: Color.newHex('228B22'),
			fuchsia: Color.newHex('FF00FF'),
			gainsboro: Color.newHex('DCDCDC'),
			ghost_white: Color.newHex('F8F8FF'),
			gold: Color.newHex('FFD700'),
			goldenrod: Color.newHex('DAA520'),
			gray: Color.newHex('808080'),
			grey: Color.newHex('696969'),
			green: Color.newHex('008000'),
			green_yellow: Color.newHex('ADFF2F'),
			honeydew: Color.newHex('F0FFF0'),
			hot_pink: Color.newHex('FF69B4'),
			indian_red: Color.newHex('CD5C5C'),
			indigo: Color.newHex('4B0082'),
			ivory: Color.newHex('FFFFF0'),
			khaki: Color.newHex('F0E68C'),
			lavender: Color.newHex('E6E6FA'),
			lavender_blush: Color.newHex('FFF0F5'),
			lawn_green: Color.newHex('7CFC00'),
			lemon_chiffon: Color.newHex('FFFACD'),
			light_blue: Color.newHex('ADD8E6'),
			light_coral: Color.newHex('F08080'),
			light_cyan: Color.newHex('E0FFFF'),
			light_goldenrod: Color.newHex('EEDD82'),
			light_goldenrod_yellow: Color.newHex('FAFAD2'),
			light_green: Color.newHex('90EE90'),
			light_gray: Color.newHex('D3D3D3'),
			light_grey: Color.newHex('D3D3D3'),
			light_pink: Color.newHex('FFB6C1'),
			light_salmon: Color.newHex('FFA07A'),
			light_sea_green: Color.newHex('20B2AA'),
			light_sky_blue: Color.newHex('87CEFA'),
			light_slate_blue: Color.newHex('8470FF'),
			light_slate_gray: Color.newHex('778899'),
			light_slate_grey: Color.newHex('778899'),
			light_steel_blue: Color.newHex('B0C4DE'),
			light_yellow: Color.newHex('FFFFE0'),
			lime: Color.newHex('00FF00'),
			lime_green: Color.newHex('32CD32'),
			linen: Color.newHex('FAF0E6'),
			magenta: Color.newHex('FF00FF'),
			maroon: Color.newHex('800000'),
			medium_aquamarine: Color.newHex('66CDAA'),
			medium_blue: Color.newHex('0000CD'),
			medium_orchid: Color.newHex('BA55D3'),
			medium_purple: Color.newHex('9370D8'),
			medium_sea_green: Color.newHex('3CB371'),
			medium_slate_blue: Color.newHex('7B68EE'),
			medium_spring_green: Color.newHex('00FA9A'),
			medium_turquoise: Color.newHex('48D1CC'),
			medium_violet_red: Color.newHex('C71585'),
			midnight_blue: Color.newHex('191970'),
			mint_cream: Color.newHex('F5FFFA'),
			misty_rose: Color.newHex('FFE4E1'),
			moccasin: Color.newHex('FFE4B5'),
			navajo_white: Color.newHex('FFDEAD'),
			navy: Color.newHex('000080'),
			navy_blue: Color.newHex('000080'),
			
			// none: Color.clear,   /// just in case
			
			old_lace: Color.newHex('FDF5E6'),
			olive: Color.newHex('808000'),
			olive_drab: Color.newHex('6B8E23'),
			orange: Color.newHex('FFA500'),
			orange_red: Color.newHex('FF4500'),
			orchid: Color.newHex('DA70D6'),
			pale_goldenrod: Color.newHex('EEE8AA'),
			pale_green: Color.newHex('98FB98'),
			pale_turquoise: Color.newHex('AFEEEE'),
			pale_violet_red: Color.newHex('D87093'),
			papaya_whip: Color.newHex('FFEFD5'),
			peach_puff: Color.newHex('FFDAB9'),
			peru: Color.newHex('CD853F'),
			pink: Color.newHex('FFC0CB'),
			plum: Color.newHex('DDA0DD'),
			powder_blue: Color.newHex('B0E0E6'),
			purple: Color.newHex('800080'),
			red: Color.newHex('FF0000'),
			rosy_brown: Color.newHex('BC8F8F'),
			royal_blue: Color.newHex('4169E1'),
			saddle_brown: Color.newHex('8B4513'),
			salmon: Color.newHex('FA8072'),
			sandy_brown: Color.newHex('F4A460'),
			sea_green: Color.newHex('2E8B57'),
			seashell: Color.newHex('FFF5EE'),
			sienna: Color.newHex('A0522D'),
			silver: Color.newHex('C0C0C0'),
			sky_blue: Color.newHex('87CEEB'),
			slate_blue: Color.newHex('6A5ACD'),
			slate_gray: Color.newHex('708090'),
			slate_grey: Color.newHex('708090'),
			snow: Color.newHex('FFFAFA'),
			spring_green: Color.newHex('00FF7F'),
			steel_blue: Color.newHex('4682B4'),
			tan: Color.newHex('D2B48C'),
			teal: Color.newHex('008080'),
			thistle: Color.newHex('D8BFD8'),
			tomato: Color.newHex('FF6347'),
			turquoise: Color.newHex('40E0D0'),
			violet: Color.newHex('EE82EE'),
			violet_red: Color.newHex('D02090'),
			wheat: Color.newHex('F5DEB3'),
			white: Color.newHex('FFFFFF'),
			white_smoke: Color.newHex('F5F5F5'),
			yellow: Color.newHex('FFFF00'),
			yellow_green: Color.newHex('9ACD32')
			);
			
		if(name.notNil)
			{ ^table.at(name.asSymbol).alpha_(alpha); }
			{ ^table }
		}
		
	*xwindowsBW { arg name, alpha = 1.0, includeGrayShades = true;
		var table;
		table = (
			black: Color(0, 0, 0), 
			white: Color(1, 1, 1),
			grey: Color.grey,
			gray: Color.grey,
			light_gray: Color.new255(211, 211, 211),
			light_grey: Color.new255(211, 211, 211),
			dim_gray: Color.new255(105, 105, 105),
			dim_grey: Color.new255(105, 105, 105),
			gainsboro: Color.new255(220, 220, 220),
			white_smoke: Color.new255(245, 245, 245)
			);
		if( includeGrayShades )
		{	101.do({ |i|
			var shade;
			shade = Color.grey(i/100);
			table.put(("gray" ++ i).asSymbol, shade);
			table.put(("grey" ++ i).asSymbol, shade);
			}); };
			
		if(name.notNil)
			{ ^table.at(name).alpha_(alpha); }
			{ ^table }
		}
			
	*xwindows { arg name, alpha = 1.0, includeGrayShades = false; // save speed
		var table;
		table = (
			alice_blue: Color.new255(240, 248, 255),
			antique_white: Color.new255(250, 235, 215),
			antique_white1: Color.new255(255, 239, 219),
			antique_white2: Color.new255(238, 223, 204),
			antique_white3: Color.new255(205, 192, 176),
			antique_white4: Color.new255(139, 131, 120),
			aquamarine: Color.new255(127, 255, 212),
			aquamarine1: Color.new255(127, 255, 212),
			aquamarine2: Color.new255(118, 238, 198),
			aquamarine3: Color.new255(102, 205, 170),
			aquamarine4: Color.new255(69, 139, 116),
			azure: Color.new255(240, 255, 255),
			azure1: Color.new255(240, 255, 255),
			azure2: Color.new255(224, 238, 238),
			azure3: Color.new255(193, 205, 205),
			azure4: Color.new255(131, 139, 139),
			beige: Color.new255(245, 245, 220),
			bisque: Color.new255(255, 228, 196),
			bisque1: Color.new255(255, 228, 196),
			bisque2: Color.new255(238, 213, 183),
			bisque3: Color.new255(205, 183, 158),
			bisque4: Color.new255(139, 125, 107),
			blanched_almond: Color.new255(255, 235, 205),
			blue: Color.new255(0, 0, 255),
			blue1: Color.new255(0, 0, 255),
			blue2: Color.new255(0, 0, 238),
			blue3: Color.new255(0, 0, 205),
			blue4: Color.new255(0, 0, 139),
			blue_violet: Color.new255(138, 43, 226),
			brown: Color.new255(165, 42, 42),
			brown1: Color.new255(255, 64, 64),
			brown2: Color.new255(238, 59, 59),
			brown3: Color.new255(205, 51, 51),
			brown4: Color.new255(139, 35, 35),
			burlywood: Color.new255(222, 184, 135),
			burlywood1: Color.new255(255, 211, 155),
			burlywood2: Color.new255(238, 197, 145),
			burlywood3: Color.new255(205, 170, 125),
			burlywood4: Color.new255(139, 115, 85),
			cadet_blue: Color.new255(95, 158, 160),
			cadet_blue1: Color.new255(152, 245, 255),
			cadet_blue2: Color.new255(142, 229, 238),
			cadet_blue3: Color.new255(122, 197, 205),
			cadet_blue4: Color.new255(83, 134, 139),
			chartreuse: Color.new255(127, 255, 0),
			chartreuse1: Color.new255(127, 255, 0),
			chartreuse2: Color.new255(118, 238, 0),
			chartreuse3: Color.new255(102, 205, 0),
			chartreuse4: Color.new255(69, 139, 0),
			chocolate: Color.new255(210, 105, 30),
			chocolate1: Color.new255(255, 127, 36),
			chocolate2: Color.new255(238, 118, 33),
			chocolate3: Color.new255(205, 102, 29),
			chocolate4: Color.new255(139, 69, 19),
			coral: Color.new255(255, 127, 80),
			coral1: Color.new255(255, 114, 86),
			coral2: Color.new255(238, 106, 80),
			coral3: Color.new255(205, 91, 69),
			coral4: Color.new255(139, 62, 47),
			cornflower_blue: Color.new255(100, 149, 237),
			cornsilk: Color.new255(255, 248, 220),
			cornsilk1: Color.new255(255, 248, 220),
			cornsilk2: Color.new255(238, 232, 205),
			cornsilk3: Color.new255(205, 200, 177),
			cornsilk4: Color.new255(139, 136, 120),
			cyan: Color.new255(0, 255, 255),
			cyan1: Color.new255(0, 255, 255),
			cyan2: Color.new255(0, 238, 238),
			cyan3: Color.new255(0, 205, 205),
			cyan4: Color.new255(0, 139, 139),
			dark_goldenrod: Color.new255(184, 134, 11),
			dark_goldenrod1: Color.new255(255, 185, 15),
			dark_goldenrod2: Color.new255(238, 173, 14),
			dark_goldenrod3: Color.new255(205, 149, 12),
			dark_goldenrod4: Color.new255(139, 101, 8),
			dark_green: Color.new255(0, 100, 0),
			dark_khaki: Color.new255(189, 183, 107),
			dark_olive_green: Color.new255(85, 107, 47),
			dark_olive_green1: Color.new255(202, 255, 112),
			dark_olive_green2: Color.new255(188, 238, 104),
			dark_olive_green3: Color.new255(162, 205, 90),
			dark_olive_green4: Color.new255(110, 139, 61),
			dark_orange: Color.new255(255, 140, 0),
			dark_orange1: Color.new255(255, 127, 0),
			dark_orange2: Color.new255(238, 118, 0),
			dark_orange3: Color.new255(205, 102, 0),
			dark_orange4: Color.new255(139, 69, 0),
			dark_orchid: Color.new255(153, 50, 204),
			dark_orchid1: Color.new255(191, 62, 255),
			dark_orchid2: Color.new255(178, 58, 238),
			dark_orchid3: Color.new255(154, 50, 205),
			dark_orchid4: Color.new255(104, 34, 139),
			dark_salmon: Color.new255(233, 150, 122),
			dark_sea_green: Color.new255(143, 188, 143),
			dark_sea_green1: Color.new255(193, 255, 193),
			dark_sea_green2: Color.new255(180, 238, 180),
			dark_sea_green3: Color.new255(155, 205, 155),
			dark_sea_green4: Color.new255(105, 139, 105),
			dark_slate_blue: Color.new255(72, 61, 139),
			dark_slate_gray: Color.new255(47, 79, 79),
			dark_slate_gray1: Color.new255(151, 255, 255),
			dark_slate_gray2: Color.new255(141, 238, 238),
			dark_slate_gray3: Color.new255(121, 205, 205),
			dark_slate_gray4: Color.new255(82, 139, 139),
			dark_slate_grey: Color.new255(47, 79, 79),
			dark_turquoise: Color.new255(0, 206, 209),
			dark_violet: Color.new255(148, 0, 211),
			deep_pink: Color.new255(255, 20, 147),
			deep_pink1: Color.new255(255, 20, 147),
			deep_pink2: Color.new255(238, 18, 137),
			deep_pink3: Color.new255(205, 16, 118),
			deep_pink4: Color.new255(139, 10, 80),
			deep_sky_blue: Color.new255(0, 191, 255),
			deep_sky_blue1: Color.new255(0, 191, 255),
			deep_sky_blue2: Color.new255(0, 178, 238),
			deep_sky_blue3: Color.new255(0, 154, 205),
			deep_sky_blue4: Color.new255(0, 104, 139),
			dodger_blue: Color.new255(30, 144, 255),
			dodger_blue1: Color.new255(30, 144, 255),
			dodger_blue2: Color.new255(28, 134, 238),
			dodger_blue3: Color.new255(24, 116, 205),
			dodger_blue4: Color.new255(16, 78, 139),
			firebrick: Color.new255(178, 34, 34),
			firebrick1: Color.new255(255, 48, 48),
			firebrick2: Color.new255(238, 44, 44),
			firebrick3: Color.new255(205, 38, 38),
			firebrick4: Color.new255(139, 26, 26),
			floral_white: Color.new255(255, 250, 240),
			forest_green: Color.new255(34, 139, 34),
			ghost_white: Color.new255(248, 248, 255),
			gold: Color.new255(255, 215, 0),
			gold1: Color.new255(255, 215, 0),
			gold2: Color.new255(238, 201, 0),
			gold3: Color.new255(205, 173, 0),
			gold4: Color.new255(139, 117, 0),
			goldenrod: Color.new255(218, 165, 32),
			goldenrod1: Color.new255(255, 193, 37),
			goldenrod2: Color.new255(238, 180, 34),
			goldenrod3: Color.new255(205, 155, 29),
			goldenrod4: Color.new255(139, 105, 20),
			green: Color.new255(0, 255, 0),
			green1: Color.new255(0, 255, 0),
			green2: Color.new255(0, 238, 0),
			green3: Color.new255(0, 205, 0),
			green4: Color.new255(0, 139, 0),
			green_yellow: Color.new255(173, 255, 47),
			honeydew: Color.new255(240, 255, 240),
			honeydew1: Color.new255(240, 255, 240),
			honeydew2: Color.new255(224, 238, 224),
			honeydew3: Color.new255(193, 205, 193),
			honeydew4: Color.new255(131, 139, 131),
			hot_pink: Color.new255(255, 105, 180),
			hot_pink1: Color.new255(255, 110, 180),
			hot_pink2: Color.new255(238, 106, 167),
			hot_pink3: Color.new255(205, 96, 144),
			hot_pink4: Color.new255(139, 58, 98),
			indian_red: Color.new255(205, 92, 92),
			indian_red1: Color.new255(255, 106, 106),
			indian_red2: Color.new255(238, 99, 99),
			indian_red3: Color.new255(205, 85, 85),
			indian_red4: Color.new255(139, 58, 58),
			ivory: Color.new255(255, 255, 240),
			ivory1: Color.new255(255, 255, 240),
			ivory2: Color.new255(238, 238, 224),
			ivory3: Color.new255(205, 205, 193),
			ivory4: Color.new255(139, 139, 131),
			khaki: Color.new255(240, 230, 140),
			khaki1: Color.new255(255, 246, 143),
			khaki2: Color.new255(238, 230, 133),
			khaki3: Color.new255(205, 198, 115),
			khaki4: Color.new255(139, 134, 78),
			lavender: Color.new255(230, 230, 250),
			lavender_blush: Color.new255(255, 240, 245),
			lavender_blush1: Color.new255(255, 240, 245),
			lavender_blush2: Color.new255(238, 224, 229),
			lavender_blush3: Color.new255(205, 193, 197),
			lavender_blush4: Color.new255(139, 131, 134),
			lawn_green: Color.new255(124, 252, 0),
			lemon_chiffon: Color.new255(255, 250, 205),
			lemon_chiffon1: Color.new255(255, 250, 205),
			lemon_chiffon2: Color.new255(238, 233, 191),
			lemon_chiffon3: Color.new255(205, 201, 165),
			lemon_chiffon4: Color.new255(139, 137, 112),
			light_blue: Color.new255(173, 216, 230),
			light_blue1: Color.new255(191, 239, 255),
			light_blue2: Color.new255(178, 223, 238),
			light_blue3: Color.new255(154, 192, 205),
			light_blue4: Color.new255(104, 131, 139),
			light_coral: Color.new255(240, 128, 128),
			light_cyan: Color.new255(224, 255, 255),
			light_cyan1: Color.new255(224, 255, 255),
			light_cyan2: Color.new255(209, 238, 238),
			light_cyan3: Color.new255(180, 205, 205),
			light_cyan4: Color.new255(122, 139, 139),
			light_goldenrod: Color.new255(238, 221, 130),
			light_goldenrod1: Color.new255(255, 236, 139),
			light_goldenrod2: Color.new255(238, 220, 130),
			light_goldenrod3: Color.new255(205, 190, 112),
			light_goldenrod4: Color.new255(139, 129, 76),
			light_goldenrod_yellow: Color.new255(250, 250, 210),
			light_pink: Color.new255(255, 182, 193),
			light_pink1: Color.new255(255, 174, 185),
			light_pink2: Color.new255(238, 162, 173),
			light_pink3: Color.new255(205, 140, 149),
			light_pink4: Color.new255(139, 95, 101),
			light_salmon: Color.new255(255, 160, 122),
			light_salmon1: Color.new255(255, 160, 122),
			light_salmon2: Color.new255(238, 149, 114),
			light_salmon3: Color.new255(205, 129, 98),
			light_salmon4: Color.new255(139, 87, 66),
			light_sea_green: Color.new255(32, 178, 170),
			light_sky_blue: Color.new255(135, 206, 250),
			light_sky_blue1: Color.new255(176, 226, 255),
			light_sky_blue2: Color.new255(164, 211, 238),
			light_sky_blue3: Color.new255(141, 182, 205),
			light_sky_blue4: Color.new255(96, 123, 139),
			light_slate_blue: Color.new255(132, 112, 255),
			light_slate_gray: Color.new255(119, 136, 153),
			light_slate_grey: Color.new255(119, 136, 153),
			light_steel_blue: Color.new255(176, 196, 222),
			light_steel_blue1: Color.new255(202, 225, 255),
			light_steel_blue2: Color.new255(188, 210, 238),
			light_steel_blue3: Color.new255(162, 181, 205),
			light_steel_blue4: Color.new255(110, 123, 139),
			light_yellow: Color.new255(255, 255, 224),
			light_yellow1: Color.new255(255, 255, 224),
			light_yellow2: Color.new255(238, 238, 209),
			light_yellow3: Color.new255(205, 205, 180),
			light_yellow4: Color.new255(139, 139, 122),
			lime_green: Color.new255(50, 205, 50),
			linen: Color.new255(250, 240, 230),
			magenta: Color.new255(255, 0, 255),
			magenta1: Color.new255(255, 0, 255),
			magenta2: Color.new255(238, 0, 238),
			magenta3: Color.new255(205, 0, 205),
			magenta4: Color.new255(139, 0, 139),
			maroon: Color.new255(176, 48, 96),
			maroon1: Color.new255(255, 52, 179),
			maroon2: Color.new255(238, 48, 167),
			maroon3: Color.new255(205, 41, 144),
			maroon4: Color.new255(139, 28, 98),
			medium_aquamarine: Color.new255(102, 205, 170),
			medium_blue: Color.new255(0, 0, 205),
			medium_orchid: Color.new255(186, 85, 211),
			medium_orchid1: Color.new255(224, 102, 255),
			medium_orchid2: Color.new255(209, 95, 238),
			medium_orchid3: Color.new255(180, 82, 205),
			medium_orchid4: Color.new255(122, 55, 139),
			medium_purple: Color.new255(147, 112, 219),
			medium_purple1: Color.new255(171, 130, 255),
			medium_purple2: Color.new255(159, 121, 238),
			medium_purple3: Color.new255(137, 104, 205),
			medium_purple4: Color.new255(93, 71, 139),
			medium_sea_green: Color.new255(60, 179, 113),
			medium_slate_blue: Color.new255(123, 104, 238),
			medium_spring_green: Color.new255(0, 250, 154),
			medium_turquoise: Color.new255(72, 209, 204),
			medium_violet_red: Color.new255(199, 21, 133),
			midnight_blue: Color.new255(25, 25, 112),
			mint_cream: Color.new255(245, 255, 250),
			misty_rose: Color.new255(255, 228, 225),
			misty_rose1: Color.new255(255, 228, 225),
			misty_rose2: Color.new255(238, 213, 210),
			misty_rose3: Color.new255(205, 183, 181),
			misty_rose4: Color.new255(139, 125, 123),
			moccasin: Color.new255(255, 228, 181),
			navajo_white: Color.new255(255, 222, 173),
			navajo_white1: Color.new255(255, 222, 173),
			navajo_white2: Color.new255(238, 207, 161),
			navajo_white3: Color.new255(205, 179, 139),
			navajo_white4: Color.new255(139, 121, 94),
			navy: Color.new255(0, 0, 128),
			navy_blue: Color.new255(0, 0, 128),
			old_lace: Color.new255(253, 245, 230),
			olive_drab: Color.new255(107, 142, 35),
			olive_drab1: Color.new255(192, 255, 62),
			olive_drab2: Color.new255(179, 238, 58),
			olive_drab3: Color.new255(154, 205, 50),
			olive_drab4: Color.new255(105, 139, 34),
			orange: Color.new255(255, 165, 0),
			orange1: Color.new255(255, 165, 0),
			orange2: Color.new255(238, 154, 0),
			orange3: Color.new255(205, 133, 0),
			orange4: Color.new255(139, 90, 0),
			orange_red: Color.new255(255, 69, 0),
			orange_red1: Color.new255(255, 69, 0),
			orange_red2: Color.new255(238, 64, 0),
			orange_red3: Color.new255(205, 55, 0),
			orange_red4: Color.new255(139, 37, 0),
			orchid: Color.new255(218, 112, 214),
			orchid1: Color.new255(255, 131, 250),
			orchid2: Color.new255(238, 122, 233),
			orchid3: Color.new255(205, 105, 201),
			orchid4: Color.new255(139, 71, 137),
			pale_goldenrod: Color.new255(238, 232, 170),
			pale_green: Color.new255(152, 251, 152),
			pale_green1: Color.new255(154, 255, 154),
			pale_green2: Color.new255(144, 238, 144),
			pale_green3: Color.new255(124, 205, 124),
			pale_green4: Color.new255(84, 139, 84),
			pale_turquoise: Color.new255(175, 238, 238),
			pale_turquoise1: Color.new255(187, 255, 255),
			pale_turquoise2: Color.new255(174, 238, 238),
			pale_turquoise3: Color.new255(150, 205, 205),
			pale_turquoise4: Color.new255(102, 139, 139),
			pale_violet_red: Color.new255(219, 112, 147),
			pale_violet_red1: Color.new255(255, 130, 171),
			pale_violet_red2: Color.new255(238, 121, 159),
			pale_violet_red3: Color.new255(205, 104, 137),
			pale_violet_red4: Color.new255(139, 71, 93),
			papaya_whip: Color.new255(255, 239, 213),
			peach_puff: Color.new255(255, 218, 185),
			peach_puff1: Color.new255(255, 218, 185),
			peach_puff2: Color.new255(238, 203, 173),
			peach_puff3: Color.new255(205, 175, 149),
			peach_puff4: Color.new255(139, 119, 101),
			peru: Color.new255(205, 133, 63),
			pink: Color.new255(255, 192, 203),
			pink1: Color.new255(255, 181, 197),
			pink2: Color.new255(238, 169, 184),
			pink3: Color.new255(205, 145, 158),
			pink4: Color.new255(139, 99, 108),
			plum: Color.new255(221, 160, 221),
			plum1: Color.new255(255, 187, 255),
			plum2: Color.new255(238, 174, 238),
			plum3: Color.new255(205, 150, 205),
			plum4: Color.new255(139, 102, 139),
			powder_blue: Color.new255(176, 224, 230),
			purple: Color.new255(160, 32, 240),
			purple1: Color.new255(155, 48, 255),
			purple2: Color.new255(145, 44, 238),
			purple3: Color.new255(125, 38, 205),
			purple4: Color.new255(85, 26, 139),
			red: Color.new255(255, 0, 0),
			red1: Color.new255(255, 0, 0),
			red2: Color.new255(238, 0, 0),
			red3: Color.new255(205, 0, 0),
			red4: Color.new255(139, 0, 0),
			rosy_brown: Color.new255(188, 143, 143),
			rosy_brown1: Color.new255(255, 193, 193),
			rosy_brown2: Color.new255(238, 180, 180),
			rosy_brown3: Color.new255(205, 155, 155),
			rosy_brown4: Color.new255(139, 105, 105),
			royal_blue: Color.new255(65, 105, 225),
			royal_blue1: Color.new255(72, 118, 255),
			royal_blue2: Color.new255(67, 110, 238),
			royal_blue3: Color.new255(58, 95, 205),
			royal_blue4: Color.new255(39, 64, 139),
			saddle_brown: Color.new255(139, 69, 19),
			salmon: Color.new255(250, 128, 114),
			salmon1: Color.new255(255, 140, 105),
			salmon2: Color.new255(238, 130, 98),
			salmon3: Color.new255(205, 112, 84),
			salmon4: Color.new255(139, 76, 57),
			sandy_brown: Color.new255(244, 164, 96),
			sea_green: Color.new255(46, 139, 87),
			sea_green1: Color.new255(84, 255, 159),
			sea_green2: Color.new255(78, 238, 148),
			sea_green3: Color.new255(67, 205, 128),
			sea_green4: Color.new255(46, 139, 87),
			seashell: Color.new255(255, 245, 238),
			seashell1: Color.new255(255, 245, 238),
			seashell2: Color.new255(238, 229, 222),
			seashell3: Color.new255(205, 197, 191),
			seashell4: Color.new255(139, 134, 130),
			sienna: Color.new255(160, 82, 45),
			sienna1: Color.new255(255, 130, 71),
			sienna2: Color.new255(238, 121, 66),
			sienna3: Color.new255(205, 104, 57),
			sienna4: Color.new255(139, 71, 38),
			sky_blue: Color.new255(135, 206, 235),
			sky_blue1: Color.new255(135, 206, 255),
			sky_blue2: Color.new255(126, 192, 238),
			sky_blue3: Color.new255(108, 166, 205),
			sky_blue4: Color.new255(74, 112, 139),
			slate_blue: Color.new255(106, 90, 205),
			slate_blue1: Color.new255(131, 111, 255),
			slate_blue2: Color.new255(122, 103, 238),
			slate_blue3: Color.new255(105, 89, 205),
			slate_blue4: Color.new255(71, 60, 139),
			slate_gray: Color.new255(112, 128, 144),
			slate_gray1: Color.new255(198, 226, 255),
			slate_gray2: Color.new255(185, 211, 238),
			slate_gray3: Color.new255(159, 182, 205),
			slate_gray4: Color.new255(108, 123, 139),
			slate_grey: Color.new255(112, 128, 144),
			snow: Color.new255(255, 250, 250),
			snow1: Color.new255(255, 250, 250),
			snow2: Color.new255(238, 233, 233),
			snow3: Color.new255(205, 201, 201),
			snow4: Color.new255(139, 137, 137),
			spring_green: Color.new255(0, 255, 127),
			spring_green1: Color.new255(0, 255, 127),
			spring_green2: Color.new255(0, 238, 118),
			spring_green3: Color.new255(0, 205, 102),
			spring_green4: Color.new255(0, 139, 69),
			steel_blue: Color.new255(70, 130, 180),
			steel_blue1: Color.new255(99, 184, 255),
			steel_blue2: Color.new255(92, 172, 238),
			steel_blue3: Color.new255(79, 148, 205),
			steel_blue4: Color.new255(54, 100, 139),
			tan: Color.new255(210, 180, 140),
			tan1: Color.new255(255, 165, 79),
			tan2: Color.new255(238, 154, 73),
			tan3: Color.new255(205, 133, 63),
			tan4: Color.new255(139, 90, 43),
			thistle: Color.new255(216, 191, 216),
			thistle1: Color.new255(255, 225, 255),
			thistle2: Color.new255(238, 210, 238),
			thistle3: Color.new255(205, 181, 205),
			thistle4: Color.new255(139, 123, 139),
			tomato: Color.new255(255, 99, 71),
			tomato1: Color.new255(255, 99, 71),
			tomato2: Color.new255(238, 92, 66),
			tomato3: Color.new255(205, 79, 57),
			tomato4: Color.new255(139, 54, 38),
			turquoise: Color.new255(64, 224, 208),
			turquoise1: Color.new255(0, 245, 255),
			turquoise2: Color.new255(0, 229, 238),
			turquoise3: Color.new255(0, 197, 205),
			turquoise4: Color.new255(0, 134, 139),
			violet: Color.new255(238, 130, 238),
			violet_red: Color.new255(208, 32, 144),
			violet_red1: Color.new255(255, 62, 150),
			violet_red2: Color.new255(238, 58, 140),
			violet_red3: Color.new255(205, 50, 120),
			violet_red4: Color.new255(139, 34, 82),
			wheat: Color.new255(245, 222, 179),
			wheat1: Color.new255(255, 231, 186),
			wheat2: Color.new255(238, 216, 174),
			wheat3: Color.new255(205, 186, 150),
			wheat4: Color.new255(139, 126, 102),
			yellow: Color.new255(255, 255, 0),
			yellow1: Color.new255(255, 255, 0),
			yellow2: Color.new255(238, 238, 0),
			yellow3: Color.new255(205, 205, 0),
			yellow4: Color.new255(139, 139, 0),
			yellow_green: Color.new255(154, 205, 50)
			);
		table.putAll(Color.xwindowsBW(includeGrayShades: includeGrayShades) );
		if(name.notNil)
			{ ^table.at(name.asSymbol).alpha_(alpha); }
			{ ^table };
		}
	}