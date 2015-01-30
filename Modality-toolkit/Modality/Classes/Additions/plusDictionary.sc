+ Dictionary {

	sortedKeysValuesCollect { arg function, sortFunc;
		var keys = this.keys(Array);
		var res = this.class.new(this.size);
		keys.sort(sortFunc);

		keys.do { arg key, i;
			res.put( key, function.value(this[key], key, i) );
		};
		^res;
	}
}