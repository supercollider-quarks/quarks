// easier to grab the line where the cursor is placed
// av 8/12/07

+ Document {

	getLine { 
		var lineSize = 0, index = 0, actualLine ;
		var lineArr = this.string.split($\n) ;
		var incrementArr = lineArr.collect({|l| lineSize = lineSize+l.size+1}) ;
		while ({ incrementArr[index] <= this.selectionStart },
			{ index = index + 1 }) ;
		^actualLine = lineArr[index] ;
	}

}