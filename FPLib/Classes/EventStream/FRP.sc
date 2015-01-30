FRP : Namespace {

	classvar <namespace;

	*initClass {
		namespace = Namespace();

		namespace.softset = { |es, delta = 0.1|
			var outSig;
			var es2 = es.asCollection;
			var checked =  { |e|
				e.storePrevious(0.0).collect{ |t|
					var current = outSig.now;
					if( (absdif(current, t.at1) < delta) || (absdif(current,t.at2) < delta) ){Some(t.at2)}{None()}
				}.selectSome
			};
			var outES = es2.collect(checked).mreduce;
			outSig = outES.hold(0.0);
			outES
		}
	}

}
