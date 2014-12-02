HarmonicMetric {

	var <>type;
	
	*new {|metric|
		metric = metric ? \harmonicity;
		^super.new.type = metric}
	
	value {|ratios|
		if ((ratios.size == 2) and: (ratios[0].isNumber)) {
			type.switch(
				\harmonicity, 		{^ratios.harmonicity.abs  },
				\harmonicDistance,		{^ratios.harmonicDistance },
				\gradusSuavitatis,		{^ratios.gradusSuavitatis }
/*				\magnitiude, 			{^ratios.asHvector.magnitude }*/
			)
		}{
			^ratios.collect{|x| this.value(x) }
		}	
	}

	mostHarmonic {|ratios| var res;
		res = this.value(ratios);
		if (type == \harmonicity) { 
			^ratios[res.indexOf(res.maxItem)];
		};
		^ratios[res.indexOf(res.minItem)];
	}
	
	leastHarmonic {|ratios| var res;
		res = this.value(ratios);
		if (type == \harmonicity) { 
			^ratios[res.indexOf(res.minItem)];
		};
		^ratios[res.indexOf(res.maxItem)];
	}
	
	order {|ratios| var res, order;
		res = this.value(ratios);
		order = res.order;
		if (type == \harmonicity) {order = order.reverse};
		^ratios[order]
	}
	
	asString { ^type }	

}

/*
	add specific harmonicity

HarmonicMetric
	abs (in case of harmonicity)
	order
	comparision [ >, <, ==]  (in case of gS it is the smallest element!)
	metric 	returns harmonicity, hD, gS or mag
	
	nLargest & nSmallest: 
		return the lowest or highest number of intervals of a pitch set according to the metric
	
		
*/