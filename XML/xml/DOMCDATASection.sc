
/* 
 * SuperCollider3 source file "DOMCDATASection.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class DOMCDATASection --------------------------------------------------
//
// <p></p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7e62]  
DOMCDATASection : DOMCharacterData {

    // --- new(owner, cdata) : DOMCDATASection --------------------------------
    //      
    *new { arg owner, cdata; // types DOMDocument, String        
        ^super.new.init(owner, DOMNode.node_CDATA_SECTION, "#cdata-section", cdata, "<![CDATA[", "]]>", "");
    } // end new        


} // end DOMCDATASection
