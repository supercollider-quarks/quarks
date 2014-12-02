// For now, it is only possible to look for specific snippets... no pushing of new code... yet. :)

a = SCCode("1-f")
a.pull

a.dict.keys
a.labels

// this works in emacs, cocoa sc api. not in vi or gedit...
Document(a.name, a.code).syntaxColorize