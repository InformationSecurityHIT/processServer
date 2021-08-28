import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class cloudSever {
    private ServerSocket serversocket = null;
    private Executor executor = Executors.newCachedThreadPool();//线程池;

    public cloudSever() {
        try {
            //创建服务端套接字，之后等待客户端连接
            serversocket = new ServerSocket(9999);
            while (!serversocket.isClosed()) {
                Socket acceptedSocket_tmp = serversocket.accept();
                System.out.println("连接成功");
                //启动处理线程
                executor.execute(new ReceiveSenderRunnable(acceptedSocket_tmp));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serversocket != null) {
                try {
                    serversocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //服务端接收数据线程
    private class ReceiveSenderRunnable implements Runnable {
        private Socket severSocket;
        private DataInputStream dataInput;
        private DataOutputStream dataOutput;
        //continue:发送的保持连接的短串
        //match:后接若干张图片
        //register_name:
        private String command = "continue";        //发送的保持连接的短串
        private final int match_size = 3;
        private final int register_size = 6;
        //TODO  存储位置
        private final String PATH = "D:\\InformationSecurityCompetition\\gameUse\\processServer\\";

        public ReceiveSenderRunnable(Socket s) throws IOException {
            this.severSocket = s;
            dataInput = new DataInputStream(s.getInputStream());
            dataOutput = new DataOutputStream(s.getOutputStream());
        }

        @Override
        public void run() {
            try {
//                cloudSever_sendIMG_toAndroid cloudSever_sendIMG=new cloudSever_sendIMG_toAndroid();
                while (true) {
                    int size = dataInput.readInt();//获取服务端发送的数据的大小
                    if (size <= 0) continue;
                    byte[] data = new byte[size];
                    int len = 0;
                    //将二进制数据写入data数组
                    while (len < size) {len += dataInput.read(data, len, size - len); }
                    if(size<30){
                        command = parse_command(data);
                        String command_copy = command.split("_")[0];
                        switch (command_copy){
                            case "continue":
                                break;
                            case "match":
                                //h
                                for(int index=0;index<match_size;index++){
                                    int temp_size = dataInput.readInt();
                                    if(temp_size<=0){index--;continue;}
                                    byte[] temp_data = new byte[temp_size];
                                    int temp_len = 0;
                                    while (temp_len<temp_size){temp_len += dataInput.read(temp_data,temp_len,temp_size-temp_len);}
                                    if(temp_size < 30){System.out.println("error,match 命令后不会跟小于20的");}
                                    parse_image(temp_data,"temp\\before\\"+index+".jpg");

                                }
                                //调用脚本去匹配
                                String[] args = new String[]{"python",PATH+"process\\process_one_img_for_match.py",
                                        PATH+"temp\\before",PATH+"temp\\after"};
                                Process proc = Runtime.getRuntime().exec(args);
                                String line = null;
                                String tmp;
                                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                                while ((tmp = in.readLine()) != null) {
                                    line = tmp;
                                    System.out.println(line);
                                }
                                in.close();
                                proc.destroy();
//                                send_message(line);
                                break;
                            case "register":
                                String name = command.split("_")[1];
                                File f = new File("img\\data_before\\"+name);
                                if(!f.exists()) f.mkdir();
                                for(int index=0;index<register_size;index++){
                                    int temp_size = dataInput.readInt();
                                    if(temp_size<=0){index--;continue;}
                                    byte[] temp_data = new byte[temp_size];
                                    int temp_len = 0;
                                    while (temp_len<temp_size){temp_len += dataInput.read(temp_data,temp_len,temp_size-temp_len);}
                                    if(temp_size < 30){System.out.println("error,register 命令后不会跟小于20的");}
                                    parse_image(temp_data,"img\\data_before\\"+name+"\\"+index+".jpg");
                                }
                                //调用脚本去处理录入图片
                                String[] args_register = new String[]{"python",PATH+"process\\process_all_imgs_for_register.py",
                                        PATH+"img\\data_before\\name",
                                        PATH+"img\\data_after\\name",
                                        PATH+"img\\keyPoint\\name"};
                                Process proc_register = Runtime.getRuntime().exec(args_register);
                                String line_register;
                                String tmp_register;
                                BufferedReader in_register = new BufferedReader(new InputStreamReader(proc_register.getInputStream()));
                                while ((tmp_register = in_register.readLine()) != null) {
                                    line_register = tmp_register;
                                    System.out.println(line_register);
                                }
                                in_register.close();
                                proc_register.destroy();
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //解析命令
        public String parse_command(byte[] data){
            return new String(data);
        }
        //解析图片:数据  存储路径
        public void parse_image(byte[] data,String path){
            try (FileOutputStream fileOutputStream =
                     new FileOutputStream(new File(path))){
                fileOutputStream.write(data);
                System.out.println("success");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //发送数据
        private void send_message(String message) {
            try {
                byte[] tmp = message.getBytes();
                dataOutput.writeInt(tmp.length);
                dataOutput.write(tmp, 0, tmp.length);
                dataOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        new cloudSever();
    }
}
