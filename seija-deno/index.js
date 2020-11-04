console.log = function(v) {
  Deno.core.print(v.toString()+"\n")
}
Deno.core.ops();
var worldId = 0;
function newEntity() {
  return Deno.core.jsonOpSync("newEntity",worldId);
}

function addEntityInfo(eid,eName) {
  return Deno.core.jsonOpSync("addEntityInfo",[worldId,eid,eName]);
}
function getEntityName(eid) {
  return Deno.core.jsonOpSync("getEntityName",[worldId,eid]);
}

function setEntityName(eid,name) {
  return Deno.core.jsonOpSync("setEntityName",[worldId,eid,name]);
}


function game_start(world_rid) {
  worldId = world_rid;
  console.log(worldId);
  var root = newEntity();
  addEntityInfo(root,"NMB");
  setEntityName(root,"呵呵呵");
  var getName = getEntityName(root);
  console.log(getName);

 
}

function game_update() {
  if(arguments.length > 0) {
  
    console.log(arguments[0] +" ----"+arguments.length);
  }
 
  

}

function game_quit() {
  console.log("game quit");
}

const _newline = new Uint8Array([10]);
let s2d = Deno.core.jsonOpSync("newSimple2d",{
window:{bg_color:[0.6,0.6,0.6,1],width:1024,height:768 }
});

Seija.runApp(s2d,game_start,game_update,game_quit);