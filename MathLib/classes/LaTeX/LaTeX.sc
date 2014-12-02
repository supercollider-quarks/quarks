LaTeX {
	
	*lineEnd { ^"  \\\\  \n" }
	*hline { ^"  \\hline  " }
	*cline { arg which; ^which.collect { |x|"\\cline{" ++ x ++ "-" ++ x ++ "}" }.join(" ") }
	*tabletab { ^"\t&\t" }
	
	*math { arg str, flag=true;
		^if(flag) { "$" ++ str ++ "$" }{ str };
	}
	
	*row { arg obj, align="l", n=1;
		if(n > 1) {
			^ "\\multicolumn{" ++ n ++ "}{" ++ align ++ "}{" ++ obj ++ "}";
		};
		^obj.asString
	}
	
	*asTable { arg dict, func;
		var res;
		if(dict.isKindOf(Dictionary)) {
			dict.keysValuesDo { |key, val|
				res = res.add([key, val]);
			}
		} {
			^dict
		};
		^res
	}
	
	*tabularFooter { ^"\\end{tabular}\n" }
	*tableHeader { arg n, align="l", separator=" ";
		^ "\\begin{tabular}{" ++ (align).dup(n).join(separator) ++ "}" ++ this.lineEnd;
	}
	*tableDict { arg dict,  hlines=#[], align="l", separator=" ", math=true;
		var str, headline;
		dict = this.asTable(dict);
		str = this.tableHeader(dict.shape.at(1), align, separator);
		dict.do {|x, i|
			if(hlines.includes(i)) {str = str ++ this.hline };
			if(math) {x = x.collect(this.math(_)) };
			str = str ++ x.collect(this.row(_), align).join(this.tabletab);
			str = str ++ this.lineEnd;
		};
		str = str ++ this.tabularFooter;
		^str
	}
	
	*numericalDict { arg dict, keyName, valName, math=true;
		var str;
		str = "\\begin{tabular}{c r @{.} l}";
		if(keyName.notNil or: {valName.notNil }) {
			str = str + this.math(keyName, math) + "&" 
					++ this.row(this.math(valName, math), "c", 2) ++ this.lineEnd;
			str = str ++ this.hline 
		} {
			str = str ++ this.lineEnd;
		};
		dict.pairsDo {|key, val|
			var valStr = if(val.isNumber) {
				 	val.asString.split($.).join($&);
				 } {
					this.row(this.math(val, math), "l",  2);
			};
			str = str ++ this.row(this.math(key, math), "l", 1) ++ this.tabletab ++ valStr;
			str = str ++ this.lineEnd;
			
		};
		str = str ++ this.tabularFooter;
		^str
	}
	
	// Give it an array of (barlabel -> barvalue) Association items. 
	// "shades" can be true to cycle through shades, or false, or a function that translates the bar's index to a shade number (numbers 1 through 8).
	// LaTeX.barChart({100.0.rand}.dup(10).collect{|item, index| (("Item"+index) -> item) }, precision: 3)
	*barChart { |vals, min=0, max, interval, shades=false, precision, extras|
		var str = "", tmp, precisionfunc;
		
		precisionfunc = if(precision.isNil){
			{|val| val}
		}{
			{|val| val.asStringPrec(precision.asInteger)}
		};
		
		if(min.isNil){ min = vals.collect(_.value).minItem; min = min.roundUp(min* -0.1)};
		if(max.isNil){ max = vals.collect(_.value).maxItem; max = max.roundUp(max*  0.1)};
		if(interval.isNil){ interval = (max - min) / 10};
		if(extras.isNil){ extras = "" };
		
		str = str ++ "\\begin{barenv}"++extras++"\n\\setyaxis{"++min++"}{"++max++"}{"++interval++"}\n";
		
		shades = shades.switch(
			true, 
			{{|index| index.wrap(0, 7)+1}},
			false,
			{1},
			// default, return the user-specified function (or number, or whatever):
			shades
		);
		
		vals.do{|assoc, index|
			str = str ++ "\\bar{"++precisionfunc.value(assoc.value)++"}{"++shades.value(index)++"}["++assoc.key++"]\n";
		};
		
		str = str ++ "\\end{barenv}\n";
		^str;
	}
}

/*
cline

\begin{tabular}{l l l l }  \\  $0$	&$1$	&$2$	&$3$  \\    \hline  $4$	&$5$ 	&$6$	&$7$  \\  
\cline{1-1} \cline{3-4}$8$	&$9$	&$10$	&$11$  \\    \hline  $12$	&$13$	&$14$	&$15$  \\  \end{tabular}
*/

