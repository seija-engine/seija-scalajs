use deno_core::{JsRuntime,OpState,ZeroCopyBuf};
use crate::opts::{reg_json_op_sync,get_mut_world};
use serde_json::Value;
use seija::{math::{Vector2,Vector3}, s2d::layout::{ContentView, GridCell}, specs::{Entity, World, WorldExt, WriteStorage}};
use deno_core::error::AnyError;
use seija::render::{components::{ImageRender,Mesh2D,SpriteSheet,TextRender,LineMode,SpriteRender,ImageType,ImageFilledType},Transparent};
use seija::assets::Handle;
use seija::common::{Rect2D,AnchorAlign};
use seija::s2d::layout::{LayoutElement,LayoutAlignment,View,Stack,Grid,LNumber,Orientation,Thickness};
use byteorder::{ByteOrder,NativeEndian};


pub fn init_json_func(rt:&mut JsRuntime) {
    reg_json_op_sync(rt,"addImageRender", add_image_render);
    reg_json_op_sync(rt,"setImageColorRef", set_image_color);
    reg_json_op_sync(rt,"getImageColorRef", get_image_color);
    reg_json_op_sync(rt,"setImageTexture", set_image_texture);
    reg_json_op_sync(rt,"setImageType",set_image_type);
    reg_json_op_sync(rt,"setImageFilledValue",set_image_filled_value);

    reg_json_op_sync(rt,"addTextRender", add_text_render);
    reg_json_op_sync(rt,"setTextString", set_text_string);
    reg_json_op_sync(rt,"setTextColorRef", set_text_color);
    reg_json_op_sync(rt,"setTextFont", set_text_font);
    reg_json_op_sync(rt,"setTextFontSize", set_text_font_size);
    reg_json_op_sync(rt,"setTextAnchor", set_text_anchor);
    reg_json_op_sync(rt,"setTextLineMode", set_text_line_mode);

    reg_json_op_sync(rt,"addSpriteRender", add_sprite_render);
    reg_json_op_sync(rt,"setSpriteName", set_sprite_name);
    reg_json_op_sync(rt,"setSpriteColorRef",set_sprite_color);
    reg_json_op_sync(rt,"setSpriteSheet", set_sprite_sheet);
    reg_json_op_sync(rt,"setSpriteType",set_sprite_type);
    reg_json_op_sync(rt,"getSpriteColorRef", get_sprite_color);
    reg_json_op_sync(rt,"setSpriteFilledValue",set_sprite_filled_value);
    reg_json_op_sync(rt,"setSpriteSliceByConfig",set_sprite_slice_by_config);



    reg_json_op_sync(rt,"addRect2D",add_rect_2d);
    reg_json_op_sync(rt,"setRect2DSizeRef", set_rect2d_size);
    reg_json_op_sync(rt,"setRect2DAnchorRef", set_rect2d_anchor);
    reg_json_op_sync(rt,"getRect2DSizeRef", get_rect2d_size);
    reg_json_op_sync(rt,"getRect2DAnchorRef", get_rect2d_anchor);

    reg_json_op_sync(rt,"setTransparent", set_transparent);

    reg_json_op_sync(rt,"addLayoutView",add_layout_view);
    reg_json_op_sync(rt,"setLayoutMargin", set_layout_margin);
    reg_json_op_sync(rt,"setLayoutPadding", set_layout_padding);
    reg_json_op_sync(rt,"setLayoutHor", set_layout_hor);
    reg_json_op_sync(rt,"setLayoutVer", set_layout_ver);
    reg_json_op_sync(rt,"setLayoutSize", set_layout_size);
    reg_json_op_sync(rt,"setLayoutPosition", set_layout_position);
    reg_json_op_sync(rt,"addStackLayout",add_stack_layout);
    reg_json_op_sync(rt,"setStackOrientation",set_stack_orientation);
    reg_json_op_sync(rt,"setStackSpacing",set_stack_spacing);
    reg_json_op_sync(rt,"addGridLayout",add_grid_layout);
    reg_json_op_sync(rt,"addGridRow",add_grid_row);
    reg_json_op_sync(rt,"addGridCol",add_grid_col);
    reg_json_op_sync(rt,"setGridRows",set_grid_rows);
    reg_json_op_sync(rt,"setGridCols",set_grid_cols);
    reg_json_op_sync(rt,"addGridCell", add_grid_cell);
    reg_json_op_sync(rt,"setGridCell", set_grid_cell);
    reg_json_op_sync(rt,"addContentView",add_context_view);
    reg_json_op_sync(rt,"removeContentView",remove_context_view);
    
}


