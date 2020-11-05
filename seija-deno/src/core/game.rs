use seija::core::IGame;
use seija::specs::{World,WorldExt,Builder};
use deno_core::{v8,OpState};
use seija::common::{Transform};
use seija::common::transform::component::ParentHierarchy;
use seija::window::ViewPortSize;
use seija::render::{Camera,ActiveCamera};
use seija::s2d::{S2DLoader};
use seija::event::{GameEventType};
use std::rc::Rc;
use std::cell::RefCell;
use std::collections::VecDeque;
use once_cell::sync::Lazy;
use crate::core::ToJsValue;
use crate::core::event::GameMessage;
use seija::specs::ReaderId;
use seija::specs_hierarchy::{HierarchyEvent};
pub struct JSGame<'a> {
    start_func:v8::Local<'a,v8::Function>,
    update_func:v8::Local<'a,v8::Function>,
    quit_func:v8::Local<'a,v8::Function>,
    scope:v8::HandleScope<'a>,
    local_value:v8::Local<'a,v8::Value>,
    op_state:Rc<RefCell<OpState>>,
    events:Vec<v8::Local<'a,v8::Value>>,
    entity_events:Option<ReaderId<HierarchyEvent>>
}

pub static mut MESSAGES: Lazy<VecDeque<GameMessage>> = Lazy::new(|| { VecDeque::default() });
pub static mut WORLD:Option<*mut World> = None;

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
            events:vec![],
            entity_events:None
        }
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
        let ev_typ_id = GameEventType::KeyBoard as u32;
        crate::opts::core::add_global_event_(entity.id(), ev_typ_id);
        world.fetch::<S2DLoader>().env().set_fs_root("./res/");
        
        let box_world = Box::new(world as *mut World);
        let res_id = self.op_state.borrow_mut().resource_table.add("World", box_world);
        let world_res_id = v8::Integer::new(&mut self.scope, res_id as i32);

        let mut hierarchy = world.fetch_mut::<ParentHierarchy>();
        let parent_events_id = hierarchy.track();
        self.entity_events = Some(parent_events_id);
       
        
        self.start_func.call(&mut self.scope, self.local_value, &[world_res_id.into()]);
    }

    fn update(&mut self,world:&mut World) {
        {
            let hierarchy = world.fetch_mut::<ParentHierarchy>();
            if let Some(e_reader) = self.entity_events.as_mut() {
                for event in  hierarchy.changed().read(e_reader) {
                    match event {
                        HierarchyEvent::Modified(_) => (),
                        HierarchyEvent::Removed(e) => {
                           let msg = GameMessage  {type_id:3,entity_id:e.id(),ev_type:0,ex0:0,event:None};
                           unsafe {MESSAGES.push_back(msg)};
                        }
                    }
                }
            }
        };
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