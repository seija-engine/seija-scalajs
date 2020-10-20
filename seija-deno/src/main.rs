use deno_core::JsRuntime;
mod opts;
mod core;
use clap::{App,Arg};

fn main() {
    let mut runtime = JsRuntime::new(Default::default());
    opts::init(&mut runtime);
    let matches = App::new("seija").arg(Arg::with_name("file").short("f").default_value("./index.js")).get_matches();
    
    let file_name = matches.value_of("file").unwrap();
   
    let indexjs = std::fs::read_to_string(file_name).unwrap();
    runtime.execute("<top>", &indexjs).unwrap();

   
}


