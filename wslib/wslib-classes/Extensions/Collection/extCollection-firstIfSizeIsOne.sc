+ Collection {
	firstIfSizeIsOne {
		if( this.size == 1 )
			{ ^this[0] }
			{ ^this }
		}
		
	repeatLast { |n = 1| ^this ++ this.class.fill( n, { this.last } ); }
	downSize { |n=1| ^this[..( this.size-(1+n) )] } // relative downsize
	
	
	}