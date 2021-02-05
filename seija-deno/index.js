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

function addTransform(eid) {
  return Deno.core.jsonOpSync("addTransform",[worldId,eid]);
}

function addRect2D(eid,w,h) {
  return Deno.core.jsonOpSync("addRect2D",[worldId,eid,w,h,0.5,0.5]);
}

function getEntityName(eid) {
  return Deno.core.jsonOpSync("getEntityName",[worldId,eid]);
}

function setEntityName(eid,name) {
  return Deno.core.jsonOpSync("setEntityName",[worldId,eid,name]);
}

function addEventNode(eid,evType,isCapture) {
  return Deno.core.jsonOpSync("addEventNode",[eid,evType,isCapture]);
}

function setParent(eid,p) {
  return Deno.core.jsonOpSync("entitySetParent",[worldId,eid,p]);
}

function deleteEntity(eid) {
  return Deno.core.jsonOpSync("deleteEntity",[worldId,eid]);
}

function fs_root() {
  return Deno.core.jsonOpSync("fs_root",[]);
}

function fs_split_path(path) {
  return Deno.core.jsonOpSync("fs_split_path",path);
}

var root = null;
var childrenLst = [];
function game_start(world_rid) {

}

function game_update(args) {

}

function game_quit() {
  console.log("game quit");
}

let s2d = Seija.makeSimple2d({
  window:{bg_color:[0.6,0.6,0.6,1],width:320,height:240 }
});

Seija.runApp(s2d,game_start,game_update,game_quit);