fn add_image_render(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let tex_id = arr[1].as_i64().map(|v| Handle::new(v as u32));

    let image_render = ImageRender::new(tex_id);
    let mut image_storage = world.write_storage::<ImageRender>();
    if image_storage.contains(entity) {
       return Ok(Value::Bool(false));
    }

    let mut mesh_storage = world.write_storage::<Mesh2D>();
    image_storage.insert(entity,image_render).unwrap();

    mesh_storage.insert(entity,Mesh2D::default()).unwrap();
    Ok(Value::Bool(true)) 
}

fn set_image_color(_: &mut OpState,value: Value,buffer:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let bytes =  &mut *buffer[0];
    let r  = NativeEndian::read_f32(bytes);
    let g = NativeEndian::read_f32(&mut bytes[4..8]);
    let b = NativeEndian::read_f32(&mut bytes[8..12]);
    let a = NativeEndian::read_f32(&mut bytes[12..16]);
    let mut image_storage = world.write_storage::<ImageRender>();
    let mimage = image_storage.get_mut(entity);
    if let Some(image) = mimage {
        image.set_color(r, g, b, a);
        update_mesh_2d(world, entity);
    }
    Ok(Value::Null)
}

fn get_image_color(_: &mut OpState,value: Value,buffer:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let bytes =  &mut *buffer[0];
    
    let mut image_storage = world.write_storage::<ImageRender>();
    let mimage = image_storage.get_mut(entity);
    if let Some(image) = mimage {
       let color = image.get_color();
       NativeEndian::write_f32_into(color, bytes);
       update_mesh_2d(world, entity);
    }
    Ok(Value::Null)
}

fn set_image_texture(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let tex_id = arr[1].as_i64().unwrap() as u32;
    let mut image_storage = world.write_storage::<ImageRender>();
    let mimage = image_storage.get_mut(entity);
    if let Some(image) = mimage {
        image.set_texture(Handle::new(tex_id));
        update_mesh_2d(world,entity);
    }
    Ok(Value::Null)
}

fn set_image_type(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut image_storage = world.write_storage::<ImageRender>();
    let mimage = image_storage.get_mut(entity);
    if let Some(image) = mimage {
       let img_type = json_to_image_type(&arr[1]);   
       image.set_type(img_type)
    }
    Ok(Value::Null)
}

fn set_image_filled_value(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let value = arr[1].as_f64().unwrap() as f32;

    let mut image_storage = world.write_storage::<ImageRender>();
    let mimage = image_storage.get_mut(entity);
    if let Some(image) = mimage {
        image.info_mut().set_fill_value(value);
        update_mesh_2d(world, entity);
    }
    Ok(Value::Null)
}

fn json_to_image_type(value:&Value) -> ImageType {
    if let Some(typ) = value.as_i64() {
        match typ {
            0 => ImageType::Simple,
            3 => ImageType::Tiled,
            _ => panic!("error image type")
        }
    } else {
        let arr = value.as_array().unwrap();
        match arr[0].as_i64().unwrap() {
            1 => ImageType::Sliced(arr[1].as_f64().unwrap() as f32,
                                   arr[2].as_f64().unwrap() as f32,
                                   arr[3].as_f64().unwrap() as f32,
                                   arr[4].as_f64().unwrap() as f32),
            2 => {
                let filled_type = ImageFilledType::from(arr[1].as_i64().unwrap() as u32);
                ImageType::Filled(filled_type,arr[2].as_f64().unwrap() as f32)
            },
            _ => panic!("error image type")
        }
    }
}

