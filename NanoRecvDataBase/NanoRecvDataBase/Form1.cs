using nanomsgclient;
using NNanomsg.Protocols;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace NanoRecvDataBase
{
    public partial class Form1 : Form
    {

        private PairSocket pairSocket = null;

        public Form1()
        {
            InitializeComponent();

            CheckForIllegalCrossThreadCalls = false;

            _tbMsg = tbMsg;
        }

        #region 文本框操作

        //定义文本框
        private static TextBox _tbMsg;

        //定义Action
        private Action<string> TextShowAction = new Action<string>(TextShow);

        //定义更新UI函数
        private static void TextShow(string sMsg)
        {
            //当文本行数大于500后清空
            if (_tbMsg.Lines.Length > 500)
            {
                _tbMsg.Clear();
            }

            string ShowMsg = DateTime.Now + "  " + sMsg + "\r\n";
            _tbMsg.AppendText(ShowMsg);

            //让文本框获取焦点 
            _tbMsg.Focus();
            //设置光标的位置到文本尾 
            _tbMsg.Select(_tbMsg.TextLength, 0);
            //滚动到控件光标处 
            _tbMsg.ScrollToCaret();
        }

        #endregion

        private void btnRecv_Click(object sender, EventArgs e)
        {
            try
            {
                if (pairSocket == null)
                {
                    pairSocket = new PairSocket();
                    pairSocket.Options.ReceiveTimeout = new TimeSpan(0, 0, 2);
                    var ipadr = tbipadr.Text;
                    TextShow("要连接的IP地址为：" + ipadr);
                    pairSocket.Connect(ipadr);
                }

                var cts = new CancellationTokenSource(3000);

                var res = new Task<string>(() =>
                {
                    bool ressend = pairSocket.SendImmediate(CreateTransBytes(CTransStatus.GetDBName));
                    if (ressend)
                    {
                        while (true)
                        {
                            if (cts.IsCancellationRequested)
                            {
                                throw new Exception("请求超时！");
                            }
                            Thread.Sleep(50);
                            //接收数据
                            byte[] buffer = pairSocket.ReceiveImmediate();
                            if (buffer != null)
                            {
                                string recvstr = Encoding.UTF8.GetString(buffer);
                                return recvstr;
                            }
                        }
                    }
                    else
                    {
                        throw new Exception("发送数据失败！");
                    }
                }, cts.Token);

                res.Start();
                var getdbnum = res.Result;
                var dbnames = getdbnum.Split('#');
                TextShow("接收到数据库文件个数：" + dbnames.Length);

                var resfile = new Task<String>(() =>
                {
                    for (int i = 0; i < dbnames.Length; ++i)
                    {
                        string filename = dbnames[i];
                        bool ressend = pairSocket.SendImmediate(CreateTransBytes(CTransStatus.TransDB, filename));
                        if (ressend)
                        {
                            while (true)
                            {
                                Thread.Sleep(50);
                                //接收数据
                                byte[] buffer = pairSocket.Receive();
                                if (buffer != null)
                                {
                                    var pathfile = "D:\\DataBase\\" + filename;
                                    FileHelper.ByteToFile(buffer, pathfile);
                                    TextShow(pathfile + "文件传输成功");
                                    break;
                                }
                            }
                        }
                        else
                        {
                            throw new Exception("发送数据失败！");
                        }
                    }
                    return "传输完成";
                });
                resfile.Start();

                TextShow(resfile.Result);


            }
            catch (Exception ex)
            {
                Exception exp = ExceptionExtensions.GetOriginalException(ex);
                TextShow(exp.Message);

            }
        }


        private byte[] CreateTransBytes(string transtype, string transmsg = "")
        {
            string transstr = transtype + "#" + transmsg;
            return Encoding.UTF8.GetBytes(transstr);
        }

        private void tsbtnclear_Click(object sender, EventArgs e)
        {
            tbSqlStr.Text = "";
        }

        private void tsbtnexec_Click(object sender, EventArgs e)
        {
            try
            {
                if(tbSqlStr.Text.Trim().Length == 0)
                {
                    TextShow("没有可执行的语句。");
                    return;
                }
                if (pairSocket == null)
                {
                    pairSocket = new PairSocket();
                    pairSocket.Options.ReceiveTimeout = new TimeSpan(0, 0, 2);
                    var ipadr = tbipadr.Text;
                    TextShow("要连接的IP地址为：" + ipadr);
                    pairSocket.Connect(ipadr);
                }

                var cts = new CancellationTokenSource(3000);

                var res = new Task<string>(() =>
                {
                    bool ressend = pairSocket.SendImmediate(CreateTransBytes(CTransStatus.ExecSql, tbSqlStr.Text));
                    if (ressend)
                    {
                        while (true)
                        {
                            if (cts.IsCancellationRequested)
                            {
                                throw new Exception("请求超时！");
                            }
                            Thread.Sleep(50);
                            //接收数据
                            byte[] buffer = pairSocket.ReceiveImmediate();
                            if (buffer != null)
                            {
                                string recvstr = Encoding.UTF8.GetString(buffer);
                                return recvstr;
                            }
                        }
                    }
                    else
                    {
                        throw new Exception("发送数据失败！");
                    }
                }, cts.Token);

                res.Start();
                TextShow(res.Result);

            }
            catch (Exception ex)
            {
                Exception exp = ExceptionExtensions.GetOriginalException(ex);
                TextShow(exp.Message);

            }
        }
    }
}
