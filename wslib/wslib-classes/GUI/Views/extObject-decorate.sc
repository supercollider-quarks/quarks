+ Object {
	decorate { |margin, gap| if( this.respondsTo( \addFlowLayout ) )
				{ this.addFlowLayout( margin, gap ); ^this };	}
}