fn add_text_render(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let font_id = arr[1].as_i64().map(|v| Handle::new(v as u32));
    
    let mut storage = world.write_storage::<TextRender>();
    if storage.contains(entity) {
        return  Ok(Value::Bool(false));
    }
    let  text_render = TextRender::new(font_id);
    storage.insert(entity, text_render).unwrap();
    let mut mesh_storage = world.write_storage::<Mesh2D>();
    mesh_storage.insert(entity,Mesh2D::default()).unwrap();
    Ok(Value::Null)
}

fn set_text_string(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let string = arr[1].as_str().unwrap();
    let mut storage = world.write_storage::<TextRender>();
    if let Some(text) = storage.get_mut(entity) {
        text.set_text(string);
    }
    Ok(Value::Null)
}

fn set_text_color(_: &mut OpState,value: Value,buffer:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    
    let mut storage = world.write_storage::<TextRender>();
    if let Some(text) = storage.get_mut(entity) {
        let bytes = &mut *buffer[0];
        NativeEndian::read_f32_into(bytes, &mut text.color);
    }
    Ok(Value::Null)
}

fn set_text_font_size(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let font_size = arr[1].as_i64().unwrap() as i32;
    let mut storage = world.write_storage::<TextRender>();
    if let Some(text) = storage.get_mut(entity) {
        text.set_font_size(font_size);
    }
    Ok(Value::Null)
}

fn set_text_font(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let font_id = arr[1].as_i64().unwrap() as u32;
    let mut storage = world.write_storage::<TextRender>();
    if let Some(text) = storage.get_mut(entity) {
        text.font = Some(Handle::new(font_id));
    }
    Ok(Value::Null)
}

fn set_text_anchor(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let anchor:AnchorAlign = (arr[1].as_i64().unwrap() as u32).into();
    let mut storage = world.write_storage::<TextRender>();
    if let Some(text) = storage.get_mut(entity) {
        text.set_anchor(anchor);
    }
    Ok(Value::Null)
}

fn set_text_line_mode(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let line_mode:LineMode = (arr[1].as_i64().unwrap() as u32).into();
    let mut storage = world.write_storage::<TextRender>();
    if let Some(text) = storage.get_mut(entity) {
        text.set_line_mode(line_mode);
    }
    Ok(Value::Null)
}

fn add_sprite_render(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut storage = world.write_storage::<SpriteRender>();
    if storage.contains(entity) {
        return Ok(Value::Bool(false))
    }
    let sheet_id = arr[1].as_i64().map(|v| Handle::new(v as u32) );
    let sprite_name = arr[2].as_str();
    let sprite_render = SpriteRender::new(sheet_id, sprite_name);
    storage.insert(entity, sprite_render).unwrap();
    let mut mesh_storage = world.write_storage::<Mesh2D>();
    mesh_storage.insert(entity,Mesh2D::default()).unwrap();
    Ok(Value::Bool(true))
}

fn set_sprite_name(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let name = arr[1].as_str();
    let mut storage = world.write_storage::<SpriteRender>();
    if let Some(sprite) = storage.get_mut(entity) {
       sprite.set_sprite_name(name);
       update_mesh_2d(world, entity);
    }
    Ok(Value::Null)
}

fn set_sprite_color(_: &mut OpState,value: Value,buffer:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let mut storage = world.write_storage::<SpriteRender>();
    if let Some(sprite) = storage.get_mut(entity) {
        let bytes = &mut *buffer[0];
        let r =  NativeEndian::read_f32(bytes);
        let g =  NativeEndian::read_f32(&mut bytes[4..8]);
        let b =  NativeEndian::read_f32(&mut bytes[8..12]);
        let a =  NativeEndian::read_f32(&mut bytes[12..16]);
       sprite.set_color(r, g, b, a);
       update_mesh_2d(world, entity);
    }
    Ok(Value::Null)
}

fn get_sprite_color(_: &mut OpState,value: Value,buffer:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let mut storage = world.write_storage::<SpriteRender>();
    if let Some(sprite) = storage.get_mut(entity) {
        let bytes = &mut *buffer[0];
        let color = sprite.get_color();
        NativeEndian::write_f32_into(color, bytes);
    }
    Ok(Value::Null)
}

