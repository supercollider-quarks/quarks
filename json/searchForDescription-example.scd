// get all gists (of a certain user) with a description containing a specific term
// a rudimentary example on how to use the interface to the gist cloud, hosted at github.
// if the following fails for you, please check if you have to update wslib's curl method by the one in 
//	json/extString-curl.scd


// 2011 Till Bovermann
// http://tangibleauditoryinterfaces.de 
// http://tai-studio.org 
// http://LFSaw.de 

q = q ? ();


a = Gist.allGistsFor("LFSaw", q.username, q.password).select{|gist|
	gist.description.contains("#scTip")
}

// print all descriptions
a.do(_.prettyprint)


// get content for one
b = a.first.pull

(
// post content of first .scd file
var title;

title = b.filenames.detect{|name| name.asString.contains(".scd")};
b.files[title]["content"].postcs; ""
)

(
// open all .scd files as new documents

b.filenames.select{|name| name.asString.contains(".scd")}.do{|title|
	Document.new("Gist(%): %".format(b.id, title), "// Gist(%)\n// by %\n// \"%\"\n// filename: %\n\n%".format(b.id, b.user["login"], b.description, title, b.files[title]["content"])).background_(Color(0.9, 0.9, 0.9, 1.0)).syntaxColorize
}
)


///////////////////// create a tip ///////////////
// write your tip into a separate document; 
// first line should be a line-comment ("// " ++ description ++ "#scTip") such as
// I am a dscription #scTip

q = ();

q.content = Document.allDocuments.detect{|doc| (doc.string.split($\n).first.contains("#scTip")) && {doc != Document.current && {doc.isListener.not}}}.string;


(
var username = "Gendy"; // set your user and pasword if you want to follow forks, edit, or let your successor be able to track your cadavre.
var password = "1number";

var content, description, contentDict;
var isPublic = true;


content = q.content.split($\n);
description = content.first[3..];
content = content[1..].join($\n);

// push as a new gist
contentDict = (
	'tip.scd': content
).postln;

g = Gist.createAndPush(description, contentDict, isPublic, username, password);

)

// look at it in the web browser
g.html_url.openInFinder

