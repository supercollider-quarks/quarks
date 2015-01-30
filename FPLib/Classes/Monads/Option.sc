/*
*  	copied from Scala API
*  Scala API (c) 2002-2010, LAMP/EPFL
*  http://scala-lang.org/
*
* conversion to sc: Miguel Negrão, 2011
*/

Option {



    /*
                very intersting: traverse cannot be defined for Option in a dynamic language, because
                there is no specification of what the class of f.(a) should be for None()...
                'traverse' : { |f,a|
                var fa = f.(a);
                if( a.isDefined) {
                f.a.fmap{ |x| Some(x) }
                } {
                None().pure(?????)
                }
                }*/

    *from{ |x| ^if (x.isNil){ None() }{ Some(x) } }

    *empty{ ^None() }

    *makePure { |a| ^Some(a) }

	*zero { ^None() }

    == { |ob|
        ^if(ob.isKindOf(Option) ) {
            this.match(
                { |a|
                    ob.match(
                        { |b|
                            a == b
                        },{
                            false
                        }
                    )
                }, {
                    ob.isEmpty
                }
            )
        } {
            false
        }
    }

	asOption {
		^this
	}

}

Some : Option {
    var value;

    *new{ |val| ^super.newCopyArgs(val) }

    match{ |fsome, fnone|
        ^fsome.(value)
    }

    isEmpty{ ^false }

    get{ ^value }

    isDefined{ ^true }

    getOrElse{ ^this.get }

    getOrElseDoNothing{ ^this.get }

    orNil{ ^this.get }

    collect{ |f| ^Some(f.(this.get)) }

    >>= { |f| ^f.(this.get) }

	|+| { |o| ^o.collect{ |x| value |+| x } }

    select{ |p| ^if(p.(this.get)){ this }{ None() } }

    do{ |f| f.(this.get) }

    exists{ ^this.get }

    orElse{ ^this }

    asArray{ ^[this.get]}

    flatten{ ^this.asArray }

    printOn { arg stream;
        stream << this.class.name << "(" << value << ")";
    }

	storeArgs {
		^[value]
	}
}

None : Option{
    classvar singleton;

    *initClass{
        singleton = None.makeNew();
    }

    *makeNew{
        ^super.new
    }

    *new {
        ^singleton
    }

    match{ |fsome, fnone|
        ^fnone.()
    }

    isEmpty{ ^true }

    get{ Error("None().get").throw }

    isDefined{ ^false }

    getOrElse{ |default| ^default }

    getOrElseDoNothing{ ^IO{ Unit } }

    orNil{ ^nil }

    collect{}

    >>= {}

	|+| {}

    select{}

    do{}

    exists{ ^false }

    orElse{ |alternative| ^alternative }

    asArray{ ^[] }

    flatten{ ^this.asArray }
    printOn { arg stream;
        stream << "None()";
    }

}

+ Object {

    asOption {
        ^Some(this)
    }

}

+ Nil {

    asOption {
        ^None()
    }
}

+ Array {

    atOption { |i|
        var x = this.at(i);
        ^if(x.isNil) { None() } { Some(x) }
    }

    catOptions {
        ^this.inject([], { |state, ma|
            ma.match({ |a|
                state ++ [a]
                },{
                    state
            })
        })
    }

    catOptions2 {
        ^if( this.isEmpty ) {
            None()
        } {
            Some( this.catOptions )
        }
    }

}