fn set_sprite_sheet(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let sheet = arr[1].as_i64().map(|v| Handle::new(v as u32));
    let mut storage = world.write_storage::<SpriteRender>();
    if let Some(sprite) = storage.get_mut(entity) {
       sprite.set_sprite_sheet(sheet);
       update_mesh_2d(world, entity);
    }
    Ok(Value::Null)
}

fn set_sprite_type(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut storage = world.write_storage::<SpriteRender>();
    if let Some(sprite) = storage.get_mut(entity) {
        sprite.set_type(json_to_image_type(&arr[1]))
    }
    Ok(Value::Null)
}

fn set_sprite_filled_value(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let value = arr[1].as_f64().unwrap() as f32;
    let mut storage = world.write_storage::<SpriteRender>();
    if let Some(sprite) = storage.get_mut(entity) {
        sprite.info_mut().set_fill_value(value);
        update_mesh_2d(world, entity);
    }
    Ok(Value::Null)
}

fn set_sprite_slice_by_config(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let value = arr[1].as_i64().unwrap() as usize;
    let mut storage = world.write_storage::<SpriteRender>();
    if let Some(sprite) = storage.get_mut(entity) {
        let sheet_storage = world.fetch::<seija::assets::AssetStorage<SpriteSheet>>();
        sprite.set_slice_type_by_cfg(value, &sheet_storage);
    }
    Ok(Value::Null)
}


fn add_rect_2d(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut storage = world.write_storage::<Rect2D>();
    if storage.contains(entity) {
        return  Ok(Value::Bool(false));
    }
    let width = arr[1].as_f64().unwrap() as f32;
    let height = arr[2].as_f64().unwrap() as f32;
    let a_x = arr[3].as_f64().unwrap() as f32;
    let a_y = arr[4].as_f64().unwrap() as f32;
    let rect2d = Rect2D::new(width, height, [a_x,a_y]);
    storage.insert(entity, rect2d).unwrap();
    Ok(Value::Bool(true))
}


fn set_transparent(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let b = arr[1].as_bool().unwrap();
    let mut storage = world.write_storage::<Transparent>();
    let has = storage.contains(entity);
    let is_succ = if b && !has  {
        storage.insert(entity, Transparent).is_ok()
    } else if !b && has {
        storage.remove(entity).is_some()
    } else {
        true
    };
    Ok(Value::Bool(is_succ))
}


fn add_layout_view(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let view = View::default();
    let elem = LayoutElement::View(view);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    if !elems.contains(entity) {
        elems.insert(entity, elem).unwrap();
        Ok(Value::Bool(true))
    } else {
        Ok(Value::Bool(false))
    }
}

fn set_layout_margin(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems = world.write_storage::<LayoutElement>();
    let elem = elems.get_mut(entity).unwrap();
    let l = arr[1].as_f64().unwrap();
    let t = arr[2].as_f64().unwrap();
    let r = arr[3].as_f64().unwrap();
    let b = arr[4].as_f64().unwrap();
    elem.fview_mut(|view| {
        view.margin = Thickness::new(l,t,r,b);
    });
    Ok(Value::Null)
}

fn set_layout_padding(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems = world.write_storage::<LayoutElement>();
    let elem = elems.get_mut(entity).unwrap();
    let l = arr[1].as_f64().unwrap();
    let t = arr[2].as_f64().unwrap();
    let r = arr[3].as_f64().unwrap();
    let b = arr[4].as_f64().unwrap();
    elem.fview_mut(|view| {
        view.padding = Thickness::new(l,t,r,b);
    });
    Ok(Value::Null)
}

fn set_layout_hor(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems = world.write_storage::<LayoutElement>();
    let elem = elems.get_mut(entity).unwrap();
    let hor:u32 = arr[1].as_u64().unwrap() as u32;
    elem.fview_mut(|view| {
        view.hor = LayoutAlignment::from(hor);
    });
    Ok(Value::Null)
}

fn set_layout_ver(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems = world.write_storage::<LayoutElement>();
    let elem = elems.get_mut(entity).unwrap();
    let ver:u32 = arr[1].as_u64().unwrap() as u32;
    elem.fview_mut(|view| {
        view.ver = LayoutAlignment::from(ver);
    });
    Ok(Value::Null)
}

