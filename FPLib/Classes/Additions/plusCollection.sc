/*

Server.all.collect{ |x| x.name -> x.addr}.as(Array).asDictFromAssocs

*/



+ Collection {

	asDictFromAssocs{
		^Dictionary.with(*this)
	}

	asIdentDictFromAssocs{
		^IdentityDictionary.with(*this)
	}

}