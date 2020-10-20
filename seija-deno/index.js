console.log = function(v) {
  Deno.core.print(v.toString()+"\n")
}

Deno.core.ops();

console.log("enter index.js");
let world;

function newEntity() {
  return Deno.core.jsonOpSync("newEntity",world);
}

function deleteEntity(entity) {
  return Deno.core.jsonOpSync("deleteEntity",[world,entity]);
}


function entityChildrens(e) {
  return Deno.core.jsonOpSync("entityChildrens",[world,e]);
}

function entitySetParent(entity,parent) {
  return Deno.core.jsonOpSync("entitySetParent",[world,entity,parent]);
}

function updateWorld() {
  return Deno.core.jsonOpSync("updateWorld",world);
}

function getTimeDelta() {
  return Deno.core.jsonOpSync("getTimeDelta",world);
}

function getTimeScale() {
  return Deno.core.jsonOpSync("getTimeScale",world);
}

function setTimeScale(scale) {
  return Deno.core.jsonOpSync("setTimeScale",[world,scale]);
}

function getAbsoluteTime() {
  return Deno.core.jsonOpSync("getAbsoluteTime",world);
}

function deleteAllChildren(e) {
  return Deno.core.jsonOpSync("deleteAllChildren",[world,e]);
}

function loadSync(typ,path) {
  return Deno.core.jsonOpSync("loadSync",[world,typ,path,null]); 
}

function setAssetRootPath(path) {
  return Deno.core.jsonOpSync("setAssetRootPath",[world,path]);
}

function addTransform(e,value) {
  return Deno.core.jsonOpSync("addTransform",[world,e,value]);
}

function getTransformPosition(e) {
  return Deno.core.jsonOpSync("getTransformPosition",[world,e]);
}

function getTransformPositionRef(e,buffer) {
  return Deno.core.jsonOpSync("getTransformPositionRef",[world,e],buffer);
}

function getTransformScale(e) {
  return Deno.core.jsonOpSync("getTransformScale",[world,e]);
}

function getTransformRotation(e) {
  return Deno.core.jsonOpSync("getTransformRotation",[world,e]);
}

function setTransformPosition(e,vec3) {
  return Deno.core.jsonOpSync("setTransformPosition",[world,e,vec3]);
}

function setTransformScale(e,vec3) {
  return Deno.core.jsonOpSync("setTransformScale",[world,e,vec3]);
}

function setTransformRotation(e,vec3) {
  return Deno.core.jsonOpSync("setTransformRotation",[world,e,vec3]);
}

function addImageRender(e,texId) {
  return Deno.core.jsonOpSync("addImageRender",[world,e,texId]);
}

function addRect2D(e,width,height,anchorX,anchorY) {
    return Deno.core.jsonOpSync("addRect2D",[world,e,width,height,anchorX,anchorY]);
}

function setRect2DSize(e,width,height) {
  let arr = new Float32Array([width,height]);
  return Deno.core.jsonOpSync("setRect2DSizeRef",[world,e],arr);
}

function setRect2dAnchor(e,x,y) {
  let arr = new Float32Array([x,y]);
  return Deno.core.jsonOpSync("setRect2dAnchorRef",[world,e],arr);
}

function setTransparent(e,b) {
  return Deno.core.jsonOpSync("setTransparent",[world,e,b]);
}

function setImageColor(e,r,g,b,a) {
  return Deno.core.jsonOpSync("setImageColor",[world,e,r,g,b,a]);
}

function setImageTexture(e,texId) {
  return Deno.core.jsonOpSync("setImageTexture",[world,e,texId]);
}

function addTextRender(e,fontId) {
  return Deno.core.jsonOpSync("addTextRender",[world,e,fontId]);
}

function setTextString(e,string) {
  return Deno.core.jsonOpSync("setTextString",[world,e,string]);
}

function setTextColor(e,r,g,b,a) {
  return Deno.core.jsonOpSync("setTextColor",[world,e,r,g,b,a]);
}

function setFontSize(e,fontSize) {
  return Deno.core.jsonOpSync("setFontSize",[world,e,fontSize]);
}

function setFontAnchor(e,anchorType) {
  return Deno.core.jsonOpSync("setTextAnchor",[world,e,anchorType]);
}

