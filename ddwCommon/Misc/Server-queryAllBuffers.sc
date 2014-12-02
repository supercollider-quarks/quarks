
// server buffer debugger

+ Server {
	queryAllBuffers { |timeout = 1.2, wait = 0.001, action|
		var	returns = Array.newClear(this.options.numBuffers),
			osc = OSCresponderNode(this.addr, '/b_info', { |t, r, m|
				bInfoCount = bInfoCount + 1;
				(m.size > 1 and: { m[2] > 0 }).if({
					returns[m[1]] = Buffer.new(this, m[2], m[3], m[1])
						.sampleRate_(m[4]);
				});
			}).add,
			bInfoCount = 0;
		{	this.options.numBuffers.do({ |bufnum|
				this.sendMsg(\b_query, bufnum);
				wait.wait;
			});
		}.fork(SystemClock);
		AppClock.sched(timeout, {
			(bInfoCount == this.options.numBuffers).if({
				returns = returns.reject(_.isNil);
				if(action.notNil) {
					action.value(returns)
				} {
					returns.do({ |buf|
						buf.postln;
					});
				};
			}, {
				"queryAllBuffers failed; some b_queries timed out (% / % = %%)."
					.format(bInfoCount, this.options.numBuffers,
						(bInfoCount / this.options.numBuffers * 100).round(0.01), $%)
					.warn;
			});
			osc.remove;
			nil;
		});
		^returns
	}
}
