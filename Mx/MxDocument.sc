

MxDocument {
    /*
        this is just a container for an SC code Document
        either as a path or as a string.

        a Document in sc can only exist if the document window is open,
        so this container allows it to be evaluated even from disk
        or if being edited by another app
    */

    var >path, >content;
    var document;

    *new { arg path, content;
        ^super.newCopyArgs(path, content)
    }
    eval {
        if(document.notNil, {
            ^document.text.compile.value()
        });
        if(path.notNil, {
            ^Document.standardizePath(path).load
        });
        if(content.notNil and: {content != ""}, {
            ^content.compile.value()
        })
    }
    path {
        if(document.isNil, { ^path });
        ^Document.abbrevPath(document.path)
    }
    content {
        if(document.isNil, { ^content }); // maybe load
        ^document.text
    }
    name {
        var p;
        if(document.isNil, {
            p = this.path;
            if(p.notNil, {
                ^PathName(p).fileName
            });
            ^((content?"").copyRange(0, 10) ++ "...")
        });
        ^document.name
    }
    printOn { arg stream;
        stream << "Document:";
        stream << (this.name ?? {this.path})
    }
    storeArgs {
        var c, p = this.path;
        if(p.isNil, {
            c = this.content
        }, {
            p = Document.abrevPath(p)
        });
        ^[p, c]
    }

    gui { arg parent, bounds;
        if(document.notNil, {
            ^document.front
        });
        if(path.notNil, {
            document = Document.open(Document.standardizePath(path), envir:currentEnvironment)
        }, {
            document = Document(string:content ? "", envir:currentEnvironment)
        });
        document.onClose = {
            path = document.path;
            if(path.isNil, {
                content = document.text
            });
            document = nil;
        };
        // later will support gui in a text area
    }
}
