# TransAndroidSqliteDBDemo
程序里有Android端和C#端，用于将Andriod本地的Sqlite数据库传输到PC，通过PC端查询数据用使用  
已在桌面C#端加入了简单的查询分析及修改数据的功能。  

在开发初期，当Android端嵌入在硬件中，并且本地数据库单机业务逻辑挺多，往往要分析数据是否处理正常，需要直接从数据库中查看，这时我们一般都是将数据库拷贝到PC端后查看分析，在虚拟机中可以，但是真机无法直接访问Android端data/data/包名/databases的数据库路径，所以做了一个小Demo，通过网络将本地数据库文件传到PC端。  

通讯方式使用的是NanoMsg  
C#程序目录为NanoRecvDataBase，做为接收端，使用的Nuget中的NNanoMsg  
Android程序目录为RoomDemo，做为服务端发送，使用的是自己的开源通讯库VnanoMsg，还有JetPack组件的Room及消息组件LiveEventBus  

2021-11-17 做了个对Android本地数据库简单的查询分析器的功能。在C#桌面端加入了输入框，可以直接写简单的查询及修改语句。  
2022-06-29 Android端升级到sdk31，加入了本地数据库备份和还原的功能，为了防止在程序包目录备份，如果手动点击清除数据后，备份文件也删除的不意味着，使用了外部存储，并针对Android 11的所有文件存储的适配权限做的动态申请。  
