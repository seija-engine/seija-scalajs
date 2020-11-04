pub mod app;
pub mod core;
mod libs;
pub mod component2d;
use deno_core::{JsRuntime,ZeroCopyBuf,json_op_sync,OpState,OpId};
use deno_core::error::AnyError;
use serde_json::Value;
use deno_core::v8 as v8;
use v8::MapFnTo;
use seija::specs::{World};
use seija::math::Vector3;
use byteorder::{ByteOrder,NativeEndian};
use crate::core::game::WORLD;

pub fn init(rt:&mut JsRuntime) {
    reg_json_op_sync(rt, "op_close", deno_core::op_close);
    reg_json_op_sync(rt, "op_resources", deno_core::op_resources);
    app::init_json_func(rt);
    core::init_json_func(rt);
    component2d::init_json_func(rt);

    let gctx = rt.global_context();
    let mut scope = &mut v8::HandleScope::with_context(rt.v8_isolate(), gctx);
    let cur_ctx = scope.get_current_context();
    let global = cur_ctx.global(&mut scope);
    
    let seija_key = v8::String::new(&mut scope, "Seija").unwrap();
    let seija_val = v8::Object::new(&mut scope);
    global.set(&mut scope, seija_key.into(), seija_val.into());

    app::init_v8_func(scope,seija_val);
    libs::init_v8_func(scope, seija_val);
    component2d::init_v8_func(scope, seija_val);
}

pub fn reg_json_op_sync<F>(rt:&mut JsRuntime,name:&str,op_fn:F) -> OpId 
    where F:Fn(&mut OpState, Value, &mut [ZeroCopyBuf]) -> Result<Value, AnyError> + 'static {
    rt.register_op(name, json_op_sync(op_fn))
}

pub fn reg_v8_func(scope: &mut v8::HandleScope,object:v8::Local<v8::Object>,name:&str,callback: impl MapFnTo<v8::FunctionCallback>) {
    let cb_key = v8::String::new(scope, name).unwrap();
    let cb_tmpl = v8::FunctionTemplate::new(scope, callback);
    let cb_val = cb_tmpl.get_function(scope).unwrap();
    object.set(scope, cb_key.into(), cb_val.into());
   
}


pub fn json_to_vec4(val:&Value) -> [f32;4] {
    let arr = val.as_array().unwrap();
    [arr[0].as_f64().unwrap() as f32,
     arr[1].as_f64().unwrap() as f32,
     arr[2].as_f64().unwrap() as f32,
     arr[3].as_f64().unwrap() as f32]
}

pub fn json_to_vec3(val:&Value) -> Vector3<f32> {
    let arr = val.as_array().unwrap();
    Vector3::new(arr[0].as_f64().unwrap() as f32,arr[1].as_f64().unwrap() as f32,arr[2].as_f64().unwrap() as f32)
}

pub fn get_mut_world() -> &'static mut World {
    unsafe {std::mem::transmute(*WORLD.as_mut().unwrap())}
}

pub fn write_vec3_to_buffer(&vec:&Vector3<f32>,buffer:&mut [u8]) {
    NativeEndian::write_f32(buffer,vec.x);
    NativeEndian::write_f32(&mut buffer[4..8], vec.y);
    NativeEndian::write_f32(&mut buffer[8..12], vec.z);
}
/*
fn throw_type_error<'s>(
    scope: &mut v8::HandleScope<'s>,
    message: impl AsRef<str>,
  ) {
    let message = v8::String::new(scope, message.as_ref()).unwrap();
    let exception = v8::Exception::type_error(scope, message);
    scope.throw_exception(exception);
  }
  */