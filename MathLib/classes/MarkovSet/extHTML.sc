
+MarkovSet {
	writeHtml { arg argPath="supermarkov", nPages=5, pagelength=60, style;
			style = style ?? ();
			style.parent =
			(
				backcolor: #{Color.new255(171, 146, rrand(100, 200)) },
				textcolor: Color.black,
				linkcolor: Color.new255(128, 116, 54),
				vlinkcolor: Color.new255(197, 182, 25),
				alinkcolor: Color.new255(23, 254, 54)
			);
			nPages.do { arg i;
				var scream, pat, file, string, path, linkpath, nx;
				path = argPath ++ i.asString ++ ".html";
				file = File(path, "w");
				pat = Pfsm2(this, inf);
				scream = pat.asStream;
					
				string = String.streamContents({ |stream|
					stream << "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">";
					stream.nl;
					stream << "<\meta name=\"generator\" content=\"Supermarkov\">";
					stream.nl;
					stream.nl;
   
					stream << "<html>"; stream.nl;
								
					stream << "<head>"; stream.nl;
					stream << "<title> Supermarkov </title>"; stream.nl;
					stream << "</head>"; stream.nl;
					stream <<  "<body text="
					
					<<< style[\textcolor].value(stream, i).asHtml
					<< " bgcolor="
					<<< style[\backcolor].value(stream, i).asHtml
					<< "link="
					<<< style[\linkcolor].value(stream, i).asHtml
					<< "vlink="
					<<< style[\vlinkcolor].value(stream, i).asHtml
					<< "alink="
					<<< style[\alinkcolor].value(stream, i).asHtml
					<< ">";
					stream.nl;
					10.do { stream << "<br>" };
					stream << "<span width=250\">";      //"
					stream << " ... ";
						pagelength.do { var word;
							word = scream.next.asString;
							if(0.1.coin)
							{ 
							//avoid links to the same page
							while { nx = nPages.value.asInteger.rand; nx == i }; 
							linkpath =  argPath ++ nx.asString ++ ".html";
							stream << ("<a href=\" " ++ linkpath ++ "\">" ++ word ++ "</a>") 
							}
							{ stream << word };
							if(word[word.size-2] === $. and: {0.4.coin}) { stream << "<br>" };
							stream << " "; 
						};
						
						stream.nl;
						stream << "</span>";
						stream.nl;
						stream << "</body>"; stream.nl;
						stream << "</html>"; stream.nl;
						
					});
					
					file.putString(string);
					
					file.close;
			}
	}
}


+ Color {
	asHtml {
		^"#" ++ [red, green, blue].collect { |u| (u * 255).round.asInteger.asHexString(2) }.join
	}

}


