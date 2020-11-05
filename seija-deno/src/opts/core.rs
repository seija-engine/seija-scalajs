use deno_core::{JsRuntime,OpState,ZeroCopyBuf};
use deno_core::error::AnyError;
use crate::opts::{reg_json_op_sync,get_mut_world,json_to_vec3,write_vec3_to_buffer};
use seija::specs::{WorldExt,Entity,world::Builder,Join};
use serde_json::Value;
use seija::s2d::{S2DLoader,DefaultBackend};
use seija::common::EntityInfo;
use seija::common::transform::{component::ParentHierarchy,Parent,transform::Transform};
use seija::rendy::texture::image::ImageTextureConfig;
use seija::assets::{TextuteLoaderInfo,AssetLoadError,SpriteSheetLoaderInfo,FontAssetLoaderInfo};
use seija::math::Vector3;
use seija::core::Time;
use byteorder::{ByteOrder,NativeEndian};
use seija::rendy::hal::image::{SamplerDesc,Filter,WrapMode};
use seija::event::{global::GlobalEventNode,GameEventType,EventNode,cb_event::CABEventRoot};
use crate::core::event::{JSEventCallback,GameMessage};
use crate::core::game::MESSAGES;

pub fn init_json_func(rt:&mut JsRuntime) {
    reg_json_op_sync(rt, "newEntity", new_entity);
    reg_json_op_sync(rt, "entityChildrens", entity_childrens);
    reg_json_op_sync(rt, "entitySetParent", entity_set_parent);
    reg_json_op_sync(rt, "deleteEntity", delete_entity);
    reg_json_op_sync(rt, "deleteAllChildren", delete_all_children);
    reg_json_op_sync(rt, "entityIsAlive", entity_is_alive);
    reg_json_op_sync(rt, "entityAll", entity_all);
    

    reg_json_op_sync(rt, "loadSync", load_sync);
    reg_json_op_sync(rt, "setAssetRootPath", set_asset_root_path);
  
    reg_json_op_sync(rt, "addEntityInfo",add_entity_info);
    reg_json_op_sync(rt, "getEntityName",get_entity_name);
    reg_json_op_sync(rt, "setEntityName",set_entity_name);
    reg_json_op_sync(rt, "addTransform", add_transform);
    reg_json_op_sync(rt, "getTransformPosition", get_transform_position);
    reg_json_op_sync(rt, "getTransformScale", get_transform_scale);
    reg_json_op_sync(rt, "getTransformRotation", get_transform_rotation);
    reg_json_op_sync(rt, "getTransformPositionRef", get_transform_position_ref);
    reg_json_op_sync(rt, "getTransformScaleRef", get_transform_scale_ref);
    reg_json_op_sync(rt, "getTransformRotationRef", get_transform_rotation_ref);
    reg_json_op_sync(rt, "setTransformPosition", set_transform_position);
    reg_json_op_sync(rt, "setTransformScale", set_transform_scale);
    reg_json_op_sync(rt, "setTransformRotation", set_transform_rotation);
    reg_json_op_sync(rt, "setTransformPositionRef", set_transform_position_ref);
    reg_json_op_sync(rt, "setTransformScaleRef", set_transform_scale_ref);
    reg_json_op_sync(rt, "setTransformRotationRef", set_transform_rotation_ref);
    

    reg_json_op_sync(rt, "getTimeDelta", get_time_delta);
    reg_json_op_sync(rt, "getTimeScale", get_time_scale);
    reg_json_op_sync(rt, "getAbsoluteTime", get_absolute_time);
    reg_json_op_sync(rt, "setTimeScale", set_time_scale);
    
    reg_json_op_sync(rt, "updateWorld", update_world);

    reg_json_op_sync(rt, "addEventNode", add_event_node);
    reg_json_op_sync(rt, "addCABEventRoot", add_cabevent_root);
}

fn new_entity(_: &mut OpState,_: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let e:Entity = world.create_entity().build();
    Ok(Value::from(e.id() as i64)) 
}

fn add_cabevent_root(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let eid = value.as_u64().unwrap() as u32;
    let entity = world.entities().entity(eid);
    let mut storage = world.write_storage::<CABEventRoot>();
    if !storage.contains(entity) {
        storage.insert(entity , CABEventRoot{}).unwrap();
    }
    Ok(Value::Null) 
}

