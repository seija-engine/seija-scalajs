## 结构梳理

####  
1. 一个Control可以使用不同的Template，但是一个Control同时只能有一个Template
2. Control对应FRP里面的Behavior
3. Control组成树，由于只有一个Template没有大问题
5. 属性和dataContent:T == Behavior<MyControl<T>>
6. Event 如何传递？ eventRecv属性从父Control接受事件（可过滤和转换）
                   Control 可以导出任意名称的属性在特殊时候触发，然后通过(emit )给父控件
7. 路径和名称如何对应？ 还是通过xmlns
UISystem
  controls
  create(xxxPath:String)

IBehavior
  handleEvent(data:SExpr):Unit

Control
 Property:Map<String,Any>
 dataContent:Any
 template

ImageControl
  init():Unit = {
      this.property("position")
      this.property("scale")
      this.property("rotation")
  }

  handleEvent(data:SList):Unit = {
      data.first()

      this.emit([:UpdateColor v])
  }


ListControl
  init():Unit = {
      this.property("dataSource",[])

  }

```
<ImageControl>
    <Template>
       <Entity>
         <Components>
            <Transform />
            <EventNode click="(emit [:Click])" />
            <Rect2D size="(attr :position)" />
            <ImageRender color="(bind-p :color :UpdateColor)" />
         </Conponents>
       </Entity>
    </Template>
</ImageControl>


<XXXPanel>
   <TextList dataSource="[]" eventRecv="(filter :xx)"  />

   <Button onClick="(emit :AddOne)" >
</XXXPanel>
```