
/* 
 * SuperCollider3 source file "DOMText.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class DOMText ----------------------------------------------------------
//
// <p></p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7d62]  
DOMText : DOMCharacterData {

    // --- attributes

    var textValue; // type  String


    // --- new(owner, text) : DOMText -----------------------------------------
    //      
    *new { arg owner, text; // types DOMDocument, String        
        ^super.new.init(owner, DOMNode.node_TEXT, "#text", text, "", "", "<");
    } // end new        


    // --- splitText(offset) : DOMText ----------------------------------------
    //     
    splitText { arg offset; // type int        
        var s;
        var n;
        
        // create a second text-node right after this one, contains the latter part of this node's content split at offset
        s = this.getData;
        n = this.cloneNode(false);
        this.getParentNode.insertBefore(n, this.getNextSibling);
        this.setData(s.copyFromStart(offset-1));
        n.setData(s.copyToEnd(offset));
        ^n; // type DOMText
    } // end splitText        


    // --- getText() : String -------------------------------------------------
    //    
    getText {        
        // getText is an extension to the original DOM.
        // In contrast to getData, it provides a string with already decoded standard entities.
        if ( textValue == nil, { // first access, cache now
            textValue = DOMDocument.decodeStandardEntities( this.getNodeValue );
        });
        ^textValue;
    } // end getText        


    // --- setText(text) :  ---------------------------------------------------
    //    
    setText { arg text; // type String        
        // setText is an extension to the original DOM.
        // In contrast to setData, it encodes standard entities in the string.
        nodeValue = DOMDocument.encodeStandardEntities( text );
        textValue = text;        
    } // end setText        


    // --- getDataFormatted() : String ----------------------------------------
    //    
    getDataFormatted {        
        ^DOMDocument.encodeStandardEntities( this.getData ); // type String
    } // end getDataFormatted        


    // --- setData(data) :  ---------------------------------------------------
    //    
    setData { arg data; // type String        
        // overwrites DOMCharacterData.setData
        textValue = nil; // uncache
        super.setData(data);
    } // end setData        


} // end DOMText
