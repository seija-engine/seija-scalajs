use seija::{core::IGame, event::global::GlobalEventNode, specs::WriteStorage};
use seija::specs::{World,WorldExt,Builder};
use deno_core::{v8,OpState};
use seija::common::{Transform};
use seija::window::ViewPortSize;
use seija::render::{Camera,ActiveCamera};
use seija::s2d::{S2DLoader};
use seija::event::{GameEventType};
use std::{ffi::c_void, ops::Deref, rc::Rc};
use std::cell::RefCell;
use std::collections::VecDeque;
use once_cell::sync::Lazy;
use crate::core::ToJsValue;
use crate::core::event::GameMessage;

use super::event::JSEventCallback;
pub struct JSGame<'a> {
    start_func:v8::Local<'a,v8::Function>,
    update_func:v8::Local<'a,v8::Function>,
    quit_func:v8::Local<'a,v8::Function>,
    scope:v8::HandleScope<'a>,
    local_value:v8::Local<'a,v8::Value>,
    events:Vec<v8::Local<'a,v8::Value>>,
}

pub static mut MESSAGES: Lazy<VecDeque<GameMessage>> = Lazy::new(|| { VecDeque::default() });
pub static mut WORLD:Option<*mut World> = None;

impl<'a> JSGame<'a> {
    pub fn new(start_func:v8::Local<'a,v8::Function>,
               update_func:v8::Local<'a,v8::Function>,
               quit_func:v8::Local<'a,v8::Function>,
               mut scope:v8::HandleScope<'a>) -> JSGame<'a> {
        let global = scope.get_current_context().global(&mut scope).into();
       
        JSGame {
            start_func,
            update_func,
            quit_func,
            scope,
            local_value:global,
            events:vec![],
        }
    }

    fn add_global_events(world:&mut World,eid:u32) {
        let entity = world.entities().entity(eid);
        let mut global_storage:WriteStorage<GlobalEventNode> = world.write_storage::<GlobalEventNode>();
        let mut event_node = GlobalEventNode::default();

        event_node.insert(GameEventType::KeyBoard, Box::new(JSEventCallback {eid }));
        event_node.insert(GameEventType::TouchStart, Box::new(JSEventCallback {eid }));
        event_node.insert(GameEventType::TouchEnd, Box::new(JSEventCallback {eid }));
        global_storage.insert(entity, event_node).unwrap();    
        
    }
}

impl<'a> IGame for JSGame<'a> {
    fn start(&mut self, world:&mut World) {
        unsafe { WORLD = Some( world as *mut World ) } ;
        let camera_transform = Transform::default();
        let (w,h) = {
            let view_port = world.fetch::<ViewPortSize>();
            (view_port.width() as f32,view_port.height() as f32)
        };
        let entity = world.create_entity()
                                  .with(camera_transform)
                                  .with(Camera::standard_2d(w, h))
                                  .build();
        world.insert(ActiveCamera {entity : Some(entity) });
       
        JSGame::add_global_events(world, entity.id());
        world.fetch::<S2DLoader>().env().set_fs_root("./res/");
        
       
        let ex_world = v8::External::new(&mut self.scope,world as *mut World as *mut c_void);
        let world_object = v8::Object::new(&mut self.scope);
        world_object.set_index(&mut self.scope, 0, ex_world.into());
        self.start_func.call(&mut self.scope, self.local_value, &[world_object.into()]);
    }


    fn update(&mut self,_world:&mut World) {
       
        unsafe {
            for msg in MESSAGES.iter() {
                self.events.push(msg.to(&mut self.scope));
            }
            MESSAGES.clear();
        };
        let event_arr = v8::Array::new_with_elements(&mut self.scope, self.events.as_slice());
        self.update_func.call(&mut self.scope, self.local_value, &[event_arr.into()]);
        self.events.clear();
    }

    fn quit(&mut self,_world:&mut World) {
        self.quit_func.call(&mut self.scope, self.local_value, &[]);
    }
}