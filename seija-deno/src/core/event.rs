use deno_core::{v8::{self},JsRuntime};
use seija::event::{GameEventCallBack,GameEvent};
use crate::core::game::MESSAGES;
use crate::core::ToJsValue;

pub struct GameMessage {
    type_id:u32,
    entity_id:u32,
    event:GameEvent
}

impl ToJsValue for GameMessage {
    fn to<'a>(&self, scope:&mut v8::HandleScope<'a>) -> v8::Local<v8::Value> {
        let v8_type = v8::Integer::new(scope, self.type_id as i32).into();
        let v8_entity = v8::Integer::new(scope, self.entity_id as i32).into();
        let v8_ev = self.event.to(scope);
        let arr:v8::Local<v8::Value> = v8::Array::new_with_elements(scope, &[v8_type ,v8_entity,v8_ev]).into();
        unsafe {std::mem::transmute(arr) } 
    }
}

pub struct JSEventCallback {
    pub eid:u32
}

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
        unsafe { MESSAGES.push_back(GameMessage { type_id:0, entity_id:self.eid, event:ev.clone()})};
    }
}