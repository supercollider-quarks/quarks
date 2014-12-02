/*
KDTree.test;

TestKDTree.dumpTrees = false
TestKDTree.dumpTrees = true

TestKDTree.tree.dumpTree
TestKDTree.tree.highestUniqueId

TestKDTree.tree.nearest([0,1], verbose: true)
TestKDTree.tree2.nearest([7,9], verbose: true).location

TestKDTree.tree.nearest([5,5,0])[0].location
~bl = TestKDTree.tree.pr_BestLeafFor([5,9])
~bl = TestKDTree.tree.pr_QuickDescend([5,9])
~bl.location
~bl.leftChild
~bl.rightChild
~bl.isLeftChild
~bl.parent.location

~node = TestKDTree.tree.find([7, 4, 3]);
~node.leftChild.nearest(~node.location)
~node.nearestToNode[0].location

*/
TestKDTree : UnitTest {
	classvar <array, <size, <dims, <tree, <array2, <tree2, <>dumpTrees=false;
	
	setUp {
	}
	tearDown {
	}
	
	// Creates a 3D data structure and tests whether certain searches return the same 
	// results under simple data rotation.
	test_multi {
		// int
		this.multiTest(2,60, 10);
		this.multiTest(3,60, 10);
		// float
		this.multiTest(2,60, 10.0);
		this.multiTest(3,60, 10.0);
	}
	
	multiTest { |dims=3, size=100, randLimit=10|
		var probe, probe2, match1, match2, dist1, dist2;
		
		array = {{randLimit.rand}.dup(dims)}.dup(size);
		
		//"rotationTest array:".postln;
		//array.do(_.postln);
		
		array2 = array.collect(_.rotate(1));
		
		// Data is spatially the same but rotated 90 degrees in each dim.
		// Because of the way KDTree uses dimensions to chop the data,
		// the actual tree structure will be very different,
		// but the results of queries should be the same (after the 
		// rotation is compensated).
		tree  = KDTree(array);
		tree2 = KDTree(array2);
		
		this.assert(tree.size==array.size, "tree.size==array.size");
		this.assert(tree.min==array.flop.collect(_.minItem), "tree.min==array.flop.collect(_.minItem) : % == %".format(tree.min,array.flop.collect(_.minItem)));
		this.assert(tree.max==array.flop.collect(_.maxItem), "tree.max==array.flop.collect(_.maxItem) : % == %".format(tree.max,array.flop.collect(_.maxItem)));
		
		if(this.class.dumpTrees){
			tree.dumpTree;
			tree2.dumpTree;
		};
		
		// Easy test - nearest neighbour to actual node, *without* excluding that node,
		// should be either that node itself or another node with exact same location.
		"Running TestKDTree nearest-to-node tests".postln;
		tree.do{|node|
			probe = node.location;
			# match1, dist1 = tree.nearest(probe);
			this.assert(dist1==0, "tree.nearest(%) dist==0".format(probe, dist1), false);
			
			probe2 = node.location.rotate(1);
			# match2, dist2 = tree2.nearest(probe2);
			this.assert(dist2==0, "tree2.nearest(%) dist==0".format(probe2, dist2), false);
			
			// When we use .nearestToNode, we should *not* get ourself back.
			# match1, dist1 = node.nearestToNode;
			this.assert(match1 != node, "node.nearestToNode should not return the self node: %, depth %".format(node.location, node.depth), false);
		};
		
		
		// Now we create some random spatial points, and do a NN query.
		// If there is a "tie-break" in NN queries then the order of preference
		// is arbitrary, so we have to test not on what the actual NN is, but 
		// by checking its distance from the probe.
		"Running TestKDTree nearest-to-point tests".postln;
		50.do{
			probe = {randLimit.rand}.dup(dims);
			probe2 = probe.rotate(1);
			
			# match1, dist1 = tree.nearest(probe);
			# match2, dist2 = tree2.nearest(probe2);
						
			this.assert(dist1 == dist2, "rotated space check: tree.nearest(%) same distance away as tree2.nearest(%)".format(probe, probe2), false);
		};
		
		this.allNearestTest(tree);
	}
	
	allNearestTest { |tree|
		var probe, match1, dist1, match2, dist2;
		// Check that the all-nearest neighbours query is returning the same data as the nearest-neighbour query is doing
		"Running TestKDTree:allNearestTest".postln;
		tree.allNearest.do{ |res|
			probe = res.key;
			match1 = res.value[0];
			dist1 = res.value[1];
			
			# match2, dist2 = probe.nearestToNode;
			
			this.assert(dist1 == dist2, "allNearest check: allNearest result (% -> %) same distance away as node.nearestToNode %: % == %"
					.format(probe.location, match1.location, match2.location, dist1, dist2), false);
		};
	}
}