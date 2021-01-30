use deno_core::v8;
use seija::event::{GameEventCallBack,GameEvent};
use crate::core::game::MESSAGES;
use crate::core::ToJsValue;
#[derive(Debug)]
pub struct GameMessage {
   pub type_id:u32,
   pub entity_id:u32,
   pub ev_type:u32,
   pub ex0:u32,
   pub event:Option<GameEvent>
}

impl ToJsValue for GameMessage {
    fn to<'a>(&self, scope:&mut v8::HandleScope<'a>) -> v8::Local<v8::Value> {
        let v8_type = v8::Integer::new(scope, self.type_id as i32).into();
        let v8_ev_type = v8::Integer::new(scope, self.ev_type as i32).into();
        let v8_entity = v8::Integer::new(scope, self.entity_id as i32).into();
        let v8_ex0 = v8::Integer::new(scope, self.ex0 as i32).into();
        let arr:v8::Local<v8::Value> = if let Some(gev) = self.event.as_ref() {
            let v8_ev = gev.to(scope);
            v8::Array::new_with_elements(scope, &[v8_type ,v8_entity,v8_ev_type,v8_ex0,v8_ev.into()]).into()
        } else {
            v8::Array::new_with_elements(scope, &[v8_type ,v8_entity,v8_ev_type,v8_ex0]).into()
        };
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
            GameEvent::TouchStart((x,y)) => {
                let v8_x = v8::Number::new(scope, *x).into();
                let v8_y = v8::Number::new(scope, *y).into();
                v8::Array::new_with_elements(scope, &[v8_x ,v8_y]).into()
            }
            GameEvent::TouchEnd((x,y)) => {
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
        let ev_type = ev.to_type() as u32;
        unsafe { MESSAGES.push_back(GameMessage {ev_type, type_id:0, entity_id:self.eid,ex0:0, event:Some(ev.clone())})};
    }
}