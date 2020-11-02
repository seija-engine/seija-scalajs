#[doc(hidden)] 
use deno_core::{JsRuntime,OpState,ZeroCopyBuf};
use deno_core::error::AnyError;
use crate::opts::*;
use serde_json::Value;
use seija::s2d::Simple2d;
use seija::win::{window::WindowBuilder,dpi::{LogicalSize,Size}};
use seija::app::AppBuilder;
use seija::core::LimitSetting;
use deno_core::v8 as v8;
use std::convert::TryFrom;
use crate::core::game::JSGame;
use seija::specs::{World,WorldExt};
use seija::shrev::{EventChannel};
use seija::core::AppControlFlow;

pub fn init_json_func(rt:&mut JsRuntime) {
   reg_json_op_sync(rt, "newSimple2d", new_simple2d);
   reg_json_op_sync(rt, "closeApp", close_app);
}

pub fn init_v8_func(scope: &mut v8::HandleScope,object:v8::Local<v8::Object>) {
    reg_v8_func(scope, object, "runApp", run_app);
}



fn new_simple2d(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let object = value.as_object();
    let window = object.and_then(|v|v.get("window")).and_then(|v| v.as_object());

    let window_title = window.and_then(|v|v.get("title"))
                                   .and_then(|v| v.as_str())
                                   .unwrap_or("Seija Game");
                           
    let window_width = window.and_then(|v| v.get("width"))
                                  .and_then(|v| v.as_i64()).unwrap_or(1024);
    
    let window_height = window.and_then(|v| v.get("height"))
                                   .and_then(|v| v.as_i64()).unwrap_or(768);

    let bg_color = window.and_then(|v| v.get("bg_color"))
                                   .map(json_to_vec4).unwrap_or([0f32,0f32,0f32,1f32]);

    
    
                                            
    let mut s2d = Simple2d::new();
    s2d.with_bg_color(bg_color);
    s2d.with_window(move |wb: &mut WindowBuilder| {
        wb.window.title = window_title.to_string();
         wb.window.inner_size = Some(Size::Logical( LogicalSize {
             width: window_width as f64,
             height: window_height as f64,
         }));
     });
    let rid = state.resource_table.add("Simple2d", Box::new(s2d));
    Ok(Value::from(rid))
}

fn close_app(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world_rid = value.as_i64().unwrap();
    let world:*mut World = *state.resource_table.get(world_rid as u32).unwrap();
    let world_ref:&mut World = unsafe { std::mem::transmute(world) };
    world_ref.write_resource::<EventChannel<AppControlFlow>>().single_write(AppControlFlow::Quit);
    Ok(Value::Null)
}




fn run_app(scope: &mut v8::HandleScope,args: v8::FunctionCallbackArguments,_rv: v8::ReturnValue) {
    let s2d_res_id = v8::Local::<v8::Int32>::try_from(args.get(0)).unwrap().value() as u32;
    let start_fn = v8::Local::<v8::Function>::try_from(args.get(1)).unwrap();
    let update_fn = v8::Local::<v8::Function>::try_from(args.get(2)).unwrap();
    let quit_fn = v8::Local::<v8::Function>::try_from(args.get(3)).unwrap();
    
    let s2d = JsRuntime::state(scope)
                                      .borrow_mut().op_state
                                      .borrow_mut().resource_table.remove::<Simple2d>(s2d_res_id).unwrap();
  
    let op_state = JsRuntime::state(scope).borrow().op_state.clone();
    let js_game = JSGame::new(start_fn, update_fn, quit_fn,v8::HandleScope::new(scope),op_state);
    let builder = AppBuilder::new().with_update_limiter(LimitSetting::Sleep(60));
    let mut app = builder.build(*s2d, js_game);

    app.run();
}

