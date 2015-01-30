+ Object {

    collectOrApply { |f|
    	^if( this.isKindOf(Collection) ) {
    		this.collect(f)
    	} {
    		f.(this)
    	}
    }

}