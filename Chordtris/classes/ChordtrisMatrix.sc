ChordtrisMatrix {
	
	// twodimensional Array containing Colors or nil for blank cells of the matrix
	var <array;
	
	// number of columns in the matrix
	var <numColumns;
	
	// number of rows in the matrix
	var <numRows;
	
	// square size of matrix cells used for drawing
	var <squareSize;
	
	*new { |numRows, numColumns, squareSize| ^super.new.init(numRows, numColumns, squareSize) }
	
	init { |rows, cols, sqSize|
		numRows = rows;
		numColumns = cols;
		squareSize = sqSize;
		
		this.clear;
	}
	
	clear {
		array = nil ! numColumns ! numRows;
	}
	
	// checks for complete lines
	checkLines {
		var completeLines = 0;
		
	 	array.do { |row, index|
	 		var count = 0;
	 		row.do { |cell|
	 			if(cell.notNil) { count = count + 1};
	 		};
	 		
	 		if(count == numColumns)
	 		{
	 			completeLines = completeLines + 1;
	 			
	 			// delete the row
	 			row.do { |cell, ci|
	 				array[index][ci] = nil;
	 			};
	 			
	 			// make all the rows above go down
	 			(index..1).do { |i|
	 				array[i].do { |cell, ci|
	 					array[i][ci] = array[i-1][ci];
	 				}
	 			}
	 		};
	 	}
	 	
	 	^completeLines;
	 }
	 
	 // colors all cells of the given brick in its associated color
	 colorBrickCells { |brick|
		 brick.getParts.do { |item, i|
			if(this.cellIsInsideMatrix(item.x, item.y))
			{
				array[item.y][item.x] = brick.getColor;
			};
		}
	 }
	 
	 // deletes all cells of the given brick
	 deleteBrickCells { |brick|
		brick.getParts.do { |item, i|
			if(this.cellIsInsideMatrix(item.x, item.y))
			{
				array[item.y][item.x] = nil;
			}
		};
	}
	 
	 // returns whether the cell with the given coordinates is inside the matrix
	 cellIsInsideMatrix { |x,y|
		if(x < 0 or: { y < 0 }){ ^false };
		if(y > (numRows-1) or: { x > (numColumns-1) }){ ^false };
		
		^true
	}
	
	
	// draws the Matrix using a Pen
	// the result can be moved to another position by providing the parameters xOffset and yOffset
	draw { |xOffset=0, yOffset=0|
		array.do { |row, ri|
				row.do { |cell, ci|
					var rect;
					if(cell.notNil)
					{
						Pen.color = cell;
						rect = Rect(
							xOffset + (ci*squareSize),
							yOffset + (ri*squareSize),
							squareSize,
							squareSize);
						
						Pen.fillRect(rect);
						//Pen.fillAxialGradient(rect.leftTop, rect.rightBottom, cell, Color.black);
						
						Pen.color = Color.grey(0.2);
						Pen.strokeRect(rect);
						Pen.strokeRect(rect.insetBy(4));
						
					}
				}
			};
	}
}