
/* 
 * SuperCollider3 source file "TwoWayDictionary.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class TwoWayDictionary -------------------------------------------------
//
// <p></p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7cc2]  
TwoWayDictionary : Dictionary {

    // --- attributes

    var reverse; // type  Dictionary


    // --- add(association) : void --------------------------------------------
    //    
    add { arg association; // type Association        
        var rev;
        
        super.add(association);
        if ( reverse == nil, {
            reverse = Dictionary.new;
        });
        rev = Association.new(association.value, association.key);
        reverse.add(rev);
    } // end add        


    // --- atValue(value) : Object --------------------------------------------
    //     
    atValue { arg value; // type Object        
        ^reverse.at(value);
    } // end atValue        


} // end TwoWayDictionary
