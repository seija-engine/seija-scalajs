use deno_core::{v8::{self},JsRuntime};
use seija::event::{GameEventCallBack,GameEvent};
use crate::core::game::MESSAGES;
use crate::core::ToJsValue;

pub struct JSEventCallback {}

unsafe impl Send for JSEventCallback {}
unsafe impl Sync for JSEventCallback {}

impl ToJsValue for GameEvent {
    fn to<'a>(&self, scope:&mut v8::HandleScope<'a>) -> v8::Local<v8::Value> {
        let local:v8::Local<v8::Value> =  match self {
            GameEvent::KeyBoard(key,press) => {
                let v8_key = v8::Number::new(scope, *key as f64).into();
                let v8_press = v8::Boolean::new(scope, *press).into();
                v8::Array::new_with_elements(scope, &[v8_key ,v8_press]).into()
            },
            GameEvent::Click((x,y))  => {
                let v8_x = v8::Number::new(scope, *x).into();
                let v8_y = v8::Number::new(scope, *y).into();
                v8::Array::new_with_elements(scope, &[v8_x ,v8_y]).into()
            }
            _ => v8::String::new(scope, format!("{:?}",self).as_str()).unwrap().into()
        };
       unsafe {std::mem::transmute(local) } 
    }
}

impl GameEventCallBack for JSEventCallback {
    fn run(&self, ev:&GameEvent) { 
      let c = ev.clone();
       unsafe { MESSAGES.push_back(Box::new(c) ) };
    }
}