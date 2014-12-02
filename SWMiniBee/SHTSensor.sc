SHTSensor{

	var <rTemp, <rHum;
	var <tempC, <tempF;
	var <humidity;

	var <dewPoint,<dewPointF;

	var <>c1 = -4.0;
	var <>c2 = 0.0405;
	var <>c3 = -0.0000028;
	var <>d1C = -40.0;
	var <>d2C = 0.01;
	var <>d1F = -40.0;
	var <>d2F = 0.01;
	var <>t1 = 0.01;
	var <>t2 = 0.00008;

	*new{
		^super.new.init;
	}

	init{
		this.setModeRH;
		this.setModeT;
		this.rTemp_( 20 );
		this.rHum_( 30 );
	}

	setModeT{ |mode = '14bit', volt = 'V35'|
		switch( volt,
			'V5', { d1C = -40.1; d1F = -40.2 },
			'V4', { d1C = -39.8; d1F = -39.6 },
			'V35', { d1C = -39.7; d1F = -39.5 },
			'V3', { d1C = -39.6; d1F = -39.3 },
			'V25', { d1C = -39.4; d1F = -38.9 }
		);
		switch( mode,
			'14bit', { d2C = 0.01; d2F = 0.018;  },
			'12bit', { d2C = 0.04; d2F = 0.072;  }
		);
	}

	setModeRH{ |mode = 'V4_12bit'|
		switch( mode,
			\V4_12bit, { c1 = -2.0468; c2 = 0.0367; c3 = -1.5955e-6; t1=0.01; t2 = 0.00008; },
			\V4_8bit,  { c1 = -2.0468; c2 = 0.5872; c3 = -4.0845e-4; t1=0.01; t2 = 0.00128; },
			\V3_12bit, { c1 = -4.0; c2 = 0.0405; c3 = -2.8e-6; t1=0.01; t2 = 0.00008; },
			\V3_8bit, { c1 = -4.0; c2 = 0.648; c3 = -7.2e-4;  t1=0.01; t2 = 0.00128; }
		)
	}

	getVals{
		^[ rTemp, rHum, tempC, tempF, humidity, dewPoint, dewPointF ];
	}
	
	rTemp_{ |val|
		rTemp = val;
		this.calcTempC;
		this.calcTempF;
	}

	rHum_{ |val|
		rHum = val;
		this.calcRH;
		this.calcDewpointSHT;
	}

	calcRH{
		var rhlin;
		rhlin = (c3 * rHum * rHum) + (c2 * rHum) + c1;
		humidity = tempC * (t1 + (t2*rHum)) + rhlin;
		humidity = humidity.clip( 0.1, 100 );
	}

	calcTempC{
		tempC = rTemp * d2C;
		tempC = tempC + d1C;
	}

	calcTempF{
		tempF = rTemp * d2F;
		tempF = tempF + d1F;
	}

	// from someone's arduino code
	calcDewpoint{
		var logEx;
		logEx= 0.66077 + ((7.5*tempC)/(237.3+tempC)) + (humidity.log10 - 2);
		dewPoint = (logEx - 0.66077)*237.3/(0.66077+7.5-logEx);
		dewPointF = 9 * dewPoint / 5 + 32;
	}

	// from SHT datasheet
	calcDewpointSHT{
		var tn, m;
		var mt,lnrh;
		if ( tempC > 0 ){
			tn = 243.12; m = 17.62;
		}{
			tn = 272.62; m = 22.46;
		};

		mt = m*tempC / (tn+tempC);
		lnrh = (humidity/100).log;
		dewPoint = tn * ( lnrh + mt ) / ( m - lnrh - mt );
		dewPointF = 9 * dewPoint / 5 + 32;
	}

}

