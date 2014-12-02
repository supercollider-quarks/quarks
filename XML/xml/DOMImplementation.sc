
/* 
 * SuperCollider3 source file "DOMImplementation.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class DOMImplementation ------------------------------------------------
//
// <p></p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7d44]  
DOMImplementation {

    // --- attributes

    classvar instance; // type  DOMImplementation


    // --- instance() : DOMImplementation -------------------------------------
    //    
    *instance {        
        if ( DOMImplementation.instance == nil , {
            DOMImplementation.instance = DOMInstance.new;
        };)
        ^DOMImplementation.instance; // type  DOMImplementation
    } // end instance        


    // --- hasFeature(feature, version) : boolean -----------------------------
    //      
    hasFeature { arg feature, version; // types String, String        
        // This is completely useless and will most likely never be used. 
        // But it's part of the DOM specification...
        if ( (feature.compare("XML", true) == 0), {
            if ( version == nil, {
                ^true;
            },{
                if ( version.compare("1.0") <= 0, {
                    ^true;
                });
            });
        };)
        ^false;            
    } // end hasFeature        


} // end DOMImplementation
