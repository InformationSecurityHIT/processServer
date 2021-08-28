
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;


/**
 * @author Administrator
 */
//窗体类
public class MyFrame extends JFrame {
    public String name = "用户1";
    private cloudSever cloudSever;

    public MyFrame() {
        this.setSize(500, 100);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("my jframe");
        this.setVisible(true);
//        System.out.println(lis.getName());
    }
    public String getName(){return this.name;}
//    public static void main(String[] args) {
//        // TODO Auto-generated method stub
//        new MyFrame();
//    }


    public static void main(String[] args) {
        new cloudSever();
    }
}

