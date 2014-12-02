
/* 
 * SuperCollider3 source file "DOMNode.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class DOMNode ----------------------------------------------------------
//
// <p>Attributes of the node are stored via Dictionary-entries.</p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7da9]  
DOMNode {

    // --- attributes

    classvar <>node_ELEMENT = 1; // type  int
    classvar <>node_ATTRIBUTE = 2; // type  int
    classvar <>node_TEXT = 3; // type  int
    classvar <>node_CDATA_SECTION = 4; // type  int
    classvar <>node_PROCESSING_INSTRUCTION = 7; // type  int
    classvar <>node_COMMENT = 8; // type  int
    classvar <>node_DOCUMENT = 9; // type  int
    classvar <>node_DOCUMENT_FRAGMENT = 11; // type  int
    var nodeValue; // type  String
    var nodeName; // type  String
    var nodeType; // type  int
    var startIndex; // type  int
    var splitIndex; // type  int
    var endIndex; // type  int
    var attributes = nil; // type  Dictionary


    // --- relationships

    var ownerDocument; // 0..1-relation to type  DOMDocument
    var parent; // 0..1-relation to type  DOMNode
    var children; // 0..*-relation to type  DOMNode



    // --- getNodeName() : String ---------------------------------------------
    //    
    getNodeName {        
        ^nodeName;
    } // end getNodeName        


    // --- setNodeName(name) : void -------------------------------------------
    //    
    setNodeName { arg name; // type String        
        nodeName = name;
    } // end setNodeName        


    // --- getNodeValue() : String --------------------------------------------
    //    
    getNodeValue {        
        ^nodeValue;
    } // end getNodeValue        


    // --- setNodeValue(value) : void -----------------------------------------
    //    
    setNodeValue { arg value; // type String        
        nodeValue = value;
    } // end setNodeValue        


    // --- getNodeType() : int ------------------------------------------------
    //    
    getNodeType {        
        ^nodeType;
    } // end getNodeType        


    // --- setNodeType(type) : void -------------------------------------------
    //    
    setNodeType { arg type; // type int        
        nodeType = type;
    } // end setNodeType        


    // --- getParentNode() : DOMNode ------------------------------------------
    //    
    getParentNode {        
        ^parent; // type DOMNode
    } // end getParentNode        


    // --- setParentNode(p) :  ------------------------------------------------
    //    
    setParentNode { arg p; // type DOMNode        
        parent = p;
    } // end setParentNode        


    // --- getChildNodes(deep) : List -----------------------------------------
    //     
    getChildNodes { arg deep; // type boolean        
        var l;
        
        if ( deep == true, {
            l = List.new;
            children.do({ arg node;
                l.add(node);
                l.addAll( node.getChildNodes(true) );
            });
        },{
            l = children;
        });
        ^l; // type List
    } // end getChildNodes        


    // --- getFirstChild() : DOMNode ------------------------------------------
    //    
    getFirstChild {        
        ^children.first;
    } // end getFirstChild        


    // --- getLastChild() : DOMNode -------------------------------------------
    //    
    getLastChild {        
        ^children.last;
    } // end getLastChild        


    // --- getPreviousSibling() : DOMNode -------------------------------------
    //    
    getPreviousSibling {        
        if ( parent != nil, {
            ^parent.getPreviousSiblingOfChild(this);
        },{
            ^nil;
        });
    } // end getPreviousSibling        


    // --- getNextSibling() : DOMNode -----------------------------------------
    //    
    getNextSibling {        
        if ( parent != nil, {
            ^parent.getNextSiblingOfChild(this);
        },{
            ^nil;
        });
    } // end getNextSibling        


    // --- getAttributes() : Dictionary ---------------------------------------
    //    
    getAttributes {        
        // default implementation returns nil, overwritten by DOMElement
        ^nil; // type Dictionary
    } // end getAttributes        


    // --- getOwnerDocument() : DOMDocument -----------------------------------
    //    
    getOwnerDocument {        
        ^ownerDocument; // type DOMDocument
    } // end getOwnerDocument        


    // --- insertBefore(newChild, refChild) : DOMNode -------------------------
    //      
    insertBefore { arg newChild, refChild; // types DOMNode, DOMNode        
        var i = children.indexOf(refChild);
        if ( i != nil , {
            children.insert(i, newChild);
            newChild.setParentNode(this);
        },{
            this.appendChild(newChild); // insert at end if refChild==nil
        });
    } // end insertBefore        


    // --- replaceChild(newChild, oldChild) : DOMNode -------------------------
    //      
    replaceChild { arg newChild, oldChild; // types DOMNode, DOMNode        
        var i = children.indexOf(oldChild);
        if ( i != nil , {
            children.put(i, newChild);
            oldChild.setParentNode(nil);
            newChild.setParentNode(this);
        });
    } // end replaceChild        


    // --- removeChild(oldChild) : DOMNode ------------------------------------
    //     
    removeChild { arg oldChild; // type DOMNode        
        var i = children.indexOf(oldChild);
        if ( i != nil , {
            children.removeAt(i);
            oldChild.setParentNode(nil);
        });
    } // end removeChild        


    // --- appendChild(node) : void -------------------------------------------
    //    
    appendChild { arg node; // type DOMNode        
        if ( ( not (children.includes(node)) ) , {
            node.setParentNode(this);
            children.add(node);
        });
    } // end appendChild        


    // --- hasChildNodes() : boolean ------------------------------------------
    //    
    hasChildNodes {        
        ^( not (children.isEmpty) ); // type boolean
    } // end hasChildNodes        


    // --- cloneNode(deep) : DOMNode ------------------------------------------
    //     
    cloneNode { arg deep; // type boolean        
        var node;
        var child;
        node =  switch ( this.getNodeType,
            // node_ATTRIBUTEs are handled internally by DOMElement
            node_CDATA_SECTION,
                DOMCDATASection.new,
            node_COMMENT,
                DOMComment.new,
            node_DOCUMENT_FRAGMENT,
                DOMDocumentFragment.new,
            node_DOCUMENT,
                DOMDocument.new,
            node_ELEMENT,
                DOMElement.new,
            node_PROCESSING_INSTRUCTION,
                DOMProcessingInstruction.new,
            node_TEXT,
                DOMText.new );
        node.init(this.getOwnerDocument, this.getNodeType, this.getNodeName, this.getNodeValue, startIndex, splitIndex, endIndex);
        //node.attributes = nil; // remains nil, DOMElements will have to re-parse their attributes
        if ( deep == true , {
            this.getChildNodes.do({ arg ch;
                child = ch.cloneNode(true);
                node.appendChild(child);
            });
        });
        ^node;
    } // end cloneNode        


    // --- hasAttributes() : boolean ------------------------------------------
    //    
    hasAttributes {        
        ^false; // default implementation for all classes except DOMElement
    } // end hasAttributes        


    // --- do(action) : void --------------------------------------------------
    //    
    do { arg action; // type Function        
        action.value( this ); // perform action
        this.getChildNodes.do( { arg child; 
            child.do(action);
        });
    } // end do        


    // --- collect(action) : List ---------------------------------------------
    //     
    collect { arg action; // type Function        
        var result = List.new;
        //result.add( action.value(this) );
        this.do({ arg node; // 'do' is full left-order traversal
            result.add( action.value(node) ); // perform action
        });
        ^result;
    } // end collect        


    // --- select(action) : List ----------------------------------------------
    //     
    select { arg action; // type Function        
        var result = List.new;
        this.do({ arg node; // 'do' is full left-order traversal
            if ( (action.value(node) == true) , {
                result.add(node);
            });
        });
        ^result;
    } // end select        


    // --- reject(action) : List ----------------------------------------------
    //     
    reject { arg action; // type Function        
        ^this.select( { arg n; not ( action.value(n) ) } ); // (I love the SC language...)
    } // end reject        


    // --- detect(action) : DOMNode -------------------------------------------
    //     
    detect { arg action; // type Function        
        var r;
        
        if( action.value(this) == true , {
            ^this;
        },{
            this.getChildNodes.do({ arg child; // 'do' is full left-order traversal
                r = child.detect(action); // recursion
                if( r != nil , {
                    ^r;
                });
            });
        });
        ^nil;
    } // end detect        


    // --- init(owner, type, name, value, start, split, end) :  ---------------
    //          
    init { arg owner, type, name, value, start, split, end; // types DOMDocument, int, String, String, int, int, int        
        ownerDocument = owner;
        nodeType = type;
        nodeName = name;
        nodeValue = value;
        // start, split, end are optional
        if ( start != nil , { startIndex = start });
        if ( split != nil , { splitIndex = split });
        if ( end != nil , { endIndex = end });
        if ( children == nil , { // (might be != nil if re-init during DOMDocument.importNode)
            children = List.new;
        });
    } // end init        


    // --- getNextSiblingOfChild(child) : DOMNode -----------------------------
    //     
    getNextSiblingOfChild { arg child; // type DOMNode        
        var i = children.indexOf(child);
        if ( (i < (children.size - 1)) , {
            ^children[ i + 1 ];
        },{
            ^nil;
        });
    } // end getNextSiblingOfChild        


    // --- getPreviousSiblingOfChild(child) : DOMNode -------------------------
    //     
    getPreviousSiblingOfChild { arg child; // type DOMNode        
        var i = children.indexOf(child);
        if ( i > 0 , {
            ^children[ i - 1 ];
        },{
            ^nil;
        });
    } // end getPreviousSiblingOfChild        


    // --- parse(parent, pos) : int -------------------------------------------
    //      
    parse { arg parent, pos; // types DOMNode, int
        "ABSTRACT".die; // simulate abstract method
    }


    // --- format(indentLevel) : String ---------------------------------------
    //     
    format { arg indentLevel; // type int
        "ABSTRACT".die; // simulate abstract method
    }


} // end DOMNode