fn add_event_node(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let arr = value.as_array().unwrap();
    let eid = arr[0].as_u64().unwrap() as u32;
    let entity = world.entities().entity(eid);
    let mut event_storage = world.write_storage::<EventNode>();
    if !event_storage.contains(entity) {
        let event = EventNode::default();
        event_storage.insert(entity, event).unwrap();
        if arr.len() == 1 {
            return Ok(Value::Bool(true));
        }
    }
    let ev_type_id = arr[1].as_u64().unwrap() as u32;
    let is_capture = arr[2].as_bool().unwrap();
    let event_node = event_storage.get_mut(entity).unwrap();
    if let Some(event_type) = GameEventType::from(ev_type_id) {
        event_node.register(is_capture, event_type, move |e,_| {
           let msg = GameMessage {type_id :1,entity_id:e.id(),ev_type:ev_type_id,event:None,ex0:is_capture.into() };
           unsafe { MESSAGES.push_back(msg); }
        });
    }
    Ok(Value::Bool(false))
}

pub fn add_global_event_(eid:u32,ev_type:u32) -> bool {
    let world = get_mut_world();
    let entity = world.entities().entity(eid);
    let mut global_storage = world.write_storage::<GlobalEventNode>();
    if !global_storage.contains(entity) {
       let mut event_node = GlobalEventNode::default();
       let call_back = JSEventCallback {eid };
       if let  Some(typ) = GameEventType::from(ev_type) {
            event_node.insert(typ, Box::new(call_back));
            global_storage.insert(entity, event_node).unwrap();
            return true;
       }
    }
    false
}

fn entity_childrens(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let eid = arr[1].as_i64().unwrap() as u32;
    let entity = world.entities().entity(eid);
    
    let hierarchy = world.fetch_mut::<ParentHierarchy>();
    let mut arr:Vec<Value> = vec![];

    for ce in hierarchy.all_children_iter(entity) {
        arr.push(Value::from(ce.id()) );
    }
    Ok(Value::Array(arr))
}

fn entity_set_parent(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let e:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    let pe:Entity = world.entities().entity(arr[2].as_i64().unwrap() as u32);
    let mut storage = world.write_storage::<Parent>();
    if !storage.contains(e) {
        let p = Parent {entity:pe };
        storage.insert(e,p).unwrap();
    } else {
        let cur_p = storage.get_mut(e).unwrap();
        cur_p.entity = pe;
    }
    Ok(Value::from(e.id() as i64)) 
}

fn update_world(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    world.maintain();
    Ok(Value::Null)
}

fn get_time_delta(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let delta_time = world.read_resource::<Time>().delta_seconds();
    Ok(Value::from(delta_time))
}

fn get_time_scale(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let scale_time = world.read_resource::<Time>().time_scale();
    Ok(Value::from(scale_time))
}
fn set_time_scale(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let scale = arr[1].as_f64().unwrap();
    world.write_resource::<Time>().set_time_scale(scale as f32);
    Ok(Value::Null)
}

fn get_absolute_time(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let absolute_time = world.read_resource::<Time>().absolute_time_seconds();
    Ok(Value::from(absolute_time))
}

fn delete_entity(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    let is_ok = world.delete_entity(entity).is_ok();
    Ok(Value::Bool(is_ok))
}

fn entity_is_alive(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    
    Ok(Value::Bool(world.is_alive(entity)))
}

fn entity_all(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let arr:Vec<Value> = world.entities().join().map(|e| e.id().into()).collect();
    
    Ok(Value::Array(arr))
}

fn delete_all_children(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    let hierarchy = world.fetch_mut::<ParentHierarchy>();
    let entities = world.entities();
    let mut is_succ = true;
    for ce in hierarchy.all_children_iter(entity) {
        println!("del:{}",ce.id());
        if  entities.delete(ce).is_err() {
            is_succ = false;
        }
    }
    Ok(Value::Bool(is_succ))
}


