// original by sc.solar 25.09.01.21
// extended by till bovermann
// 	bielefeld unversity 2006

// inserted Christofer Fraunberger's code for matrix class Multiplication for compatibility reasons.
// additional methods dan stowell 2008

Matrix[slot] : Array {

	*newClear { arg rows=1, cols=1; // return (rows x cols) - zero matrix 
		^super.fill(rows, { Array.newClear(cols).fill(0) });
	}
	*with { arg array; // return matrix from 2D array (array of rows)
		var rows;
		if((array.flat == array.flop.flop.flat)
		.and(array.flatten.every({arg element; element.isNumber})) ,{
			rows = array.size;
			^super.fill(rows, {arg col; array.at(col) });
		},{
			error("wrong type of argument in Meta_Matrix-with");this.halt
		});
	}
	*withFlatArray { arg rows, cols, array; // return (rows x cols) - matrix from one slot array
		if((array.size == (rows*cols) )
		.and(array.every({arg element; element.isNumber})) ,{
			^super.fill(rows, {arg col; array.copyRange(col*cols, (col+1)*cols -1) });
		},{
			error("wrong type of argument in Meta_Matrix-withFlatArray");this.halt
		});
	}
	*newIdentity { arg n; // return an (n x n) identity matrix
		var ident;
		ident = super.fill(n, { Array.newClear(n).fill(0) });
			n.do({ arg i; ident.put(i,i,1) });
		^ident;
	}
	*fill { arg rows, cols, func;
		var matrix;
		matrix = this.newClear(rows, cols);
		rows.do({ arg row;
			cols.do({ arg col;
				matrix.put(row, col, func.value(row, col))
			});
		});
		^matrix
	}

	/* Class method: *mul
	   Matrix multiplication
	   Parameter: 
	   	m1: Matrix 1
	   	m2: Matrix 2
	   Return:
	   	out = m1 * m2
	*/
	*mul { arg m1=0, m2=0;
		
		var m2s;
		var out, size;
		
		// TODO: do some checking here whether multiplication is possible	
		size = m1.size;
		out = Array.new(size);
		size.do { out.add(Array.new(size)); };
		
		// swap the m2 Matrix (rows -> columns)
		m2s = m2.flop;
		
		// multiplication
		m1.do { arg row, i;
			size.do { arg u;
				out.at(i).add((row * m2s.at(u)).sum);
			};
		};
		^out;	
	}

	rows {
		^this.size;
	}
	cols {
		^super.at(0).size;
	}
	shape {
		^([this.rows, this.cols]);
	}
	printOn { | stream |
		if (stream.atLimit) { ^this };
		stream << this.class.name << "[ " ;
		this.printItemsOn(stream);
		stream << " ]" ;
	}
	printItemsOn { | stream |
		this.do { | item, i |
			if (stream.atLimit) { ^this };
			if (i != 0) { stream.comma; };
			stream << "\n";
			stream.tab;
			item.printOn(stream);
		};
		stream << "\n";
	}

	postmln {
		this.do({ arg row;
			row.do({ arg item; item.post; "\t".post });
			"\n\n".post;
		});
	}
	// single elements
	at { arg row, col; 
		^super.at(row).at(col); 
	}
	get { arg row, col; // same as at
		^super.at(row).at(col); 
	}
	put { arg row, col, val; 
		if( val.isNumber,{
			super.at(row).put(col, val) 
		},{
			error("not a number in Matrix-put");this.halt
		});
	// put an Array of elements
	}
	putRow { arg row, vals;
		if(vals.size == this.cols,{
			vals.size.do({ arg col;
				this.put(row, col, vals.at(col)) 
			})
		},{
			(vals.size).min(this.cols).do({ arg col;
				this.put(row, col, vals.at(col))
			});"Warning: wrong number of vals".postln;
		})
	}
	putCol { arg col, vals;
		if(vals.size == this.rows,{
			vals.size.do({ arg row;
				this.put(row, col, vals.at(row)) 
			})
		},{
			(vals.size).min(this.rows).do({ arg row;
				this.put(row, col, vals.at(row))
			});"Warning: wrong number of vals".postln;
		});
	}
	
	fillRow { arg row, func; 
		this.cols.do({ arg col; 
			this.put(row, col, func.value(row, col)) 
		}) 
	}
	fillCol { arg col, func; 
		this.rows.do({ arg row; 
			this.put(row, col, func.value(row, col)) 
		}) 
	}	
	exchangeRow { arg posA, posB;
		var rowA;
		rowA = this.getRow(posA).copy;
		this.putRow(posA, this.getRow(posB) );
		this.putRow(posB, rowA);
	}
	exchangeCol { arg posA, posB;
		var colA;
		colA = this.getCol(posA).copy;
		this.putCol(posA, this.getCol(posB) );
		this.putCol(posB, colA);
	}
	collect { arg func;
		var res; res = Matrix.newClear(this.rows, this.cols);
		this.rows.do({ arg row;
			this.cols.do({ arg col;
				res.put(row, col, func.value(this.at(row, col), row, col))
			})
		});
		^res;
	}

	addRow { arg rowVals;
		var res;
		if( (rowVals.flat.size == this.cols)
		.and(rowVals.every({arg element; element.isNumber}) ),{
			res = this.copy;
			res = res.add(rowVals);
			^res},{
			error("wrong type or size in Matrix-addRow");this.halt;
		});
	}
	insertRow { arg col, rowVals;
		var res;
		if( (rowVals.flat.size == this.cols)
		.and(rowVals.every({arg element; element.isNumber}) ),{
			res = this.copy;
			res = res.insert(col, rowVals);
			^res},{
			error("wrong type or size in Matrix-addRow");this.halt;
		});
	}
	addCol { arg colVals;
		var res;
		if( (colVals.flat.size == this.rows)
		.and(colVals.every({arg element; element.isNumber}) ),{
			res = Matrix.newClear(this.rows, this.cols+1);
			res.rows.do({ arg row;
				var rowArray;
				rowArray = this.getRow(row).copy;
				rowArray = rowArray.add(colVals.at(row));
				res.putRow( row, rowArray);
			});
			^res},{
			error("wrong type or size in Matrix-addCol");this.halt;
		});
	}
	insertCol { arg row, colVals;
		var res;
		if( (colVals.flat.size == this.rows)
		.and(colVals.every({arg element; element.isNumber}) ),{
			res = Matrix.newClear(this.rows, this.cols+1);
			res.rows.do({ arg row;
				var rowArray;
				rowArray = this.getRow(row).copy;
				rowArray = rowArray.insert(row, colVals.at(row));
				res.putRow( row, rowArray);
			});
			^res},{
			error("wrong type or size in Matrix-addCol");this.halt;
		});
	}
	// returns arrays
	getRow { arg row;
		^super.at(row);	
	}
	getCol { arg col;
		var res;
		res = Array.new;
		this.rows.do({ arg row;
			res = res.add( this.at(row, col) )
		});
		^res;	
	}
	getDiagonal {
		var diagonal;
			diagonal = Array.new;
			(this.rows).min(this.cols).do({arg i; 
				diagonal = diagonal.add(this.at(i,i));
			});
			^diagonal;
	}
	// returns matrizes
	fromRow { arg row;
		^Matrix.with([this.getRow(row)]);
	}
	fromCol { arg col;
		^Matrix.with([this.getCol(col)]).flop;
	}
	// returns matrix without row(row)/col(col)
	removeRow { arg row; 
		var array;
			array = Array.new;
			this.rows.do({ arg i;
				if( i != row, { array = array.add( this.getRow(i) ) });
			});
		^Matrix.with(array);
	}
	removeAt { arg row; ^this.removeRow(row); }
	removeCol { arg col;	
		^this.flop.removeRow(col).flop;	
	}
	
	doRow { arg row, func;
		this.getRow(row).do({ arg item,i;
			func.value(item, i);
		});
	}
	doCol { arg col, func;
		this.rows.do({ arg i;
			var item;
			item = this.at(i, col);
			func.value(item, i);
			})
	}
	doMatrix { arg function;
		this.rows.do({ arg row;
			this.cols.do({ arg col;
				function.value(this.at(row, col), row, col)
			});
		});
	}
	
	sub { arg row, col; // return submatrix to element(row,col)
		^this.removeRow(row).removeCol(col);
	}
	asArray { var array; array = Array.new; // return an array of rows
		this.rows.do({ arg i; array = array.add(this.getRow(i)) });
		^array;
	}
	flat { var array; array = Array.new; // return an array with all elements in one slot
		this.rows.do({ arg i; array = array.add(this.getRow(i)) });
		^array.flat;
	}
	flatten { ^this.flat; } 
	flop { // return transpose matrix with rows cols and cols rows
		var flopped;
		flopped = Matrix.newClear(this.cols, this.rows);
		this.rows.do({ arg i;
			flopped.putCol(i, this.getRow(i))
		});
		^flopped
	}
	
	
	
	
	
	// math
	mul { arg multplier2; // return matrix A(m,n) * B(n,r) = AB(m,r)
		var result;
		if ( multplier2.isNumber, {
			^this.mulNumber(multplier2);
		},{ if( this.rows == multplier2.cols, {
			result = Matrix.newClear(this.cols, multplier2.rows);
			this.cols.do({ arg j;
				multplier2.rows.do({ arg i;
					result.put(i, j, ( 
						multplier2.getRow(i) * (this.getCol(j)) 
					).sum );
				});
			});
			^result
		},{
			error("cols and rows don't fit in Matrix-*");
			this.dumpBackTrace;
			this.halt;
		})});
	}
	
	
	* { arg that; // return matrix A(m,n) * B(n,r) = AB(m,r)
		var result;
		
		if ( that.isNumber, {
			^this.mulNumber(that);
		},{ 
			^this.mulMatrix(that);
		});
	}
	
	mulMatrix { arg aMatrix;
		var result;
		
		if( this.cols == aMatrix.rows, {
			result = Matrix.newClear(this.rows, aMatrix.cols);
			
			this.rows.do({ arg rowI;
				aMatrix.cols.do({ arg colI;
					result.put(
						rowI, colI,
						(this.getRow(rowI) * aMatrix.getCol(colI)).sum;
					);
				});
			});
			^result
		},{
			error("Matrix-mulMatrix: cols and rows don't fit. Matrix shapes: (%) (%)".format(this.shape, aMatrix.shape));
			this.dump;
			this.dumpBackTrace;
			this.halt;
		});
	}
	
	
	mulNumber { arg aNumber;
		^(super * aNumber);	
	}
	+ { arg summand2;
		var result;
		if ( summand2.isNumber, {^this.addNumber(summand2);},{
			if( this.shape == summand2.shape,{
				result = Matrix.newClear(this.rows, this.cols);
				this.rows.do({ arg i;
					this.cols.do({ arg j;
						result.put(i, j, (this.at(i,j) + summand2.at(i,j)) );
					});
				});
				^result;
			},{error("sizes don't fit in Matrix-plus");this.halt;});
		});
	}
	addNumber { arg aNumber;
		^(super + aNumber);
	}
	- { arg summand2;
		var result;
		if ( summand2.isNumber, {^this.subNumber(summand2);},{
			if( this.shape == summand2.shape,{
				result = Matrix.newClear(this.rows, this.cols);
				this.rows.do({ arg i;
					this.cols.do({ arg j;
						result.put(i, j, (this.at(i,j) - summand2.at(i,j)) );
					});
				});
				^result;
			},{error("sizes don't fit in Matrix-plus");this.halt;});
		});
	}
	subNumber { arg aNumber; 
		^(super - aNumber);
	}
	== { arg matrix2;
		^(this.flat == matrix2.flat);
	}
	det { // return the determinant as float
		var elements, detSum = 0; 
		if( this.rows == this.cols,{
			if( (this.rows * this.cols) == 1,{^this.at(0,0)},{
				elements = this.getRow(0);
				elements.size.do({ arg i; var sub;
					detSum = detSum + ( elements.at(i) * ((-1) ** (i)) * this.sub(0,i).det );
				});
				^detSum;
			});
		},{error("matrix not square in Matrix-det");this.halt;});
	}
	cofactor { arg row, col; // return the cofactor to element (row, col)
		if( this.rows == this.cols,{
			^( ((-1) ** (row+col)) * this.sub(row, col).det)
		},{error("matrix not square in Matrix-cofactor");this.halt;});
	}
	adjoint { // return the adjoint of the matrix
		var adjoint;
		adjoint = Matrix.newClear(this.rows, this.cols);
		this.rows.do({ arg i;
			this.cols.do({ arg j; 
				adjoint.put(i,j,this.cofactor(i,j));
			});
		});
		^adjoint.flop;
	}
	inverse { // return the inverse matrix
		if(this.det != 0.0,{
			^(this.adjoint / (this.det) );
			},{error("matrix singular in Matrix-inverse");this.halt;}); 
	}
	gram { // the gram matrix
		^(this.flop * this);
	}
	grammian { // the grammian of a matrix
		^(this.gram.det);
	}
	pseudoInverse {
		if( this.cols < this.rows ,{
			^(this.gram.inverse * (this.flop));
		},{
			^(this.flop * (this * this.flop).inverse );
		});
	}
	trace {
		if( this.rows == this.cols, {
			^this.getDiagonal.sum;
			},{error("matrix not square in Matrix-trace");this.halt;
		});
	}
	norm { // the euclidean norm
		^(this * (this.flop)).trace.sqrt;
	}
	sum {
		^super.sum.sum;
	}
	sumRow { arg row;
		^this.getRow(row).sum;
	}
	sumCol { arg col;
		^this.getCol(col).sum;
	}
	// testing
	isSquare {
		^(this.rows == this.cols);
	}
	isSingular {
		^(this.det == 0.0);
	}
	isRegular {
		^(this.det != 0.0);
	}
	isSymmetric {
		^(this.flat == this.flop.flat);
	}
	isAntiSymmetric {
		^(this.flat == this.flop.flat.neg);
	}
	isPositive {
		^this.flat.every({arg element; element.isStrictlyPositive});
	}
	isNonNegative {
		^this.flat.every({arg element; element.isPositive});
	}
	isNormal {
		^((this * (this.flop)).flat == (this.flop * this).flat);
	}
	isZero {
		^this.flat.every({arg element; element == 0});
	}
	isIntegral {
		^(this.flat.frac.every({arg element; element == 0}));
	}
	isIdentity {
		if( this.rows == this.cols,{
			^(this.every({arg row,i; 
				row.every({ arg colElement,j;
					if( i == j ,{
						(colElement == 1)
					},{
						(colElement == 0)
					}) 
				});
			});)
		},{error("matrix not square in Matrix-isIdentity");this.halt;});
	}
	isDiagonal { 
		^(this.every({arg row,i; 
			row.every({ arg colElement,j;
				if( i == j ,{
					(colElement != 0)
				},{
					(colElement == 0)
				}) 
			});
		});)
	}
	isOrthogonal {
		^(this.flop * this).isIdentity;
	}
	isIdempotent {
		^(this.squared.flat == this.flat);
	} 

	sumCols { |function|
		^this.asArray.sum(function)
	}
	sumRows { |function|
		^this.asArray.collect{|row| row.sum(function) }
	}
	// mean/center/cov/covML uses the convention that rows are data points, cols are variables
	mean {
		^this.asArray.mean
	}
	center { |mean|
		if(mean.isNil){ mean = this.mean };
		^this - this.class.with(this.asArray.mean.dup(this.rows))
	}
	cov { |mean|
		var centred;
		centred = this.center(mean);
		^(centred.flop * centred) / (this.rows - 1)
	}
	covML { |mean|
		var centred;
		centred = this.center(mean);
		^(centred.flop * centred) / (this.rows)
	}
}


