
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

/**
 *
 */

/**
 * @author Administrator
 *
 */
//窗体类
public class MyFrame extends JFrame {

    /**
     * @param args
     */
    char charA;

    public MyFrame() {
        this.setSize(500, 100);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("my jframe");
        this.setVisible(true);
        this.addKeyListener(new MyKeyListener());


    }


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new MyFrame();
    }

}

//监听键盘类
class MyKeyListener extends KeyAdapter {

    public void keyPressed(KeyEvent e) {
        char charA = e.getKeyChar();
        System.out.println("你按了《" + charA + "》键");
    }
}
class listen extends KeyAdapter {
    private String name = "用户1";
    @Override
    public void keyPressed(KeyEvent e) {
        char charA = e.getKeyChar();
        System.out.println("你按了《"+charA+"》键");
        //生成36到37的随机数
        //Math.random()    [0,1]
        double tempre = 36 + Math.random();
        //显示并且在出入记录增加一条
        switch (charA) {
            case '1':
                name ="用户1";
                break;
            case '2':
                name="用户2";
                break;
            case '3':
                name = "用户3";
                break;
            case '4':
                name = "用户4";
                break;
            case '5':
                name = "用户5";
                break;
            case '6':
                name = "用户6";
                break;
            case '7':
                name = "用户7";
                break;
            case '8':
                name = "用户8";
                break;
            case '9':
                name = "用户9";
                break;
            default:
                break;
        }
    }
    public String getName(){return this.name;}
}