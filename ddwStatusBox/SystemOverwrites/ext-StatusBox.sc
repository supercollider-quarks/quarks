
+ String {
	prPostln { _PostLine }
	prPost { _PostString }
	postln {
		this.prPostln;
		StatusBox.postln(this)
	}
	post {
		this.prPost;
		StatusBox.post(this)
	}
}