// suggestions, bugs, improvements to sc.solar@studiobeige.de
// kicked instance var rows,cols and method init
// added addRow, addCol
// renamed method square --> norm
// testing isSquare, isSingular, isRegular
// added sum, sumRow, sumCol
// replaced binary and unary operators
// added shape 
// added plus
// added isSymmetric, isAntiSymmetric
// added isPositive, isNonNegative
// added isNormal
// added getDiagonal
// trace dependent on getDiagonal
// added get(row, col) equal at(row, col)
// added isIdentity, isDiagonal, isOrthogonal, isIdempotent
// added isIntegral
// added equals, gram, grammian, psydoInverse
// changed isDiagonal and isIdentity
// restriction for multi
// renamed equals to ==
// renamed plus to +; --> addNumber
// added addNumber
// renamed multi to *; --> mulNumber
// added mulNumber
// added -; --> subNumber
// added subNumber
// flatten = flat
// removeAt = removeRow
// restriction for psydoInverse
// psydoInverse --> pseudoInverse
// pseudoInverse also for rows < cols
// changed grammian 
// fill as class method and with function
// fillCol and fillRow with function
// added  doRow, doCol, doMatrix
// added exchangeRow, exChangeCol
// added postmln
// added collect 
// changed addRow, addCol (reciever unchanged, no flop)
// changed getCol (no flop)
// added inserRow, insertCol
// getDiagonal also for nonsquare matrices
// trace gets square restriction from getDiagonal

