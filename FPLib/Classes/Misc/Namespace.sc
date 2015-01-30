
/*
An environment that can storing and retrieving functions
Will not evaluate the functions when retrieved which is a necessary
condition for doing functional programming.

(
x = Namespace.make{
    ~f = { |x| x + 1 };
    ~g = { |x| x + 10 };
}
)

(x.f <> x.g).(1)

M : Namespace {

    *initClass{
         namespace = Namespace();
         namespace.sum = _+_;
    }
}

x = Namespace();
x.sum = _+_;

*/
Namespace : Environment {

	*new{
        ^super.new.know_(true)
    }

    doesNotUnderstand { arg selector ... args;
		var value = this[selector];
        ^if (value.notNil && know) {
			value
		} {
			this.superPerformList(\doesNotUnderstand, selector, args)
		}
    }

	*doesNotUnderstand { arg selector ... args;
		^this.namespace.performList(selector, args)
	}

}



	