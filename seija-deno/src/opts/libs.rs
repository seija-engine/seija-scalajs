use deno_core::v8 as v8;
use crate::opts::reg_v8_func;
use std::convert::TryFrom;
use quick_xml::{events::Event};
use std::io::{BufRead};

pub fn init_v8_func(scope: &mut v8::HandleScope,object:v8::Local<v8::Object>) {
    reg_v8_func(scope, object, "parseXML", parse_xml);
    reg_v8_func(scope, object, "parseXMLString", parse_xml_string);
}

fn parse_xml(scope: &mut v8::HandleScope,args: v8::FunctionCallbackArguments,mut ret: v8::ReturnValue) {
    let xml_string = v8::Local::<v8::String>::try_from(args.get(0));
    let path = xml_string.unwrap().to_rust_string_lossy(scope);
    let  mreader = quick_xml::Reader::from_file(path);
    
    if let Ok(reader) = mreader {
        xml_reader_to_v8(reader,scope,ret);
        return;
    }
    let error = format!("{}",mreader.err().unwrap()) ;
    ret.set(v8::String::new(scope, &error).unwrap().into())
}

fn parse_xml_string(scope: &mut v8::HandleScope,args: v8::FunctionCallbackArguments,ret: v8::ReturnValue) {
    let xml_string = v8::Local::<v8::String>::try_from(args.get(0));
    let rust_string = xml_string.unwrap().to_rust_string_lossy(scope);
    let reader = quick_xml::Reader::from_str(rust_string.as_str());
    
    xml_reader_to_v8(reader,scope,ret);
}


fn xml_reader_to_v8<T>(mut reader:quick_xml::Reader<T>,scope:&mut v8::HandleScope,mut ret: v8::ReturnValue) where T:BufRead {
    
    reader.trim_text(true);
    let mut buf = Vec::new();
    let mut stack:Vec<(v8::Local<v8::Object>,u32)> = vec![];
    let tag_name = v8::String::new(scope, "tag").unwrap().into();
    let children_name = v8::String::new(scope, "children").unwrap().into();
    let text_name = v8::String::new(scope, "text").unwrap().into();
    let attrs_name = v8::String::new(scope, "attrs").unwrap().into();
    let length_name = v8::String::new(scope, "_length").unwrap().into();
    fn set_attr(e:&quick_xml::events::BytesStart,scope: &mut v8::HandleScope,obj:&v8::Local<v8::Object>,key: v8::Local<v8::Value>) {
        let attr_object = v8::Object::new(scope);
        for eattr in e.attributes().into_iter() {
            if let Ok(attr) = eattr {
                let v8_key = v8::String::new_from_utf8(scope,  attr.key,v8::NewStringType::Normal).unwrap();
                let v8_value = v8::String::new_from_utf8(scope,  &attr.value,v8::NewStringType::Normal).unwrap();
                attr_object.set(scope, v8_key.into(), v8_value.into());
            }
        }
        obj.set(scope, key, attr_object.into());
    }
    loop {
        match reader.read_event(&mut buf) {
            Ok(Event::Start(ref e)) => {
                let object = v8::Object::new(scope);
                let v8_string = v8::String::new_from_utf8(scope,  e.name(),v8::NewStringType::Normal).unwrap();
                object.set(scope, tag_name, v8_string.into());
                let array = v8::Array::new(scope, 0);
                object.set(scope, children_name, array.into());

                if let Some((parent,len)) = stack.last_mut() {
                    let parent_children = parent.get(scope, children_name).unwrap().to_object(scope).unwrap();
                    parent_children.set_index(scope,*len, object.into());    
                    *len = *len + 1;     
                }

                set_attr(e,scope,&object,attrs_name);
                stack.push((object,0));
            },
            Ok(Event::Text(e)) => {
                if let Some((parent,_)) = stack.last_mut() {
                    let txt = e.unescape_and_decode(&reader).unwrap();
                    let v8_string = v8::String::new(scope,&txt).unwrap();
                    parent.set(scope, text_name, v8_string.into());
                }
            },
            Ok(Event::End(_)) => {
                let pop_elem = stack.pop();
                if pop_elem.is_some() {
                    let object = pop_elem.unwrap();
                    let children = object.0.get(scope, children_name).unwrap().to_object(scope).unwrap();
                    let v8_len  = v8::Number::new(scope, object.1 as f64);
                    children.set(scope, length_name, v8_len.into());
                    
                    if stack.len() == 0 {
                        ret.set(object.0.into());
                    }
                }
                
               
            },
            Ok(Event::Empty(ref empty)) => {
                let object = v8::Object::new(scope);
                let v8_string = v8::String::new_from_utf8(scope,  empty.name(),v8::NewStringType::Normal).unwrap();
                object.set(scope, tag_name, v8_string.into());
                if let Some((parent,len)) = stack.last_mut() {
                    let parent_children = parent.get(scope, children_name).unwrap().to_object(scope).unwrap();
                    parent_children.set_index(scope,*len, object.into());    
                    *len = *len + 1;     
                } else {
                    ret.set(object.into())
                }
                set_attr(empty,scope,&object,attrs_name);
            }
            Err(e) => panic!("Error at position {}: {:?}", reader.buffer_position(), e),
            Ok(Event::Eof) => {
              break;
            },
            _ => ()
        }
    }


}
