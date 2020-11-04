use seija::core::IGame;
use seija::specs::{World,WorldExt,Builder};
use deno_core::{v8,OpState};
use seija::common::Transform;
use seija::window::ViewPortSize;
use seija::render::{Camera,ActiveCamera};
use seija::s2d::{S2DLoader};
use seija::event::cb_event::CABEventRoot;
use std::rc::Rc;
use std::cell::RefCell;
use std::collections::VecDeque;
use once_cell::sync::Lazy;
use crate::core::ToJsValue;
pub struct JSGame<'a> {
    start_func:v8::Local<'a,v8::Function>,
    update_func:v8::Local<'a,v8::Function>,
    quit_func:v8::Local<'a,v8::Function>,
    scope:v8::HandleScope<'a>,
    local_value:v8::Local<'a,v8::Value>,
    op_state:Rc<RefCell<OpState>>,
    events:Vec<v8::Local<'a,v8::Value>>
}

pub static mut MESSAGES: Lazy<VecDeque<Box<dyn ToJsValue>>> = Lazy::new(|| { VecDeque::default() });

impl<'a> JSGame<'a> {
    pub fn new(start_func:v8::Local<'a,v8::Function>,
               update_func:v8::Local<'a,v8::Function>,
               quit_func:v8::Local<'a,v8::Function>,
               mut scope:v8::HandleScope<'a>,
               op_state:Rc<RefCell<OpState>>) -> JSGame<'a> {
        let global = scope.get_current_context().global(&mut scope).into();
        
        JSGame {
            start_func,
            update_func,
            quit_func,
            scope,
            local_value:global,
            op_state,
            events:vec![]
        }
    }
}

impl<'a> IGame for JSGame<'a> {
    fn start(&mut self, world:&mut World) {
        let camera_transform = Transform::default();
        let (w,h) = {
            let view_port = world.fetch::<ViewPortSize>();
            (view_port.width() as f32,view_port.height() as f32)
        };
        let entity = world.create_entity()
                                  .with(camera_transform)
                                  .with(Camera::standard_2d(w, h))
                                  .with(CABEventRoot {}).build();
        world.insert(ActiveCamera {entity : Some(entity) });
        world.fetch::<S2DLoader>().env().set_fs_root("./res/");
        
        let box_world = Box::new(world as *mut World);
        let res_id = self.op_state.borrow_mut().resource_table.add("World", box_world);
        let world_res_id = v8::Integer::new(&mut self.scope, res_id as i32);
        
        self.start_func.call(&mut self.scope, self.local_value, &[world_res_id.into()]);
    }

    fn update(&mut self,_world:&mut World) {
        unsafe {
            for msg in MESSAGES.iter() {
                self.events.push(msg.to(&mut self.scope));
            }
            MESSAGES.clear();
        };
        self.update_func.call(&mut self.scope, self.local_value, self.events.as_slice());
        self.events.clear();
    }

    fn quit(&mut self,_world:&mut World) {
        self.quit_func.call(&mut self.scope, self.local_value, &[]);
    }
}