fn load_sync(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let loader = world.fetch_mut::<S2DLoader>();
    let asset_type:i64 = arr[1].as_i64().unwrap();
    let asset_path = arr[2].as_str().unwrap();
    let assert_id = match asset_type {
        1 => {
            let mut tex_config = parse_image_config(&arr[3]);
            tex_config.premultiply_alpha = true;
            loader.load_sync::<_, DefaultBackend>(TextuteLoaderInfo::new(asset_path,tex_config), world).map(|h| h.id())
        },
        2 => {
            let mut tex_config = parse_image_config(&arr[3]);
            tex_config.premultiply_alpha = true;
            loader.load_sync::<_, DefaultBackend>(SpriteSheetLoaderInfo::new(&asset_path,tex_config), world).map(|h| h.id())
        },
        3 => {
            loader.load_sync::<_, DefaultBackend>(FontAssetLoaderInfo::new(&asset_path), world).map(|h| h.id())
        }
        _ => Err(AssetLoadError::LoadFileError)
    };
    match assert_id {
        Ok(id) => {
            Ok(Value::from(id))
        },
        Err(err) => {
            Ok(Value::String(format!("{:?}",err)))
        }
    }   
}

fn parse_image_config(value:&Value) -> ImageTextureConfig {
    let mut config = ImageTextureConfig::default();
    let object = value.as_object();
    if let Some(b) = object.and_then(|m|m.get("generateMips")).and_then(|v|v.as_bool()) {
        config.generate_mips = b;
    }
    if let Some(b) = object.and_then(|m|m.get("premultiplyAlpha")).and_then(|v|v.as_bool()) {
        config.premultiply_alpha = b;
    }
    if let Some(b) = object.and_then(|m|m.get("sampler_info")).and_then(|v|v.as_array()) {
       let filter = b[0].as_i64().unwrap_or(1);
       let wrap_mode = b[1].as_i64().unwrap_or(3);
       config.sampler_info = match filter {
           0 => {
               match wrap_mode {
                   0 => SamplerDesc::new(Filter::Nearest, WrapMode::Tile),
                   1 => SamplerDesc::new(Filter::Nearest, WrapMode::Mirror),
                   3 => SamplerDesc::new(Filter::Nearest, WrapMode::Clamp),
                   4 => SamplerDesc::new(Filter::Nearest, WrapMode::Border),
                   _ => panic!()
               }
           },
           1 => {
                match wrap_mode {
                    0 => SamplerDesc::new(Filter::Linear, WrapMode::Tile),
                    1 => SamplerDesc::new(Filter::Linear, WrapMode::Mirror),
                    3 => SamplerDesc::new(Filter::Linear, WrapMode::Clamp),
                    4 => SamplerDesc::new(Filter::Linear, WrapMode::Border),
                    _ => panic!()
                }
           },
           _ => panic!()
       };
    }
    config
}

fn set_asset_root_path(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let path = arr[1].as_str().unwrap();

    world.fetch::<S2DLoader>().env().set_fs_root(path);
    Ok(Value::String(path.to_string()))
}

fn add_entity_info(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    let name = arr[2].as_str().unwrap_or("");
    let mut storage = world.write_storage::<EntityInfo>();
    if !storage.contains(entity) {
        let mut info = EntityInfo::default();
        info.name = name.to_string();
        storage.insert(entity,info).unwrap();
        return Ok(Value::Bool(true));
    }

    Ok(Value::Bool(false))
}

fn  add_transform(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);

    let mut storage = world.write_storage::<Transform>();
    if !storage.contains(entity) {
            storage.insert(entity,Transform::default()).unwrap();
    }
    let trans:&mut Transform = storage.get_mut(entity).unwrap();
    if arr.len() > 2 && arr[2].is_array() {
        let arr = arr[2].as_array().unwrap();
        let pos = json_to_vec3(&arr[0]);
        let scale = json_to_vec3(&arr[1]);
        let rotation = json_to_vec3(&arr[2]);
        trans.set_position(pos);
        trans.set_scale(scale);
        trans.set_rotation_euler(rotation.x,rotation.y,rotation.z);
    }
    Ok(Value::Null)
}

fn get_transform_attr(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf],f:fn(&Transform) -> Vec<Value>) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);

    let storage = world.read_storage::<Transform>();
    
    let pos:Vec<Value> = if let Some(t) = storage.get(entity) {
        f(t)
    } else {
        vec![0.into(),0.into(),0.into()]
    };
    Ok(Value::Array(pos))
}


fn  get_entity_name(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    let storage = world.read_storage::<EntityInfo>();
    if let Some(t) = storage.get(entity) {
      return  Ok(Value::String(t.name.to_string()));
    };
    Ok(Value::Bool(false))
}

