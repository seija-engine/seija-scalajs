use std::{env, fs, path::{Path,Component}};
use crate::opts::{reg_json_op_sync};
use deno_core::{JsRuntime, OpState, ZeroCopyBuf};
use path_absolutize::Absolutize;
use serde_json::Value;
use deno_core::error::AnyError;

pub fn init_fs_func(rt:&mut JsRuntime) {
    reg_json_op_sync(rt, "fsRoot", fs_root);
    reg_json_op_sync(rt, "fsHome", fs_home);
    reg_json_op_sync(rt, "fsPwd", fs_pwd);
    reg_json_op_sync(rt, "fsSplitPath", fs_split_path);
    reg_json_op_sync(rt, "fsJoinPath", fs_join_path);
    reg_json_op_sync(rt,"fsGetFileName",fs_get_filename);
    reg_json_op_sync(rt,"fsHasRoot",fs_has_root);
    reg_json_op_sync(rt,"fsStartWith",fs_start_with);
    reg_json_op_sync(rt,"fsEndWith",fs_end_with);
    reg_json_op_sync(rt,"fsCreateDir",fs_create_dir);
    reg_json_op_sync(rt,"fsCreateDirAll",fs_create_dir_all);
    reg_json_op_sync(rt,"fsIsDir",fs_is_dir);
    reg_json_op_sync(rt,"fsIsLink",fs_is_link);
    reg_json_op_sync(rt,"fsListDir",fs_list_dir);
}

fn fs_root(_: &mut OpState,_: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let s = env::current_dir().unwrap();
    let mut cur_path = s.as_path();
    while let Some(p) = cur_path.parent() {
        cur_path = p;
    }
    let out_string = String::from(cur_path.to_str().unwrap());
    Ok(Value::String(out_string))
}

fn fs_home(_: &mut OpState,_: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let home_path = home::home_dir().unwrap();
    let out_string = String::from(home_path.to_str().unwrap());
    Ok(Value::String(out_string))
}

fn fs_pwd(_: &mut OpState,_: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let pwd_path = env::current_dir().unwrap();
    let out_string = String::from(pwd_path.to_str().unwrap());
    Ok(Value::String(out_string))
}

fn fs_split_path(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let p = Path::new(values.as_str().unwrap()); 
    let mut comps = p.components();
    let mut out_vecs:Vec<Value> = vec![];
    while let Some(seg) = comps.next() {
        match seg {
            Component::Prefix(_) => (),
            Component::CurDir => out_vecs.push(Value::String(String::from("."))),
            Component::ParentDir => out_vecs.push(Value::String(String::from(".."))),
            Component::RootDir => (),
            Component::Normal(normal) => out_vecs.push(Value::String(String::from(normal.to_str().unwrap()))),
        }
    }
    Ok(Value::Array(out_vecs))
}

fn fs_join_path(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let array = values.as_array().unwrap();
    let src_path = Path::new(array[0].as_str().unwrap());
    let join_path = array[1].as_str().unwrap();
    let joined_path = src_path.join(join_path);
    let abs_path = joined_path.absolutize().unwrap();
    Ok(Value::String(String::from(abs_path.to_str().unwrap())))
}

fn fs_get_filename(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let p = Path::new(values.as_str().unwrap()); 
    let fname = String::from(p.file_name().unwrap().to_str().unwrap());
    Ok(Value::String(fname))
}

fn fs_has_root(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let p = Path::new(values.as_str().unwrap()); 
    Ok(Value::Bool(p.has_root()))
}

fn fs_start_with(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let array = values.as_array().unwrap();
    let src_path = Path::new(array[0].as_str().unwrap());
    let path = array[1].as_str().unwrap();
    Ok(Value::Bool(src_path.starts_with(path)))
}


fn fs_end_with(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let array = values.as_array().unwrap();
    let src_path = Path::new(array[0].as_str().unwrap());
    let path = array[1].as_str().unwrap();
    Ok(Value::Bool(src_path.ends_with(path)))
}

fn fs_create_dir(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    match std::fs::create_dir(values.as_str().unwrap()) {
        Err(err) => Ok(Value::String(err.to_string())),
        Ok(_) => Ok(Value::Bool(true))
    }
}

fn fs_create_dir_all(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    match std::fs::create_dir_all(values.as_str().unwrap()) {
        Err(err) => Ok(Value::String(err.to_string())),
        Ok(_) => Ok(Value::Bool(true))
    }
}

fn fs_is_dir(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let p = Path::new(values.as_str().unwrap()); 
    Ok(Value::Bool(p.is_dir()))
}

fn fs_is_link(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let p = Path::new(values.as_str().unwrap());
    match p.read_link() {
        Err(_) => Ok(Value::Bool(false)),
        Ok(_) => Ok(Value::Bool(true))
    }
}

fn fs_list_dir(_: &mut OpState,values: Value,_:&mut [ZeroCopyBuf]) -> Result<Value, AnyError> {
    let p = Path::new(values.as_str().unwrap());
    let mut out_path_list:Vec<Value> = vec![];
    if p.is_dir() {
        let iter = p.read_dir().expect("read_dir call failed");
        for info in iter {
            match info {
                Err(err) => eprintln!("{:?}",err),
                Ok(dir) => {
                   let path = dir.path();
                   let abs_path = path.absolutize().unwrap();
                   let abs_path_str = abs_path.to_str().unwrap();
                   let str = String::from(abs_path_str);
                   out_path_list.push(Value::String(str))
                }
            }
        }
    }
    Ok(Value::Array(out_path_list))
}


//

#[test]
fn ttt() {
    use std::path::Path;
    let p = Path::new("C:\\www\\..\\..\\..\\dd\\c.txt");
    let abs = p.absolutize().unwrap();
    dbg!(abs);
}