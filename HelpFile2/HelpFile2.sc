HelpFile2 : File{
	var	<examples, <instanceMethods, <classMethods;
	var <classname;
	var <>shortDescription;
	var <>longDescription;

	*new { arg name, pathName;
		pathName = pathName ?? (name++".html");
		^super.new(pathName, "w").init(name);
	}

	init{ |name|
		examples = Array.new;
		instanceMethods = IdentityDictionary.new;
		classMethods = IdentityDictionary.new;
		if ( name.notNil ){ this.classname = name };
	}

	classname_{ |name|
		classname = name;
		this.createClassMethods;
		this.createInstanceMethods;
	}

	createDoc{ |varname|
		var doc = Document.new;
		var str = "";
		str = str ++ varname ++ ".shortDescription_( \"  \" );\n";
		str = str ++ varname ++ ".longDescription_( \"  \" );\n";
		// class descriptions:
		classMethods.keysValuesDo{ |key,it,i|
			str = str ++ varname ++ ".addDescription( " ++ key.asCompileString ++ ", \"  \" , 'class' );\n";
			if ( it[0].size > 1, {
				it[0].copyToEnd( 1 ).do{ |jt,j|
					str = str ++ varname ++ ".addDescription( " ++ key.asCompileString ++ ", \"  \" , 'classArg', " ++ jt.asCompileString ++  ");\n";
				};
			});
			str = str ++ "\n";
		};
		// instance descriptions:
		instanceMethods.keysValuesDo{ |key,it,i|
			str = str ++ varname ++ ".addDescription( " ++ key.asCompileString ++ ", \"  \" );\n";
			if ( it[0].size > 1, {
				it[0].copyToEnd( 1 ).do{ |jt,j|
					str = str ++ varname ++ ".addDescription( " ++ key.asCompileString ++ ", \"  \" ,'instArg', " ++ jt.asCompileString ++  ");\n";
				};
			});
			str = str ++ "\n";
		};

		str = str ++ varname ++ ".addExample( \"  \" );\n";

		str = str ++ varname ++ ".addExampleFromFile( \"  \" );\n";

		str = str ++ varname ++ ".writeToFile;\n";

		str = str ++ varname ++ ".close;\n";

		doc.string = str;
		doc.front;
	}

	addDescription{ |key,desc,type=\instance,key2|
		switch ( type,
			\instance, { instanceMethods.at(key).put(1, desc )},
			\class,    { classMethods.at(key).put(1, desc );},
			\instArg,  { 
				if ( instanceMethods.at(key).at(2).isNil){
					instanceMethods.at(key).put(2, IdentityDictionary.new);
				};
				instanceMethods.at(key).at(2).put( key2, desc );
			},
			\classArg, {
				if ( classMethods.at(key).at(2).isNil){
					classMethods.at(key).put(2, IdentityDictionary.new);
				};
				classMethods.at(key).at(2).put( key2, desc );
			}
		);
	}

	addExample{ arg examplecode;
		examples = examples.add( examplecode );
	}

	addExampleFromFile{ arg filename;
		var exampleFile = File.open( filename, "r");
		examples = examples.add( exampleFile.readAllString; );
	}

	writeToFile{
		this.writeHeader;
		// title + description:
		this.write( "<h1>"++classname++"</h1>\n<h2>"++shortDescription++"</h2>\n" );
		// inheritance:
		this.write( "<p><em>Inherits from:</em>" );
		classname.asSymbol.asClass.superclasses.reverseDo{ |it| 
			this.write( ": <strong>" );
			this.write( it.name );
			this.write( "</strong> " );
		};
		this.write( "</p>\n\n" );
		// long description:
		this.write( "<p>"++longDescription++"</p>\n\n" );
		this.writeClassMethods;
		this.writeInstanceMethods;
		this.writeExamples;
		this.writeFooter;
	}

	writeExamples{
		examples.do{ |it,i|
			this.write( "<h3> Example" + (i+1) + "</h3>\n" );
			this.write( it );
		};
	}

	createClassMethods{
		var class;
		class = classname.asSymbol.asClass;
		class.class.methods.asArray.do{ |it| 
			classMethods.put( it.name, 
				[ it.argNames, "", nil ] ); };

	}

	createInstanceMethods{
		var class;
		class = classname.asSymbol.asClass;
		class.methods.asArray.do{ |it| 
			instanceMethods.put( it.name, 
				[ it.argNames, "", nil ] ); };
	}

	writeArgDesc{ |argsDict|
		this.write( "<DL>\n");
		argsDict.keysValuesDo{ |key,it,i|
			this.write( "<DT><EM>" );
			this.write( key );
			this.write( "</EM></DT>\n" );
			this.write( "<DD>" );
			this.write( it );
			this.write( "</DD>\n" );
		};
		this.write("</DL>\n\n");		
	}

	writeClassMethods{
		this.write( "<h3>Creation / Class Methods</h3>\n<DL>\n\n");
		classMethods.keysValuesDo{ |key,it,i|
			this.write( "<DT><STRONG>*" );
			this.write( key );
			if ( it[0].size > 1, {
				this.write( "(" );
				it[0].copyToEnd( 1 ).do{ |jt,j|
					this.write(jt);
					if ( j < (it[0].copyToEnd(1).size-1),{
						this.write(","); })
				};
				this.write( ")" );
			});
			this.write( "</STRONG></DT>\n" );
			this.write( "<DD>" );
			this.write( it[1] );
			if ( it[2].notNil ){
				this.writeArgDesc( it[2] );
			};
			this.write( "</DD>\n\n" );
		};
		this.write("</DL>\n\n");
	}

	writeInstanceMethods{
		this.write( "<h3>Accessing Instance and Class Variables</h3>\n<DL>\n\n");
		instanceMethods.keysValuesDo{ |key,it,i|
			this.write( "<DT><STRONG>" );
			this.write( key );
			if ( it[0].size > 1, {
				this.write( "(" );
				it[0].copyToEnd( 1 ).do{ |jt,j|
					this.write(jt);
					if ( j < (it[0].copyToEnd(1).size-1),{
						this.write(","); })
				};
				this.write( ")" );
			});
			this.write( "</STRONG></DT>\n" );
			this.write( "<DD>" );
			this.write( it[1] );
			if ( it[2].notNil ){
				this.writeArgDesc( it[2] );
			};
			this.write( "</DD>\n\n" );
		};
		this.write("</DL>\n\n");
	}

	writeFooter{
		this.write( "<hr><p>This helpfile was created with the class HelpFile2</p>\n </body></html>\n");
	}

	writeHeader{
		this.write(
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">
	<html>
	<head>
	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">
	<meta http-equiv=\"Content-Style-Type\" content=\"text/css\">
	<title>SuperCollider helpfile: " ++ classname ++ "</title>
	<meta name=\"Generator\" content=\"SC HelpFile Writer\">

    <style type=\"text/css\">
    <!--
      body {
        color: #000000;
        background-color: #ffffff;
      }
      .comment {
        /* font-lock-comment-face */
        color: #b22222;
      }
      .comment-delimiter {
        /* font-lock-comment-delimiter-face */
        color: #b22222;
      }
      .constant {
        /* font-lock-constant-face */
        color: #5f9ea0;
      }
      .string {
        /* font-lock-string-face */
        color: #bc8f8f;
      }
      .type {
        /* font-lock-type-face */
        color: #228b22;
      }
      .variable-name {
        /* font-lock-variable-name-face */
        color: #b8860b;
      }

      a {
        color: inherit;
        background-color: inherit;
        font: inherit;
        text-decoration: inherit;
      }
      a:hover {
        text-decoration: underline;
      }
    -->
    </style>

	</head>\n\n";
		);
	}
}