fn set_layout_size(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems = world.write_storage::<LayoutElement>();
    let elem = elems.get_mut(entity).unwrap();
    let w = arr[1].as_f64().unwrap();
    let h = arr[2].as_f64().unwrap();
    elem.fview_mut(|view| {
        view.size.set(Vector2::new(w,h));
    });
    Ok(Value::Null)
}


fn set_layout_position(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let arr = value.as_array().unwrap();
    let world = get_mut_world();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems = world.write_storage::<LayoutElement>();
    let elem = elems.get_mut(entity).unwrap();
    let x = arr[1].as_f64().unwrap() as f32;
    let y = arr[2].as_f64().unwrap() as f32;
    let z = arr[3].as_f64().unwrap() as f32;
    elem.fview_mut(|view| {
        view.pos.set(Vector3::new(x,y,z));
    });
    Ok(Value::Null)
}


fn add_stack_layout(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let stack = Stack::default();
    let elem = LayoutElement::StackLayout(stack);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    if !elems.contains(entity) {
        elems.insert(entity, elem).unwrap();
        Ok(Value::Bool(true))
    } else {
        Ok(Value::Bool(false))
    }
}

fn set_stack_orientation(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let arr = value.as_array().unwrap();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    let t:u32 = arr[1].as_u64().unwrap() as u32;
    match elems.get_mut(entity) {
        Some(LayoutElement::StackLayout(stack)) => {
            stack.orientation = Orientation::from(t);
        },
        _ => ()
    }
    Ok(Value::Null)
}

fn set_stack_spacing(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let arr = value.as_array().unwrap();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    let spacing:f32 = arr[1].as_f64().unwrap() as f32;
    match elems.get_mut(entity) {
        Some(LayoutElement::StackLayout(stack)) => {
            stack.spacing = spacing;
        },
        _ => ()
    }
    Ok(Value::Null)
}

fn add_grid_layout(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let grid = Grid::default();
    let elem = LayoutElement::GridLayout(grid);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    if !elems.contains(entity) {
        elems.insert(entity, elem).unwrap();
        Ok(Value::Bool(true))
    } else {
        Ok(Value::Bool(false))
    }
}

fn add_grid_row(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let arr = value.as_array().unwrap();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    let t:u64 = arr[1].as_u64().unwrap();
    let num:f32 = arr[2].as_f64().unwrap() as f32;
    match elems.get_mut(entity) {
        Some(LayoutElement::GridLayout(grid)) => {
            if t == 0 {
                grid.rows.push(LNumber::Const(num));
            } else {
                grid.rows.push(LNumber::Rate(num));
            }
        },
        _ => ()
    }
    Ok(Value::Null)
}

fn set_grid_rows(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let arr = value.as_array().unwrap();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    match elems.get_mut(entity)  {
        Some(LayoutElement::GridLayout(grid)) => {
            let rows_arr = arr[1].as_array().unwrap();
            let mut idx = 0;
            while idx <= rows_arr.len() {
               let t = rows_arr[idx].as_u64().unwrap();
               let num = rows_arr[idx + 1].as_f64().unwrap() as f32;
               if t == 0 {
                  grid.rows.push(LNumber::Const(num));
               } else {
                  grid.rows.push(LNumber::Const(num));
               }
               idx += 2
            }
            Ok(Value::Bool(false))
        }
        _ =>  Ok(Value::Bool(true))
    }
}

fn set_grid_cols(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let arr = value.as_array().unwrap();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    match elems.get_mut(entity)  {
        Some(LayoutElement::GridLayout(grid)) => {
            let cols_arr = arr[1].as_array().unwrap();
            let mut idx = 0;
            while idx <= cols_arr.len() {
               let t = cols_arr[idx].as_u64().unwrap();
               let num = cols_arr[idx + 1].as_f64().unwrap() as f32;
               if t == 0 {
                  grid.cols.push(LNumber::Const(num));
               } else {
                  grid.cols.push(LNumber::Const(num));
               }
               idx += 2
            }
            Ok(Value::Bool(false))
        }
        _ =>  Ok(Value::Bool(true))
    }
}

