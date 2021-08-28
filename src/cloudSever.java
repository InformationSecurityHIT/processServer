import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class cloudSever {
    private ServerSocket serversocket = null;
    private Executor executor = Executors.newCachedThreadPool();//线程池;
    private String myName = null;
    private MyFrame myFrame;

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public cloudSever() {
        myFrame=new MyFrame();
        myFrame.addKeyListener(new listen());
        try {
            //创建服务端套接字，之后等待客户端连接
            serversocket = new ServerSocket(4444);
//            new Thread(new ListenRunnable()).start();
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
    public class ReceiveSenderRunnable implements Runnable {
        private Socket severSocket;
        private DataInputStream dataInput;
        private DataOutputStream dataOutput;
        //continue:发送的保持连接的短串
        //match:后接若干张图片
        //register_name:
        private String command = "continue";        //发送的保持连接的短串
        private final int match_size = 3;
        private final int register_size = 6;
        private boolean local = false;          //全局匹配还是本地匹配
        //TODO  存储位置
        private final String PATH = "D:\\InformationSecurityCompetition\\gameUse\\processServer\\";

        public ReceiveSenderRunnable(Socket s) throws IOException {
            this.severSocket = s;
            dataInput = new DataInputStream(s.getInputStream());
            dataOutput = new DataOutputStream(s.getOutputStream());
        }

        @Override
        public void run() {

            String dir = "";
            try {
//                cloudSever_sendIMG_toAndroid cloudSever_sendIMG=new cloudSever_sendIMG_toAndroid();
                while (true) {
                    int size = dataInput.readInt();//获取服务端发送的数据的大小
                    if (size <= 0) {
                        continue;
                    }
                    byte[] data = new byte[size];
                    int len = 0;
                    //将二进制数据写入data数组
                    while (len < size) {
                        len += dataInput.read(data, len, size - len);
                    }
                    if (size < 30) {
                        command = parse_command(data);
                        String command_copy = command.split("_")[0];
                        System.out.println(command_copy);
                        switch (command_copy) {
                            case "continue":
                                break;
                            case "match":
                                if (local) {
                                    dir = "device\\device0\\";
                                } else {
                                    dir = "device\\all_device\\";
                                }
                                for (int index = 0; index < match_size; index++) {
                                    int temp_size = dataInput.readInt();
                                    if (temp_size <= 0) {
                                        index--;
                                        continue;
                                    }
                                    byte[] temp_data = new byte[temp_size];
                                    int temp_len = 0;
                                    while (temp_len < temp_size) {
                                        temp_len += dataInput.read(temp_data, temp_len, temp_size - temp_len);
                                    }
                                    if (temp_size < 30) {
                                        System.out.println("error,match 命令后不会跟小于20的");
                                    }
                                    parse_image(temp_data, dir + "temp\\before\\" + index + ".jpg");
                                }
                                //调用脚本去匹配
                                String[] args = new String[]{"python", PATH + "process\\process_one_img_for_match.py",
                                        PATH + dir + "temp\\before", PATH + dir + "temp\\after"};
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
//                              //  send_message(line);
                                break;
                            case "register":
                                dir = "device\\device0\\";
                                String name = command.split("_")[1];
                                File f = new File(dir + "data_before\\" + name);
                                if (!f.exists()) {
                                    f.mkdir();
                                }
                                for (int index = 0; index < register_size; index++) {
                                    int temp_size = dataInput.readInt();
                                    if (temp_size <= 0) {
                                        index--;
                                        continue;
                                    }
                                    byte[] temp_data = new byte[temp_size];
                                    int temp_len = 0;
                                    while (temp_len < temp_size) {
                                        temp_len += dataInput.read(temp_data, temp_len, temp_size - temp_len);
                                    }
                                    if (temp_size < 30) {
                                        System.out.println("error,register 命令后不会跟小于20的");
                                    }
                                    parse_image(temp_data, dir + "data_before\\" + name + "\\" + index + ".jpg");
                                }
                                //调用脚本去处理录入图片
                                String[] args_register = new String[]{"python", PATH + "process\\process_all_imgs_for_register.py",
                                        PATH + dir + "data_before\\" + name,
                                        PATH + dir + "data_after\\" + name,
                                        PATH + dir + "keyPoint\\" + name};
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

                                copyFolder(PATH + dir + "data_before\\" + name, "device\\all_device\\data_before\\" + name);
                                copyFolder(PATH + dir + "data_after\\" + name, "device\\all_device\\data_after\\" + name);
                                copyFolder(PATH + dir + "keyPoint\\" + name, "device\\all_device\\keyPoint\\" + name);
                                break;
                            case "APP":
//                                myFrame.addKeyListener(new listen());
                                String user = myName;
                                System.out.println(user);
                                send_message(user);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //解析命令
        public String parse_command(byte[] data) {
            return new String(data);
        }

        //解析图片:数据  存储路径
        public void parse_image(byte[] data, String path) {
            try (FileOutputStream fileOutputStream =
                         new FileOutputStream(new File(path))) {
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
                myName = "1";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 复制文件夹
         *
         * @param resource 源路径
         * @param target   目标路径
         */
        private void copyFolder(String resource, String target) throws Exception {
            File resourceFile = new File(resource);
            if (!resourceFile.exists()) {
                throw new Exception("源目标路径：[" + resource + "] 不存在...");
            }
            File targetFile = new File(target);
            if (!targetFile.exists()) {
                throw new Exception("存放的目标路径：[" + target + "] 不存在...");
            }
            // 获取源文件夹下的文件夹或文件
            File[] resourceFiles = resourceFile.listFiles();
            for (File file : resourceFiles) {
                File file1 = new File(targetFile.getAbsolutePath() + File.separator + resourceFile.getName());
                // 复制文件
                if (file.isFile()) {
                    System.out.println("文件" + file.getName());
                    // 在 目标文件夹（B） 中 新建 源文件夹（A），然后将文件复制到 A 中
                    // 这样 在 B 中 就存在 A
                    if (!file1.exists()) {
                        file1.mkdirs();
                    }
                    File targetFile1 = new File(file1.getAbsolutePath() + File.separator + file.getName());
                    copyFile(file, targetFile1);
                }
                // 复制文件夹
                if (file.isDirectory()) {// 复制源文件夹
                    String dir1 = file.getAbsolutePath();
                    // 目的文件夹
                    String dir2 = file1.getAbsolutePath();
                    copyFolder(dir1, dir2);
                }
            }

        }

        /**
         * 复制文件
         *
         * @param resource
         * @param target
         */
        private void copyFile(File resource, File target) throws Exception {
            // 输入流 --> 从一个目标读取数据
            // 输出流 --> 向一个目标写入数据

            long start = System.currentTimeMillis();

            // 文件输入流并进行缓冲
            FileInputStream inputStream = new FileInputStream(resource);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            // 文件输出流并进行缓冲
            FileOutputStream outputStream = new FileOutputStream(target);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            // 缓冲数组
            // 大文件 可将 1024 * 2 改大一些，但是 并不是越大就越快
            byte[] bytes = new byte[1024 * 2];
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {
                bufferedOutputStream.write(bytes, 0, len);
            }
            // 刷新输出缓冲流
            bufferedOutputStream.flush();
            //关闭流
            bufferedInputStream.close();
            bufferedOutputStream.close();
            inputStream.close();
            outputStream.close();

            long end = System.currentTimeMillis();

            System.out.println("耗时：" + (end - start) / 1000 + " s");
        }
    }

    class listen extends KeyAdapter {
        private String name = "用户1";

        @Override
        public void keyPressed(KeyEvent e) {
            char charA = e.getKeyChar();
            System.out.println("你按了《"+charA+"》键");
            //显示并且在出入记录增加一条
            switch (charA) {
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    name="用户"+charA;
                    setMyName(name);
                    System.out.println(name);
                    break;
                default:
                    break;
            }
        }
        public String getName(){return this.name;}
    }
}
