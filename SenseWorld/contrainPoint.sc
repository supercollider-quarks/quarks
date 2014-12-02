+ Point{

	constrain{ |rect|
		if ( rect.contains( this ).not,
			{
				x = x.clip( rect.left, rect.left + rect.width );
				y = y.clip( rect.top, rect.left + rect.height );
			});
	}

    constrainCopy{ |rect|
        var point = this.copy;
		if ( rect.contains( this ).not,
			{
				point.x = point.x.clip( rect.left, rect.left + rect.width );
				point.y = point.y.clip( rect.top, rect.left + rect.height );
			});
        ^point;
	}

}