fn  set_entity_name(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    let mut storage = world.write_storage::<EntityInfo>();
    if let Some(t) = storage.get_mut(entity) {
        t.name = arr[2].as_str().unwrap_or("").to_string()
    };
    Ok(Value::Bool(false))
}


fn  get_transform_position(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    get_transform_attr(state,value,z,|t| {
        let pos = t.position();
        vec![pos.x.into(),pos.y.into(),pos.z.into()]
    })
}

fn get_transform_attr_ref(state: &mut OpState,value: Value,buffer:&mut [ZeroCopyBuf],f:fn(&Transform,buffer:&mut [u8])) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);

    let storage = world.read_storage::<Transform>();
    if let Some(t) = storage.get(entity) {
        f(t,&mut *buffer[0])
    };
    Ok(Value::Null)
}

fn get_transform_position_ref(state: &mut OpState,value: Value,zero_copy:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
   get_transform_attr_ref(state, value,zero_copy , |t,buffer| {
       write_vec3_to_buffer(t.position(), buffer)
   })
}

fn get_transform_scale_ref(state: &mut OpState,value: Value,zero_copy:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    get_transform_attr_ref(state, value,zero_copy , |t,buffer| {
        write_vec3_to_buffer(t.scale(), buffer)
    })
}

fn get_transform_rotation_ref(state: &mut OpState,value: Value,zero_copy:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    get_transform_attr_ref(state, value,zero_copy , |t,buffer| {
        let (x,y,z) = t.rotation().euler_angles();
        NativeEndian::write_f32(buffer,x);
        NativeEndian::write_f32(&mut buffer[4..8], y);
        NativeEndian::write_f32(&mut buffer[8..12], z);
    })
 }

fn  get_transform_scale(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    get_transform_attr(state,value,z,|t| {
        let pos = t.scale();
        vec![pos.x.into(),pos.y.into(),pos.z.into()]
    })
}

fn  get_transform_rotation(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    get_transform_attr(state,value,z,|t| {
        let (x,y,z) = t.rotation().euler_angles();
        vec![x.into(),y.into(),z.into()]
    })
}

fn set_transform_attr(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf],f:fn(&mut Transform,Vector3<f32>)) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    let float3:Vector3<f32> = json_to_vec3(&arr[2]);
    let mut storage = world.write_storage::<Transform>();
    if let Some(t) = storage.get_mut(entity) {
        f(t,float3);
    }
    Ok(Value::Null)
}

fn set_transform_position(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    set_transform_attr(state,value,z,|t,vec3| {
        t.set_position(vec3);
    })
}

fn set_transform_scale(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    set_transform_attr(state,value,z,|t,vec3| {
        t.set_scale(vec3);
    })
}

fn set_transform_rotation(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    set_transform_attr(state,value,z,|t,vec3| {
        t.set_rotation_euler(vec3.x,vec3.y,vec3.z);
    })
}


fn set_transform_attr_ref(state: &mut OpState,value: Value,buffer:&mut [ZeroCopyBuf],f:fn(&mut Transform,buffer:&mut [u8])) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    let mut storage = world.write_storage::<Transform>();
    if let Some(t) = storage.get_mut(entity) {
        f(t,&mut *buffer[0])
    };
    Ok(Value::Null)
}

fn set_transform_position_ref(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    set_transform_attr_ref(state,value,z,|t,buffer| {
        let x  = NativeEndian::read_f32(buffer);
        let y = NativeEndian::read_f32(&mut buffer[4..8]);
        let z = NativeEndian::read_f32(&mut buffer[8..12]);
        t.set_position_x(x);
        t.set_position_y(y);
        t.set_position_z(z);
    })
}

fn set_transform_scale_ref(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    set_transform_attr_ref(state,value,z,|t,buffer| {
        let x  = NativeEndian::read_f32(buffer);
        let y = NativeEndian::read_f32(&mut buffer[4..8]);
        let z = NativeEndian::read_f32(&mut buffer[8..12]);
        let mut_scale = t.scale_mut();
        mut_scale.x = x;
        mut_scale.y = y;
        mut_scale.z = z;
    })
}

fn set_transform_rotation_ref(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    set_transform_attr_ref(state,value,z,|t,buffer| {
        let x  = NativeEndian::read_f32(buffer);
        let y = NativeEndian::read_f32(&mut buffer[4..8]);
        let z = NativeEndian::read_f32(&mut buffer[8..12]);
        t.set_rotation_euler(x, y, z);
    })
}