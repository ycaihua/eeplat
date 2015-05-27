> EEPlat多应用模式，是指EEPlat中可以创建多个工程，而单应用模式是不推荐建立多个工程的。EEPlat多应用模式只在商业版中提供。

> 下面介绍EPlat多应用模式与单应用模式的两点不同：

> ### 创建工程 ###
> 应用程序服务器启动后，访问地址` http://{SITE}:{PORT}/eeplat（如:http://127.0.0.1/eeplat/） `。

> 进入了创建第一个工程的界面，如下图：
> <img src='http://eeplat.googlecode.com/files/m_createproject.png' />
> 创建后自动进入控制台界面。

> 填写上述要求的值，注意用户名/密码一定要记牢，用来登录控制台界面。

> 控制台界面地址为：` http://{SITE}:{PORT}/eeplat/console.jsp（如：http://127.0.0.1/eeplat/console.jsp） ` 。

> 业务界面地址为：`  http://{SITE}:{PORT}/eeplat（如:http://127.0.0.1/eeplat/）,  登录用户名/密码为: u/1111`  。


> ### PostgreSQL支持 ###
> EEPlat多应用模式支持PostgreSQL，并且自带了PostgreSQL的JDBC Jar包。进入控制台界面，选择数据源管理，如下图：
> <img src='http://eeplat.googlecode.com/files/m_databases.png' />

> 然后选择方言为PostgreSQL的数据源，如下图：
> <img src='http://eeplat.googlecode.com/files/m_postgre.png' />

> 修改为正确的URL，用户名，密码， 如下图：
> <img src='http://eeplat.googlecode.com/files/m_postgre_pwd.png' />

> 保存修改后要重启应用程序服务器如tomcat。

> 其它操作可以参见wiki的其它部分。