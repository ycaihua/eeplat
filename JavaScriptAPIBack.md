> 后台的Javascript可以调用所有的Java API，但是需要注意JavaScript的用法。

> 后台的Javascript的常用内置变量：

  * douser:当前的登陆用户
  * doform: 当前的提交的表单内容
  * doinstance:当前记录


> 常用内置变量用法：

> 内置变量.getValue("属性名称") 就可以获取属性对应的值，如获取当前登陆者的部门:
```
  var deptuid = douser.getValue("deptuid");

```


> 不常用的有：
  * docontext:全局Session,  对应JAVA类SessionContext
  * doservice:当前的服务,对应JAVA类 DOService
  * domodel:当前的模型,对应JAVA类 DOIModel

