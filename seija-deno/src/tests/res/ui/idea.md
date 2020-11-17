## 结构梳理
先放两个例子
```
<Control xmlns:core="/control/core">
    <ContentTemplate>
        <Image scale="0,0,0" texture="(env res.star)" color="#ff0000" />
    </ContentTemplate>
</Control>
```

```
<!--Image.xml-->
<Control>
    <ContentTemplate>
        <Entity>
            <Components>
                <Transform position="(param position)" scale="(param scale)" />
                <Rect2D size="(param size)" />
                <ImageRender texture = "(data a.c.b)" color="(ev-bind :Color)" />
            </Components>
        </Entity>
    </ContentTemplate>
</Control>
```

* 首先放弃了使用&lt;Ref src="xxx.xml"&gt;这种填路径的方式,以xmlns:xx="/a/b/c"这种方式，xx表示命名空间，"/a/b/c"是文件夹路径。

* Control的显示必须由Template提供。ContentTemplate是每个Control默认的Template。

### 几个问题：
#### 1. Control如何映射到Scala的类  
这个问题粗暴一点可以通过注册"core.Image" => ImageCreater 这种方式解决

#### 2. Template如何使用Control的属性  
这个问题比较复杂,还涉及到自定义Control的属性如何声明和处理  

真的应该允许Template访问Control的属性吗？
如果按照Template和Control完全事件通信的话，应该是通过事件绑定

或者假如说有一个CheckBox,
