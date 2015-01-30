// [MonoidAll(true), MonoidAll(true)].mreduce
// [MonoidAll(true), MonoidAll(false)].mreduce
MonoidAll {
	var <bool;
	*new{ |x| ^super.newCopyArgs(x) }
	*zero{ ^MonoidAll(true) }
	|+| { |x| ^MonoidAll(bool && x.bool) }
	storeArgs { ^[bool] }
	printOn { arg stream;
		stream << this.class.name << "( " << bool << " )";
	}
}

// [MonoidAny(false), MonoidAny(true)].mreduce
// [MonoidAny(false), MonoidAny(false)].mreduce
MonoidAny {
	var <bool;
	*new{ |x| ^super.newCopyArgs(x) }
	*zero{ ^MonoidAny(false) }
	|+| { |x| ^MonoidAny(bool || x.bool) }
	printOn { arg stream;
		stream << this.class.name << "( " << bool << " )";
	}
}

// [MonoidSum(2), MonoidSum(4)].mreduce
MonoidSum {
	var <a;
	*new{ |x| ^super.newCopyArgs(x) }
	*zero{ ^MonoidSum(0) }
	|+| { |x| ^MonoidSum(a + x.a) }
	printOn { arg stream;
		stream << this.class.name << "( " << a << " )";
	}
}

// [MonoidProduct(2), MonoidProduct(4)].mreduce
MonoidProduct {
	var <a;
	*new{ |x| ^super.newCopyArgs(x) }
	*zero{ ^MonoidProduct(1) }
	|+| { |x| ^MonoidProduct(a * x.a) }
	printOn { arg stream;
		stream << this.class.name << "( " << a << " )";
	}
}

