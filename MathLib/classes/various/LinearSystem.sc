/* Methods for calculating the LU decomposition of a n x n matrix A and solving a system of linear  
 * equations: Ax = B. Original C code adapted from http://mymathlib.webtrellis.net. Translation to  
 * SC and other functionality by Michael Dzjaparidze, 2010.
 */
+ Matrix {
	solve { arg b, method = \crout; var lu;
		if(method == \crout, {
			^this.crout.croutSolve(b)
		}, {
			if(method == \croutPivot, {
				lu = this.croutPivot; ^lu[0].croutPivotSolve(b, lu[1])
			}, {
				if(method == \doolittle, {
					^this.doolittle.doolittleSolve(b)
				}, {
					if(method == \doolittlePivot, {
						lu = this.doolittlePivot; ^lu[0].doolittlePivotSolve(b, lu[1])
					}, {
						if(method == \gauss, {
							^this.gauss(b)
						}, {
							^this.choleski.choleskiSolve(b)
						})
					})
				})
			})
		})
	}
	
	//Solve the linear equation Lx = B for x, where L is a lower triangular matrix
	lowerTriangularSolve { arg b; var n = this.cols, i, k, x = Array.newClear(n);
		k = 0;
		while({ k < n }, {
			if(this[k, k] == 0.0, { Error("Singular matrix.").throw });
			x[k] = b[k];
			i = 0;
			while({ i < k }, {
				x[k] = x[k] - (x[i] * this[k, i]);
				i = i + 1
			});
			x[k] = x[k] / this[k, k];
			k = k + 1
		});
		^x
	}
	
	//Solve the linear equation Lx = B for x, where L is a unit lower triangular matrix
	unitLowerTriangularSolve { arg b; var n = this.cols, i, k, x = Array.newClear(n);
		x[0] = b[0];
		k = 1;
		while({ k < n }, {
			x[k] = b[k];
			i = 0;
			while({ i < k }, {
				x[k] = x[k] - (x[i] * this[k, i]);
				i = i + 1
			});
			k = k + 1
		});
		^x
	}
	
	//Calculate the inverse of the lower triangular matrix L
	lowerTriangularInverse { var lu = this.deepCopy, n = this.cols, i, j, k, sum;
		//Invert the diagonal elements of the lower triangular matrix
		k = 0; 
		while({ k < n }, { 
			if(lu[k, k] == 0.0, { 
				Error("Singular matrix.").throw
			}, {
				lu[k, k] = lu[k, k].reciprocal
			}); 
			k = k + 1
		});
		//Invert the remaining lower triangular matrix L row by row
		i = 1;
		while({ i < n }, {
			j = 0;
			while({ j < i }, {
				sum = 0.0;
				k = j;
				while({ k < i }, { sum = sum + (lu[i, k] * lu[k, j]); k = k + 1 });
				lu[i, j] = (lu[i, i] * sum).neg;
				j = j + 1
			});
			i = i + 1
		});
		^lu
	}
	
	//Calculate the inverse of the unit lower triangular matrix L
	unitLowerTriangularInverse { var lu = this.deepCopy, n = this.cols, i, j, k;
		/* Invert the subdiagonal part of the matrix L row by row where the diagonal elements are 		 * assumed to be 1.0
		 */
		i = 1;
		while({ i < n }, {
			j = 0;
			while({ j < i }, {
				lu[i, j] = lu[i, j].neg;
				k = j + 1;
				while({ k < i }, {
					lu[i, j] = lu[i, j] - (lu[i, k] * lu[k, j]);
					k = k + 1
				});
				j = j + 1
			});
			i = i + 1
		});
		^lu
	}
	
	//Solve the linear equation Ux = B for x, where U is an upper triangular matrix
	upperTriangularSolve { arg b; var n = this.cols, i, k, x = Array.newClear(n);
		k = n - 1;
		while({ k >= 0 }, {
			if(this[k, k] == 0.0, { Error("Singular matrix.").throw });
			x[k] = b[k];
			i = k + 1;
			while({ i < n }, {
				x[k] = x[k] - (x[i] * this[k, i]);
				i = i + 1
			});
			x[k] = x[k] / this[k, k];
			k = k - 1
		});
		^x
	}
	
	//Solve the linear equation Ux = B for x, where U is a unit upper triangular matrix
	unitUpperTriangularSolve { arg b; var n = this.cols, i, k, x = Array.newClear(n);
		x[n-1] = b[n-1];
		k = n - 2;
		while({ k >= 0 }, {
			x[k] = b[k];
			i = k + 1;
			while({ i < n }, {
				x[k] = x[k] - (x[i] * this[k, i]);
				i = i + 1
			});
			k = k - 1
		});
		^x
	}
	
	//Calculate the inverse of the upper triangular matrix U
	upperTriangularInverse { var lu = this.deepCopy, n = this.cols, i, j, k, sum;
		//Invert the diagonal elements of the upper triangular matrix U
		k = 0; 
		while({ k < n }, { 
			if(lu[k, k] == 0.0, { 
				Error("Singular matrix.").throw
			}, {
				lu[k, k] = lu[k, k].reciprocal
			});
			k = k + 1 
		});
		//Invert the remaining upper triangular matrix U
		i = n - 2;
		while({ i >= 0 }, {
			j = n - 1;
			while({ j > i }, {
				sum = 0.0;
				k = i + 1;
				while({ k <= j }, {
					sum = sum + (lu[i, k] * lu[k, j]);
					k = k + 1
				});
				lu[i, j] = (lu[i, i] * sum).neg;
				j = j - 1
			});
			i = i - 1
		});
		^lu
	}
	
	//Calculate the inverse of the unit upper triangular matrix U
	unitUpperTriangularInverse { var lu = this.deepCopy, n = this.cols, i, j, k;
		/* Invert the superdiagonal part of the matrix U row by row where the diagonal elements are 		 * assumed to be 1.0
		 */
		i = n - 2;
		while({ i >= 0 }, {
			j = n - 1;
			while({ j > i }, {
				lu[i, j] = lu[i, j].neg;
				k = i + 1;
				while({ k < j }, {
					lu[i, j] = lu[i, j] - (lu[i, k] * lu[k, j]);
					k = k + 1
				});
				j = j - 1
			});
			i = i - 1
		});
		^lu
	}
	
	gauss { arg b; var a = this.deepCopy, n = this.cols, x = b.copy, row, i, j, pivotRow, max, dum;
		//For each variable find pivot row and perform forward substitution
		row = 0;
		while({ row < (n-1) }, {
			//Find pivot row
			pivotRow = row;
			max = a[row, row].abs;
			i = row + 1;
			while({ i < n }, {
				if((dum = a[i, row].abs) > max, {
					max = dum;
					pivotRow = i
				});
				i = i + 1
			});
			if(max == 0.0, { Error("Singular matrix.").throw });
			//If it differs from the current row, interchange them
			if(pivotRow != row, {
				i = row;
				while({ i < n }, {
					dum = a[row, i];
					a[row, i] = a[pivotRow, i];
					a[pivotRow, i] = dum;
					i = i + 1
				});
				dum = x[row];
				x[row] = x[pivotRow];
				x[pivotRow] = dum
			});
			//Perform forward substitution
			i = row + 1;
			while({ i < n }, {
				dum = (a[i, row] / a[row, row]).neg;
				a[i, row] = 0.0;
				j = row + 1;
				while({ j < n }, {
					a[i, j] = a[i, j] + (dum * a[row, j]);
					j = j + 1
				});
				x[i] = x[i] + (dum * x[row]);
				i = i + 1
			});
			row = row + 1
		});	
		//Perform backward substitution
		row = n - 1;
		while({ row >= 0 }, {
			if(a[row, row] == 0.0, { Error("Singular matrix.").throw });
			dum = a[row, row].reciprocal;
			i = row + 1;
			while({ i < n }, {
				a[row, i] = a[row, i] * dum;
				i = i + 1
			});
			x[row] = x[row] * dum;
			i = 0;
			while({ i < row }, {
				dum = a[i, row];
				j = row + 1;
				while({ j < n }, {
					a[i, j] = a[i, j] - (dum * a[row, j]);
					j = j + 1
				});
				x[i] = x[i] - (dum * x[row]);
				i = i + 1
			});
			row = row - 1
		});
		^x
	}
	
	choleski { var lu = this.deepCopy, n = this.cols, i, k, p, rcpr;
		k = 0;
		while({ k < n }, {
			/* Calculate the difference of the diagonal element in row k from the sum of squares 			 * of elements row k from column 0 to column k-1.
			 */
			p = 0; while({ p < k }, { lu[k, k] = lu[k, k] - (lu[k, p] * lu[k, p]); p = p + 1 });
			//If the diagonal element is not positive, return error
			if(lu[k, k] <= 0.0, { Error("Matrix is not positive definite symmetric.").throw });
			//Otherwise take the square root of the diagonal element
			lu[k, k] = lu[k, k].sqrt;
			rcpr = lu[k, k].reciprocal;
			/* For rows i = k+1 to n-1, column k, calculate the difference between the i, k'th 			 * element and the inner product of the first k-1 columns of row i and row k, then 			 * divide the difference by the diagonal element in row k. Store the transposed 			 * element in the upper triangular matrix.
			 */
			i = k + 1;
			while({ i < n }, {
				p = 0;
				while({ p < k }, {
					lu[i, k] = lu[i, k] - (lu[i, p] * lu[k, p]);
					p = p + 1
				});
				lu[i, k] = lu[i, k] * rcpr;
				lu[k, i] = lu[i, k];
				i = i + 1
			});
			k = k + 1
		});
		^lu
	}
	
	choleskiSolve { arg b; var x;
		//Solve the linear equation Ly = B for y, where L is a lower triangular matrix
		x = this.lowerTriangularSolve(b);
		/* Solve the linear equation Ux = y, where y is the solution obtained above of Ly = B and U 		 * is an upper triangular matrix.
		 */
		^this.upperTriangularSolve(x)
	}
	
	crout { var lu = this.deepCopy, n = this.cols, row, i, j, k, p;
		/* For each row and column, k = 0, ..., n-1, find the lower triangular matrix elements for		 * column k and if the matrix is non-singular (nonzero diagonal element), find the upper 
		 * triangular matrix elements for row k.
		 */
		k = 0;
		while({ k < n }, {
			i = k;
			row = k;
			while({ i < n }, {
				p = 0;
				while({ p < k }, {
					lu[row, k] = lu[row, k] - (lu[row, p] * lu[p, k]);
					p = p + 1
				});
				row = row + 1;
				i = i + 1
			});
			if(lu[k, k] == 0.0, { Error("Singular matrix.").throw });
			j = k + 1;
			while({ j < n }, {
				p = 0;
				while({ p < k }, {
					lu[k, j] = (lu[k, j] - (lu[k, p] * lu[p, j])) / lu[k, k];
					p = p + 1
				});
				j = j + 1
			});
			k = k + 1
		});
		//Return lower triangular part and non-diagonal unit upper triangular part
		^lu
	}
	
	croutSolve { arg b; var x;
		//Solve the linear equation Lx = B for x, where L is a lower triangular matrix
		x = this.lowerTriangularSolve(b);
		/* Solve the triangular equation Ux = y, where y is the solution obtained above of Lx = B 		 * and U is an upper triangular matrix. The diagonal part of the upper triangular part of 		 * the matrix is assumed to be 1.0.
		 */
		^this.unitUpperTriangularSolve(x)
	}
	
	croutPivot { var lu = this.deepCopy, n = this.cols, i, j, k, p, max, pcol, pivot = 
	Array.newClear(n);			
		//For each row and column, k = 0, ..., n-1
		k = 0;
		while({ k < n }, {
			//Find the pivot row
			pivot[k] = k;
			max = lu[k, k].abs;
			j = k + 1;
			while({ j < n }, {
				if(max < lu[j, k].abs, {
					max = lu[j, k].abs;
					pivot[k] = j;
					pcol = j
				});
				j = j + 1
			});
			//If the pivot row differs from the current row, then interchange the two rows
			if(pivot[k] != k, {
				j = 0;
				while({ j < n }, {
					max = lu[k, j];
					lu[k, j] = lu[pcol, j];
					lu[pcol, j] = max;
					j = j + 1
				})
			});
			//If the matrix is singular, return an Error
			if(lu[k, k] == 0.0, { Error("Singular matrix.").throw });
			//Otherwise find the upper triangular matrix elements for row k.
			j = k + 1;
			while({ j < n }, { lu[k, j] = lu[k, j] / lu[k, k]; j = j + 1 });
			//Update remaining matrix
			i = k + 1;
			while({ i < n }, {
				j = k + 1;
				while({ j < n }, {
					lu[i, j] = lu[i, j] - (lu[i, k] * lu[k, j]);
					j = j + 1
				});
				i = i + 1
			});
			k = k + 1
		});
		^[lu, pivot]	 //Return LU matrix and pivot array
	}
	
	croutPivotSolve { arg b, pivot; var n = this.cols, i, k, dum, x = Array.newClear(n);
		//Solve the linear equation Lx = B for x, where L is a lower triangular matrix
		k = 0;
		while({ k < n }, {
			if(pivot[k] != k, {
				dum = b[k];
				b[k] = b[pivot[k]];
				b[pivot[k]] = dum;
			});
			x[k] = b[k];
			i = 0;
			while({ i < k }, { x[k] = x[k] - (x[i] * this[k, i]); i = i + 1 });
			x[k] = x[k] / this[k, k];
			k = k + 1
		});
		/* Solve the linear equation Ux = y, where y is the solution obtained above of Lx = B and U 		 * is an upper triangular matrix. The diagonal part of the upper triangular part of the 		 * matrix is assumed to be 1.0.
		 */
		 k = n - 1;
		 while({ k >= 0 }, {
			 if(pivot[k] != k, {
				 dum = b[k];
				 b[k] = b[pivot[k]];
				 b[pivot[k]] = dum;
			 });
			 i = k + 1;
			 while({ i < n }, { x[k] = x[k] - (x[i] * this[k, i]); i = i + 1 });
			 k = k - 1
		 });
		 ^x
	}
	
	doolittle { var lu = this.deepCopy, n = this.cols, i, j, k, p, row;
		/* For each row and column, k = 0, ..., n-1, find the upper triangular matrix elements for 		 * row k and if the matrix is non-singular (nonzero diagonal element), find the lower 		 * triangular matrix elements for column k.
		 */
		 k = 0;
		 while({ k < n }, {
			 j = k;
			 while({ j < n }, {
				 p = 0;
				 while({ p < k }, {
					 lu[k, j] = lu[k, j] - (lu[k, p] * lu[p, j]);
					 p = p + 1
				 });
				 j = j + 1
			 });
			 if(lu[k, k] == 0.0, { Error("Singular matrix.").throw });
			 i = k + 1;
			 while({ i < n }, {
				 p = 0;
				 while({ p < k }, {
					 lu[i, k] = lu[i, k] - (lu[i, p] * lu[p, k]);
					 p = p + 1
				 });
				 lu[i, k] = lu[i, k] / lu[k, k];
				 i = i + 1
			 });
			 k = k + 1
		 });
		 ^lu
	}
	
	doolittleSolve { arg b; var x;
		/* Solve the linear equation Lx = B for x, where L is a lower triangular matrix with an 		 * implied 1 along the diagonal.
		 */
		x = this.unitLowerTriangularSolve(b);
		/* Solve the linear equation Ux = y, where y is the solution obtained above of Lx = B and U 		 * is an upper triangular matrix.
		 */
		^this.upperTriangularSolve(x)
	}
	
	doolittlePivot { var lu = this.deepCopy, n = this.cols, i, j, k, p, max, pcol, pivot = 	Array.newClear(n);
		//For each row and column, k = 0, ..., n-1
		k = 0;
		while({ k < n }, {
			//Find the pivot row
			pivot[k] = k;
			max = lu[k, k].abs;
			j = k + 1;
			while({ j < n }, {
				if(max < lu[j, k].abs, {
					max = lu[j, k].abs;
					pivot[k] = j;
					pcol = j
				});
				j = j + 1
			});
			//If the pivot row differs from the current row, then interchange the two rows
			if(pivot[k] != k, {
				j = 0;
				while({ j < n }, {
					max = lu[k, j];
					lu[k, j] = lu[pcol, j];
					lu[pcol, j] = max;
					j = j + 1
				})
			});
			//If the matrix is singular, return an Error
			if(lu[k, k] == 0.0, { Error("Singular matrix.").throw });
			//Otherwise find the lower triangular matrix elements for column k
			i = k + 1;
			while({ i < n }, { lu[i, k] = lu[i, k] / lu[k, k]; i = i + 1 });
			//Update remaining matrix
			i = k + 1;
			while({ i < n }, {
				j = k + 1;
				while({ j < n }, {
					lu[i, j] = lu[i, j] - (lu[i, k] * lu[k, j]);
					j = j + 1
				});
				i = i + 1
			});
			k = k + 1
		});
		^[lu, pivot]		//Return LU matrix and pivot array
	}
	
	doolittlePivotSolve { arg b, pivot; var n = this.cols, i, k, dum, x = Array.newClear(n);
		/* Solve the linear equation Lx = B for x, where L is a lower triangular matrix with an 		 * implied 1 along the diagonal.
		 */
		k = 0;
		while({ k < n }, {
			if(pivot[k] != k, {
				dum = b[k];
				b[k] = b[pivot[k]];
				b[pivot[k]] = dum
			});
			x[k] = b[k];
			i = 0;
			while({ i < k }, { x[k] = x[k] - (x[i] * this[k, i]); i = i + 1 });
			k = k + 1
		});
		/* Solve the linear equation Ux = y, where y is the solution obtained above of Lx = B and 		 * U is an upper triangular matrix.
		 */
		k = n - 1;
		while({ k >= 0 }, {
			if(pivot[k] != k, {
				dum = b[k];
				b[k] = b[pivot[k]];
				b[pivot[k]] = dum
			});
			i = k + 1;
			while({ i < n }, { x[k] = x[k] - (x[i] * this[k, i]); i = i + 1 });
			x[k] = x[k] / this[k, k];
			k = k - 1
		});
		^x
	}
}