ChordtrisBrick {
	
	// Symbol representing the different Tetris bricks I, J, L, O, S, T and Z 
	var <>brickType;
	
	// associated chord
	var <>chord;
	
	// position
	var <>x;
	var <>y;
	
	// orientation of the brick, can be 0, 1, 2 or 3
	var <>orientation = 0;
	
	*new { |brickType, chord, x, y, orientation| ^super.newCopyArgs(brickType, chord, x, y, orientation) }
	
	// returns the color associated with the current brick
	getColor {
		var color;
		
		switch (brickType,
		
			\i, {
				// green
				color = Color.new(0.1, 0.9, 0.1);
			},
			\j, {
				// yellow
				color = Color.new(0.9, 0.9, 0.2);
			},
			\l, {
				// brown
				color = Color.new(0.6, 0.35, 0.25);
			},
			\o, {
				// red
				color = Color.new(0.8, 0.1, 0.1);
			},
			\s, {
				// blue
				color = Color.new(0.1, 0.1, 0.8);
			},
			\t, {
				// cyan
				color = Color.new(0.1, 0.8, 0.8);
			},
			\z, {
				// magenta
				color = Color.new(0.8, 0.2, 0.8);
			}
		);
		
		^color;
		
	}
	
	
	
	goRight { |matrix|
		// check if we can go right at all
		this.getParts.do { |part|
			if(part.isOuterRightPart)
			{
				// check if we are already on the outer right edge
				if(part.x >= (matrix.numColumns-1) ){ ^this };
				
				// check if there is a brick right of this brick
				if(matrix.cellIsInsideMatrix(part.x+1, part.y))
				{
					if(matrix.array[part.y][part.x+1].notNil) { ^this };
				}
			}
		};
		matrix.deleteBrickCells(this);
		x = x+1;
	}
	
	goLeft { |matrix|
		// check if we can go left at all
		this.getParts.do { |part|
			if(part.isOuterLeftPart)
			{
				// check if we are already on the outer left edge
				if(part.x <= 0 ){ ^nil };
				
				// check if there is a brick left of this brick
				if(matrix.cellIsInsideMatrix(part.x-1, part.y))
				{
					if(matrix.array[part.y][part.x-1].notNil) { ^this };
				}
			}
		};

		matrix.deleteBrickCells(this);
		x = x-1;
	}
	
	rotateRight { |matrix|
		var newOrientation = (orientation+1).wrap(0,3);
		this.rotateIfPossible(newOrientation, matrix);
	}
	
	rotateIfPossible { |newOrientation, matrix|
		var rotatedBrick = ChordtrisBrick(this.brickType, this.chord, this.x, this.y, newOrientation);
		matrix.deleteBrickCells(this);
		if(rotatedBrick.fitsInMatrix(matrix))
		{
			orientation = newOrientation;
		};
	}
	
	rotateLeft { |matrix|
		var newOrientation = (orientation-1).wrap(0,3);
		this.rotateIfPossible(newOrientation, matrix);
	}
	
	fall { |matrix|
		while( { this.canGoDown(matrix) }, {
			this.goDown(matrix);
		});
	}
	
	fitsInMatrix { |matrix|
		this.getParts.do { |part, i|
			if(matrix.cellIsInsideMatrix(part.x, part.y))
			{
				if(matrix.array[part.y][part.x].notNil) { ^false };
			}
			{
				// we consider parts with y < 0 as in the matrix
				// this can happen when a new brick comes in
				if(part.y >= 0) { ^false };
			};
		}
		^true;
	}
	
	// lets the brick go down one matrix unit if possible.
	// It it hits the ground, false is returned, otherwise true.
	goDown { |matrix|
		if(this.canGoDown(matrix))
		{
			// delete the old matrix cells
			matrix.deleteBrickCells(this);
			y = y+1;
			^true;
		}
		{
			^false
		};
	}
	
	// checks if this brick can go one step down in the matrix
	canGoDown { |matrix|
		this.getParts.do { |part|
			// check if all lower parts have an empty cell belowq
			if(part.isLowerPart)
			{
				if(part.y >= (matrix.numRows-1)){ ^false };
				if(matrix.array[part.y+1][part.x].notNil) { ^false };
			};						
		};
		
		^true;
	}
	
	getParts {
		switch (brickType,
		
			\i, {
				case
					{orientation == 0 or: { orientation == 2}}
					// OOXO
					{ ^[ChordtrisBrickPart(x-2, y, true, true, false),
						ChordtrisBrickPart(x-1, y, true, false, false),
						ChordtrisBrickPart(x, y, true, false, false),
						ChordtrisBrickPart(x+1, y, true, false, true)]
					}
					// O
					// O
					// X
					// O
					{ ^[ChordtrisBrickPart(x, y-2, false, true, true),
						ChordtrisBrickPart(x, y-1, false, true, true),
						ChordtrisBrickPart(x, y, false, true, true),
						ChordtrisBrickPart(x, y+1, true, true, true)]
					};
			},
			\j, {
				case
					{orientation == 0}
					// OXO
					//   O
					{ ^[ChordtrisBrickPart(x-1, y, true, true, false),
						ChordtrisBrickPart(x, y, true, false, false),
						ChordtrisBrickPart(x+1, y, false, false, true),
						ChordtrisBrickPart(x+1, y+1, true, false, true)]
					}
					{orientation == 1}
					//  O
					//  X
					// OO
					{ ^[ChordtrisBrickPart(x, y-1, false, true, true),
						ChordtrisBrickPart(x, y, false, true, true),
						ChordtrisBrickPart(x, y+1, true, false, true),
						ChordtrisBrickPart(x-1, y+1, true, true, false)]
					}
					{orientation == 2}
					// O
					// OXO
					{ ^[ChordtrisBrickPart(x-1, y-1, false, true, true),
						ChordtrisBrickPart(x-1, y, true, true, false),
						ChordtrisBrickPart(x, y, true, false, false),
						ChordtrisBrickPart(x+1, y, true, false, true)]
					}
					{orientation == 3}
					// OO
					// X
					// O
					{ ^[ChordtrisBrickPart(x+1, y-1, true, false, true),
						ChordtrisBrickPart(x, y-1, false, true, false),
						ChordtrisBrickPart(x, y, false, true, true),
						ChordtrisBrickPart(x, y+1, true, true, true)]
					};
			},
			\l, {
				case
					{orientation == 0}
					// OXO
					// O
					{ ^[ChordtrisBrickPart(x-1, y+1, true, true, true),
						ChordtrisBrickPart(x-1, y, false, true, false),
						ChordtrisBrickPart(x, y, true, false, false),
						ChordtrisBrickPart(x+1, y, true, false, true)]
					}
					{orientation == 1}
					// OO
					//  X
					//  O
					{ ^[ChordtrisBrickPart(x-1, y-1, true, true, false),
						ChordtrisBrickPart(x, y-1, false, false, true),
						ChordtrisBrickPart(x, y, false, true, true),
						ChordtrisBrickPart(x, y+1, true, true, true)]
					}
					{orientation == 2}
					//   O
					// OXO
					{ ^[ChordtrisBrickPart(x-1, y, true, true, false),
						ChordtrisBrickPart(x, y, true, false, false),
						ChordtrisBrickPart(x+1, y, true, false, true),
						ChordtrisBrickPart(x+1, y-1, false, true, true)]
					}
					{orientation == 3}
					// O
					// X
					// OO
					{ ^[ChordtrisBrickPart(x, y-1, false, true, true),
						ChordtrisBrickPart(x, y, false, true, true),
						ChordtrisBrickPart(x, y+1, true, true, false),
						ChordtrisBrickPart(x+1, y+1, true, false, true)]
					};
				
			},
			\o, {
				// OO
				// XO
				^[ChordtrisBrickPart(x, y-1, false, true, false),
				ChordtrisBrickPart(x, y, true, true, false),
				ChordtrisBrickPart(x+1, y-1, false, false, true),
				ChordtrisBrickPart(x+1, y, true, false, true)];
			},
			\z, {
				case
					{orientation == 0 or: { orientation == 2}}
					// OO
					//  XO
					{ ^[ChordtrisBrickPart(x-1, y-1, true, true, false),
						ChordtrisBrickPart(x, y-1, false, false, true),
						ChordtrisBrickPart(x, y, true, true, false),
						ChordtrisBrickPart(x+1, y, true, false, true)]
					}
					//  O
					// XO
					// O
					{ ^[ChordtrisBrickPart(x+1, y-1, false, false, true),
						ChordtrisBrickPart(x+1, y, true, false, true),
						ChordtrisBrickPart(x, y, false, true, false),
						ChordtrisBrickPart(x, y+1, true, true, false)]
					};
			},
			\t, {
				case
					{orientation == 0}
					// OXO
					//  O
					{ ^[ChordtrisBrickPart(x, y+1, true, true, true),
						ChordtrisBrickPart(x-1, y, true, true, false),
						ChordtrisBrickPart(x, y, false, false, false),
						ChordtrisBrickPart(x+1, y, true, false, true)]
					}
					{orientation == 1}
					//  O
					// OX
					//  O
					{ ^[ChordtrisBrickPart(x-1, y, true, true, false),
						ChordtrisBrickPart(x, y-1, false, true, true),
						ChordtrisBrickPart(x, y, false, false, true),
						ChordtrisBrickPart(x, y+1, true, true, true)]
					}
					{orientation == 2}
					//  O
					// OXO
					{ ^[ChordtrisBrickPart(x-1, y, true, true, false),
						ChordtrisBrickPart(x, y, true, false, false),
						ChordtrisBrickPart(x+1, y, true, false, true),
						ChordtrisBrickPart(x, y-1, false, true, true)]
					}
					{orientation == 3}
					// O
					// XO
					// O
					{ ^[ChordtrisBrickPart(x, y-1, false, true, true),
						ChordtrisBrickPart(x, y, false, true, false),
						ChordtrisBrickPart(x, y+1, true, true, true),
						ChordtrisBrickPart(x+1, y, true, false, true)]
					};
			},
			\s, {
				case
					{orientation == 0 or: { orientation == 2}}
					//  OO
					// OX
					{ ^[ChordtrisBrickPart(x+1, y-1, true, false, true),
						ChordtrisBrickPart(x, y-1, false, true, false),
						ChordtrisBrickPart(x, y, true, false, true),
						ChordtrisBrickPart(x-1, y, true, true, false)]
					}
					// O
					// XO
					//  O
					{ ^[ChordtrisBrickPart(x+1, y+1, true, true, true),
						ChordtrisBrickPart(x+1, y, false, false, true),
						ChordtrisBrickPart(x, y, true, true, false),
						ChordtrisBrickPart(x, y-1, false, true, true)]
					};
			}
		);

	}

}