function setTextLineMode(e,lineMode) {
  return Deno.core.jsonOpSync("setTextLineMode",[world,e,lineMode]);
}

function addSpriteRender(e,sheet,name) {
  return Deno.core.jsonOpSync("addSpriteRender",[world,e,sheet,name]);
}

function setSpriteName(e,name) {
  return Deno.core.jsonOpSync("setSpriteName",[world,e,name]);
}

function getRect2dAnchorRef(e) {
  
  var fff = new Float32Array(2);
   Deno.core.jsonOpSync("getRect2dAnchorRef",[world,e],fff);
  return fff;
}
////////////////////////////////////////////
let isCall = false;
function callOnce(fn) {
  if(isCall == false) {
    fn();
    isCall = true;
  }
}

let eid = 0;
let coin_res = 0;
let start_res = 0;
let textEntity = 0;
let sheet_res = 0;
let spriteEntity = 0;
function game_start(world_rid) {
    console.log("game start: " + world_rid);
    world = world_rid;
    eid = newEntity();
    console.log("create eid:"+eid.toString());

    textEntity = newEntity();
    let eid_1 = newEntity();
    console.log("create eid_0:"+textEntity.toString());
    console.log("create eid_1:"+eid_1.toString());
    

    setAssetRootPath("./src/tests/res");
    coin_res = loadSync(1,"CoinIcon.png");
    start_res = loadSync(1,"StarIcon.png");
    sheet_res = loadSync(2,"paper.json");
    console.log("sheet:"+sheet_res);
    
    console.log(start_res);

    addTransform(eid,[[3.14159264358,0.3333333,1],[1,1,1],[0,0,0]]);
    addRect2D(eid,100,100,0.5,0.5);
    addImageRender(eid,start_res);
    setTransparent(eid,true);
    setRect2dAnchor(eid,0.5,0.5);
    setImageColor(eid,0.9,1,1,1);
    
    createText(textEntity);
    createSprite(spriteEntity);
    entitySetParent(textEntity,spriteEntity);
    entitySetParent(spriteEntity,eid);
    

    var pos_arr = new Float32Array([0,0,0]);
    getTransformPositionRef(eid,pos_arr);
    console.log("eid pos:" + pos_arr.toString());

    let arr2 = Deno.core.jsonOpSync("entityAll",world);
    console.log("cccccc2:" + arr2.toString());

    var a = getRect2dAnchorRef(eid);
    console.log("?????" + a[0] + a[1]);
}

function createText(entity) {
  addTransform(entity,[[0,-300,0],[1,1,1],[0,0,0]]);
  addRect2D(entity,160,300,0.5,0.5);
  setTransparent(entity,true);
  let fontId = loadSync(3,"WenQuanYiMicroHei.ttf");
  addTextRender(entity,fontId);
  setFontSize(entity,32);
  setTextColor(textEntity,0,0,0,1);
  setFontAnchor(textEntity,9);
  setTextLineMode(textEntity,0);
}

function createSprite(entity) {
  addTransform(entity,[[0,-300,0],[1,1,1],[0,0,0]]);
  addRect2D(entity,260,176,0.5,0.5);
  setTransparent(entity,true);
  addSpriteRender(entity,sheet_res,"BlueButtonPressed");
  
}

let curX = 0;
let dir = 1;
let isSet = false;

function game_update(v) {
  callOnce(function() {
    let arr = entityChildrens(eid);
    console.log("children:"+arr.toString());
    setSpriteName(spriteEntity,"EmptyStar");
    let arr2 = Deno.core.jsonOpSync("entityAll",world);
    console.log("cccccc:" + arr2.toString());
   
  });
  let delta = getTimeDelta();
  let abs = getAbsoluteTime();
  curX += delta;
  setTextString(textEntity,abs.toString());
  
  if(Math.ceil(curX) % 2 == 0) {
      setImageTexture(eid,coin_res);
  } else {
    setImageTexture(eid,start_res);
  }
}

function game_quit() {
    console.log("game quit");
}

const _newline = new Uint8Array([10]);
let s2d = Deno.core.jsonOpSync("newSimple2d",{
  window:{bg_color:[0.6,0.6,0.6,1] }
});
 
Seija.runApp(s2d,game_start,game_update,game_quit);
