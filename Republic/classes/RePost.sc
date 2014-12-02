
RePost {
	classvar <rePost, <rePostWin, <>toPost = true, <filters; 
	classvar <numPosts = 32, <posts;
	
	*alpha_ { |val = 0.8| rePostWin.alpha_(val) }
	
	*init { |parent, bounds|
		posts = posts ?? { List.newClear() };
		
		if (parent.isNil) { 
			if (rePostWin.notNil and: { rePostWin.isClosed.not }) { 
				rePostWin.front;
				^this
			}
		};
		try { rePostWin.close };
		
		this.makePost(parent, bounds);
	}
	
	*new { |post|
		if (posts.isNil) { this.init };

		if (posts.size > numPosts) { posts.drop(-1) };
		posts.addFirst(post);
		try { rePost.string = posts.join("") };
		if (toPost) { post.post }
	}
	
	*makePost { |parent, bounds|
		bounds = bounds ? Rect(0,0,400, 320);
		if (parent.isNil) { 
			rePostWin = parent = Window("RePost", bounds).front; 
			rePostWin.alpha_(0.8);
			bounds = bounds.moveTo(0,0);
		};
		^rePost = TextView(parent, bounds).enterInterpretsSelection_(false);
	}
	
	*clear { posts.clear; rePost.string = ""; }
}

+ Object { 
	repost { this.asString.repost }
	repostln { this.asString.repostln; }
	repostc { this.asString.postc }
	repostcln { this.asString.repostcln; }
	repostcs { this.asCompileString.repostln }
}

+ String { 
	repost { RePost(this) }
	repostln { RePost(this ++ $\n) }
	repostcln {  RePost("//" + this + $\n) }
	repostc { "// ".post; this.post; }
	repostf { |...items| 
		^this.prFormat( items.collect(_.asString) ).repost 
	}
}