fn add_grid_col(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let arr = value.as_array().unwrap();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    let t:u64 = arr[1].as_u64().unwrap();
    let num:f32 = arr[2].as_f64().unwrap() as f32;
    match elems.get_mut(entity) {
        Some(LayoutElement::GridLayout(grid)) => {
            if t == 0 {
                grid.cols.push(LNumber::Const(num));
            } else {
                grid.cols.push(LNumber::Rate(num));
            }
        },
        _ => ()
    }
    Ok(Value::Null)
}

fn add_grid_cell(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let cell = GridCell::new(0, 0, 0, 0);
    let mut cells:WriteStorage<GridCell> = world.write_storage::<GridCell>();
    if !cells.contains(entity) {
        cells.insert(entity, cell).unwrap();
        Ok(Value::Bool(true))
    } else {
        Ok(Value::Bool(false))
    }
}



fn set_grid_cell(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let arr = value.as_array().unwrap();
    let entity = world.entities().entity( arr[0].as_i64().unwrap() as u32);
    let row = arr[1].as_i64().unwrap() as usize;
    let col = arr[2].as_i64().unwrap() as usize;
    let row_span = arr[3].as_f64().unwrap() as usize;
    let col_span = arr[4].as_f64().unwrap() as usize;
    let mut cells:WriteStorage<GridCell> = world.write_storage::<GridCell>();
    if let Some(cell) = cells.get_mut(entity) {
        cell.row = row;
        cell.col = col;
        cell.row_span = row_span;
        cell.col_span = col_span;
    }
    Ok(Value::Null)
}

fn add_context_view(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let content_view = ContentView::default();
    let elem = LayoutElement::ContentView(content_view);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    if !elems.contains(entity) {
        elems.insert(entity, elem).unwrap();
        Ok(Value::Bool(true))
    } else {
        Ok(Value::Bool(false))
   }
}

fn remove_context_view(_: &mut OpState,value: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let mut elems:WriteStorage<LayoutElement> = world.write_storage::<LayoutElement>();
    if elems.contains(entity) {
        elems.remove(entity).unwrap();
    }
    Ok(Value::Null)
}

fn opt_rect_attr_ref(_: &mut OpState,value: Value,buffer:&mut [ZeroCopyBuf],f:fn(&mut Rect2D,bytes:&mut [u8])) -> Result<Value, AnyError> {
    let world = get_mut_world();
    let entity = world.entities().entity( value.as_i64().unwrap() as u32);
    let mut storage = world.write_storage::<Rect2D>();
    let may_rect2d = storage.get_mut(entity);
    if let Some(rect2d) = may_rect2d {
        f(rect2d,&mut *buffer[0]);
        update_mesh_2d(world,entity);
    }
    Ok(Value::Null)
}

fn set_rect2d_anchor(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    opt_rect_attr_ref(state,value,z,|r,bytes| {
        let x  = NativeEndian::read_f32(bytes);
        let y = NativeEndian::read_f32(&mut bytes[4..8]);
        r.anchor = [x,y];
    })
}

fn set_rect2d_size(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    opt_rect_attr_ref(state,value,z,|r,bytes| {
        let x  = NativeEndian::read_f32(bytes);
        let y = NativeEndian::read_f32(&mut bytes[4..8]);
        r.width = x;
        r.height = y;
    })
}

fn get_rect2d_size(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    opt_rect_attr_ref(state,value,z,|r,bytes| {
        NativeEndian::write_f32(bytes,r.width);
        NativeEndian::write_f32(&mut bytes[4..8], r.height);
    })
}

fn get_rect2d_anchor(state: &mut OpState,value: Value,z:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    opt_rect_attr_ref(state,value,z,|r,bytes| {
        NativeEndian::write_f32(bytes,r.anchor[0]);
        NativeEndian::write_f32(&mut bytes[4..8], r.anchor[1]);
    })
}


fn update_mesh_2d(world:&World,entity:Entity) {
    let mut mesh_storage = world.write_storage::<Mesh2D>();
    let mesh = mesh_storage.get_mut(entity);
    if let Some(m) = mesh {
        m.is_dirty = true;
    }
}