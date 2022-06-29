using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace NanoRecvDataBase
{
    internal class CTransStatus
    {
        //获取数据库名称
        public static string GetDBName = "1";
        //通讯数据库
        public static string TransDB = "2";
        //执行Sql脚本
        public static string ExecSql = "3";
        //获取日志文件
        public static string GetErrorLogName = "4";
        //通讯日志
        public static string TransLogFile = "5";
    }
}
