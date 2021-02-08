use deno_core::{FsModuleLoader, JsRuntime, ModuleSpecifier, RuntimeOptions, futures};
mod opts;
mod core;
use std::rc::Rc;
use clap::{App,Arg};

fn main() {
    let mut rt_ops:RuntimeOptions = Default::default();
    rt_ops.module_loader = Some(Rc::new(FsModuleLoader));
    let mut runtime = JsRuntime::new(rt_ops);
    opts::init(&mut runtime);
    let matches = App::new("seija").arg(Arg::with_name("file").short("f").default_value("./index.js")).get_matches();
    
    let file_name = matches.value_of("file").unwrap();
   
    let indexjs = std::fs::read_to_string(file_name).unwrap();
    let specifier = ModuleSpecifier::resolve_path("index.js").unwrap();
   

    let module_id = futures::executor::block_on(
        runtime.load_module(&specifier, Some(indexjs)),
      ).unwrap();
    futures::executor::block_on(runtime.mod_evaluate(module_id)).unwrap();
}


