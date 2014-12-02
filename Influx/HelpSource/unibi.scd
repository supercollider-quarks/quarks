unibi and biuni scale values from unipolar to bipolar range,
can optionally add noise, and clip the uni range at(0, 1).

code::
// tests
// simplenumber
0.75.unibi
0.75.unibi(0.01); // add noise

{ 0.75.unibi(0.01); }.dup(5).plot;

// arrays
[0, 0.5, 1].unibi;
[0, 0.5, 1].unibi(0.01).round(0.001);

// clips at 0, 1
(0, 0.1 .. 1.1).unibi


(-1, -0.75 .. 1).biuni;
(-1, -0.75 .. 1).biuni(0.01).round(0.001);

f = { 10.rand * 0.1 }.unibi;
f.value;
f = { 10.rand * 0.1 }.unibi(0.01);
f.value.round(0.001);

f = { 10.rand2 * 0.1 }.biuni;
f.value;
f = { 10.rand2 * 0.1 }.unibi(0.01);
f.value.round(0.001);
::