// Bind an example SynthDef to 'a' and write a file to /tmp using the
// 'dot' method.

(
  a = SynthDef.new("Example", {
    arg bus=0, freq=440;
    var e, o;
    e = EnvGen.kr(Env.perc, levelScale:0.3, doneAction:2);
    o = RLPF.ar(LFSaw.ar(freq) * e,
                LFNoise1.kr(1, 36, 110).midicps,
                0.1);
    4.do({o = AllpassN.ar(o, 0.05, [0.05.rand, 0.05.rand], 4)});
    Out.ar(bus, o);
  });
  f = File.new("/tmp/Example.dot", "w");
  a.dot(f);
  f.close;
)
