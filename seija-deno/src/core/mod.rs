pub mod event;
pub mod game;
use deno_core::v8;

pub trait ToJsValue {
    fn to<'a>(&self,scope:&mut v8::HandleScope<'a>) -> v8::Local<v8::Value>;
}