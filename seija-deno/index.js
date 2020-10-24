console.log = function(v) {
  Deno.core.print(v.toString()+"\n")
}
Deno.core.ops();

var path = "src/tests/res/image.xml";

let ret = Seija.parseXML(path);
console.log(JSON.stringify(ret));

let ret2 = Seija.parseXMLString("<Root> </Root>");
console.log(JSON.stringify(ret2));