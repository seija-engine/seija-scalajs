use std::{env, fs};
use crate::opts::{reg_json_op_sync};
use deno_core::{JsRuntime,OpState,ZeroCopyBuf};
use env::home_dir;
use serde_json::Value;
use deno_core::error::AnyError;

pub fn init_fs_func(rt:&mut JsRuntime) {
    reg_json_op_sync(rt, "fs_root", fs_root);
    reg_json_op_sync(rt, "fs_home", fs_home);
    reg_json_op_sync(rt, "fs_pwd", fs_pwd);
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

#[test]
fn ttt() {
    
    let f = home::home_dir().unwrap();
    dbg!(f);
}