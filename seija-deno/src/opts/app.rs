#[doc(hidden)] 
use deno_core::{JsRuntime,OpState,ZeroCopyBuf};
use deno_core::error::AnyError;
use v8::Handle;
use crate::opts::*;
use serde_json::{Number, Value};
use seija::s2d::Simple2d;
use seija::win::{window::WindowBuilder,dpi::{LogicalSize,Size}};
use seija::app::AppBuilder;
use seija::core::LimitSetting;
use deno_core::v8 as v8;
use std::{convert::TryFrom, ffi::c_void, ops::Deref, u64};
use crate::core::game::JSGame;
use seija::specs::{World,WorldExt};
use seija::shrev::{EventChannel};
use seija::core::AppControlFlow;

pub fn init_json_func(rt:&mut JsRuntime) {
   reg_json_op_sync(rt, "closeApp", close_app);
}

pub fn init_v8_func(scope: &mut v8::HandleScope,object:v8::Local<v8::Object>) {
    reg_v8_func(scope, object, "makeSimple2d", make_simple2d);
    reg_v8_func(scope, object, "runApp", run_app);
}


fn close_app(_: &mut OpState,_: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world_ref:&mut World = get_mut_world();
    world_ref.write_resource::<EventChannel<AppControlFlow>>().single_write(AppControlFlow::Quit);
    Ok(Value::Null)
}

fn make_simple2d(scope: &mut v8::HandleScope,args: v8::FunctionCallbackArguments,mut rv: v8::ReturnValue) {
    let window_str:v8::Local<v8::Value> = v8::String::new(scope, "window").unwrap().into();
    let width_str = v8::String::new(scope, "width").unwrap().into();
    let height_str = v8::String::new(scope, "height").unwrap().into();
    let title_str = v8::String::new(scope, "title").unwrap().into();
    let color_str = v8::String::new(scope, "bg_color").unwrap().into();
    let object = v8::Local::<v8::Object>::try_from(args.get(0)).unwrap();
    let mwindow:Option<v8::Local<v8::Value>> = object.deref().get(scope, window_str);
    let mut window_title = String::default();
    let mut window_width  = 1024;
    let mut window_height = 768;
    let mut bg_color = [0f32,0f32,0f32,1f32];
    if let Some(window) = mwindow {
       let obj = window.to_object(scope).unwrap();
       let mwidth = obj.deref().get(scope,width_str);
       if let Some(width) = mwidth {
           window_width = width.int32_value(scope).unwrap();
       }
       let mheight = obj.deref().get(scope,height_str);
       if let Some(height) = mheight {
           window_height = height.int32_value(scope).unwrap();
       }
       let mtitle = obj.deref().get(scope,title_str);
       if let Some(title) = mtitle {
           let s = title.to_string(scope).unwrap();
           window_title = s.deref().to_rust_string_lossy(scope);
       }
       let mcolor = obj.deref().get(scope,color_str);
       if let Some(color) = mcolor.and_then(|v| v.to_object(scope)) {
          let r = color.get_index(scope, 0).unwrap().to_number(scope).unwrap().value();
          let g = color.get_index(scope, 1).unwrap().to_number(scope).unwrap().value();
          let b = color.get_index(scope, 2).unwrap().to_number(scope).unwrap().value();
          let a = color.get_index(scope, 3).unwrap().to_number(scope).unwrap().value();
          bg_color = [r as f32,g as f32,b as f32,a as f32];
       }
    }
    
    let mut s2d = Simple2d::new();
    s2d.with_bg_color(bg_color);
    s2d.with_window(move |wb: &mut WindowBuilder| {
        wb.window.title = window_title.to_string();
         wb.window.inner_size = Some(Size::Logical( LogicalSize {
             width: window_width as f64,
             height: window_height as f64,
         }));
     });
     let box_s2d = Box::new(s2d);
     let raw_ptr = Box::into_raw(box_s2d);

     let ret_obj = v8::Object::new(scope);
     let ex_ptr = v8::External::new(scope, raw_ptr as *mut c_void);
     ret_obj.set_index(scope,0, ex_ptr.into());
     rv.set(ret_obj.into());
}


fn run_app(scope: &mut v8::HandleScope,args: v8::FunctionCallbackArguments,_rv: v8::ReturnValue) {
    let s2d_obj = v8::Local::<v8::Object>::try_from(args.get(0)).unwrap();
    let start_fn = v8::Local::<v8::Function>::try_from(args.get(1)).unwrap();
    let update_fn = v8::Local::<v8::Function>::try_from(args.get(2)).unwrap();
    let quit_fn = v8::Local::<v8::Function>::try_from(args.get(3)).unwrap();
   
    let s2d_value = s2d_obj.get_index(scope, 0).unwrap();
    let s2d_res_raw_ptr:v8::Local<v8::External> = v8::Local::<v8::External>::try_from(s2d_value).unwrap();
    let s2d =  unsafe { Box::from_raw(s2d_res_raw_ptr.value() as *mut Simple2d) };
  
    let js_game = JSGame::new(start_fn, update_fn, quit_fn,v8::HandleScope::new(scope));
    let builder = AppBuilder::new().with_update_limiter(LimitSetting::Sleep(60));
    let mut app = builder.build(*s2d, js_game);

    app.run();
}

