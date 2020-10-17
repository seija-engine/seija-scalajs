use deno_core::{JsRuntime,OpState,ZeroCopyBuf};
use deno_core::error::AnyError;
use crate::opts::{reg_json_op_sync,get_mut_world,json_to_vec3};
//use deno_core::v8 as v8;
use seija::specs::{WorldExt,Entity,world::Builder};
use serde_json::Value;
use seija::module_bundle::{S2DLoader,DefaultBackend};
use seija::common::transform::{component::ParentHierarchy,Parent,transform::Transform};
use seija::rendy::texture::image::ImageTextureConfig;
use seija::assets::{TextuteLoaderInfo,AssetLoadError,SpriteSheetLoaderInfo,FontAssetLoaderInfo};
use seija::math::Vector3;
use seija::core::Time;

pub fn init_json_func(rt:&mut JsRuntime) {
    reg_json_op_sync(rt, "newEntity", new_entity);
    reg_json_op_sync(rt, "entityChildrens", entity_childrens);
    reg_json_op_sync(rt, "entitySetParent", entity_set_parent);
    reg_json_op_sync(rt, "deleteEntity", delete_entity);
    reg_json_op_sync(rt, "deleteAllChildren", delete_all_children);

    reg_json_op_sync(rt, "loadSync", load_sync);
    reg_json_op_sync(rt, "setAssetRootPath", set_asset_root_path);
  
    reg_json_op_sync(rt, "addTransform", add_transform);
    reg_json_op_sync(rt, "getTransformPosition", get_transform_position);
    reg_json_op_sync(rt, "getTransformScale", get_transform_scale);
    reg_json_op_sync(rt, "getTransformRotation", get_transform_rotation);
    reg_json_op_sync(rt, "setTransformPosition", set_transform_position);
    reg_json_op_sync(rt, "setTransformScale", set_transform_scale);
    reg_json_op_sync(rt, "setTransformRotation", set_transform_rotation);

    reg_json_op_sync(rt, "getTimeDelta", get_time_delta);
    reg_json_op_sync(rt, "getTimeScale", get_time_scale);
    reg_json_op_sync(rt, "getAbsoluteTime", get_absolute_time);
    reg_json_op_sync(rt, "setTimeScale", set_time_scale);
    
    reg_json_op_sync(rt, "updateWorld", update_world);
}

fn new_entity(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world(value.as_i64().unwrap() as u32,state);
    let e:Entity = world.create_entity().build();
    Ok(Value::from(e.id() as i64)) 
}

fn entity_childrens(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
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
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
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
    let world = get_mut_world(value.as_i64().unwrap() as u32,state);
    world.maintain();
    Ok(Value::Null)
}

fn get_time_delta(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world(value.as_i64().unwrap() as u32,state);
    let delta_time = world.read_resource::<Time>().delta_seconds();
    Ok(Value::from(delta_time))
}

fn get_time_scale(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world(value.as_i64().unwrap() as u32,state);
    let scale_time = world.read_resource::<Time>().time_scale();
    Ok(Value::from(scale_time))
}
fn set_time_scale(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
    let scale = arr[1].as_f64().unwrap();
    world.write_resource::<Time>().set_time_scale(scale as f32);
    Ok(Value::Null)
}

fn get_absolute_time(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world(value.as_i64().unwrap() as u32,state);
    let absolute_time = world.read_resource::<Time>().absolute_time_seconds();
    Ok(Value::from(absolute_time))
}

fn delete_entity(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);
    let is_ok = world.delete_entity(entity).is_ok();
    Ok(Value::Bool(is_ok))
}

fn delete_all_children(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
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
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
    let loader = world.fetch_mut::<S2DLoader>();
    let asset_type:i64 = arr[1].as_i64().unwrap();
    let asset_path = arr[2].as_str().unwrap();
    let assert_id = match asset_type {
        1 => {
            let mut tex_config = ImageTextureConfig::default();
            tex_config.premultiply_alpha = true;
            loader.load_sync::<_, DefaultBackend>(TextuteLoaderInfo::new(asset_path,tex_config), world).map(|h| h.id())
        },
        2 => {
            let mut tex_config = ImageTextureConfig::default();
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

fn set_asset_root_path(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
    let path = arr[1].as_str().unwrap();

    world.fetch::<S2DLoader>().env().set_fs_root(path);
    Ok(Value::String(path.to_string()))
}

fn  add_transform(state: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
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
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
    let entity:Entity = world.entities().entity(arr[1].as_i64().unwrap() as u32);

    let storage = world.read_storage::<Transform>();
    
    let pos:Vec<Value> = if let Some(t) = storage.get(entity) {
        f(t)
    } else {
        vec![0.into(),0.into(),0.into()]
    };
    Ok(Value::Array(pos))
}

fn  get_transform_position(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    get_transform_attr(state,value,z,|t| {
        let pos = t.position();
        vec![pos.x.into(),pos.y.into(),pos.z.into()]
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
    let world = get_mut_world(arr[0].as_i64().unwrap() as u32,state);
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
