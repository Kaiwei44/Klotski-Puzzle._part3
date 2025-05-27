package tool;

import java.awt.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class tool {
    //加载背景图
    public static BufferedImage BACKGROUND;
    //加载头像的图片
    public static BufferedImage CAOCAO_IMAGE;
    public static BufferedImage GUANYU_IMAGE;
    public static BufferedImage ZHANGFEI_IMAGE;
    public static BufferedImage ZHAOYUN_IMAGE;
    public static BufferedImage HUANGZHONG_IMAGE;
    public static BufferedImage MACHAO_IMAGE;
    public static BufferedImage SOLDIER1_IMAGE;
    public static BufferedImage SOLDIER2_IMAGE;
    public static BufferedImage SOLDIER3_IMAGE;
    public static BufferedImage SOLDIER4_IMAGE;

    static {
        try {
            CAOCAO_IMAGE = ImageIO.read(tool.class.getResource("/resources/caocao.bmp"));
            GUANYU_IMAGE = ImageIO.read(tool.class.getResource("/resources/guanyu.bmp"));
            ZHAOYUN_IMAGE = ImageIO.read(tool.class.getResource("/resources/zhaoyun.bmp"));
            ZHANGFEI_IMAGE = ImageIO.read(tool.class.getResource("/resources/zhangfei.bmp"));
            HUANGZHONG_IMAGE = ImageIO.read(tool.class.getResource("/resources/huangzhong.bmp"));
            MACHAO_IMAGE = ImageIO.read(tool.class.getResource("/resources/machao.bmp"));

            SOLDIER1_IMAGE = ImageIO.read(tool.class.getResource("/resources/soldier1.bmp"));
            SOLDIER2_IMAGE = ImageIO.read(tool.class.getResource("/resources/soldier2.bmp"));
            SOLDIER3_IMAGE = ImageIO.read(tool.class.getResource("/resources/soldier3.bmp"));
            SOLDIER4_IMAGE = ImageIO.read(tool.class.getResource("/resources/soldier4.bmp"));

            BACKGROUND = ImageIO.read(tool.class.getResource("/resources/background.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //login, register界面尺寸相关的
    public static final int LOGIN_FRAME_W=700;
    public static final int LOGIN_FRAME_H=400;
    public static final int RETURN_X=(int)(LOGIN_FRAME_W/25);
    public static final int USERNAME_PASSWORD_X=(int)(0.3*LOGIN_FRAME_W) ;
    public static final int W_USERNAME_PASSWORD=(int)(0.2*LOGIN_FRAME_W);
    public static final int W_USERNAME_PASSWORD_WINDOW=(int)(6*LOGIN_FRAME_W/25);//120
    public static final int W_BUTTON=(int)(0.2*LOGIN_FRAME_W);//100
    public static final int H_ALL=(int)(2*LOGIN_FRAME_H/15);
    public static final int USERNAME_Y=(int)(2*LOGIN_FRAME_H/15);//40
    public static final int PASSWORD_Y=(int)(1*LOGIN_FRAME_H/3);
    public static final int BUTTON_Y=(int)(8*LOGIN_FRAME_H/15);
    public static final int REGISTER_X=(int)(13*LOGIN_FRAME_W/25);
    public static final int PLAY_AS_GUEST_x=(int)(0.3*LOGIN_FRAME_W);
    public static final int PLAY_AS_GUEST_y=(int)(7*LOGIN_FRAME_H/30);
    public static final int LOGIN_y=(int)(6*LOGIN_FRAME_H/10);
    public static final int PLAY_AS_GUEST_w=(int)(9*LOGIN_FRAME_W/25);

    //game panel尺寸相关的
    public static final int GRID_SIZE=100;

    //不同的玩法相关的
    public static final int[][] hengdaolima_1 = {//横刀立马
            {3, 7, 7, 5},
            {3, 7, 7, 5},
            {4, 2, 2, 6},
            {4, 0, 0, 6},
            {1, 1, 1, 1}
    };
    public static final int[][] zhihuiruoding_1={//指挥若定
            {3, 7, 7, 5},
            {3, 7, 7, 5},
            {1, 2, 2, 1},
            {4, 1, 1, 6},
            {4, 0, 0, 6}
    };
    public static final int[][] jiangyongcaoying_1={//将拥曹营
            {0, 7, 7, 0},
            {3, 7, 7, 5},
            {3, 4, 6, 5},
            {1, 4, 6, 1},
            {2, 2, 1, 1}
    };
    public static final int[][] qitoubingjin_1={//齐头并进
            {3, 7, 7, 5},
            {3, 7, 7, 5},
            {1, 1, 1, 1},
            {4, 2, 2, 6},
            {4, 0, 0, 6}
    };
    public static final int[][] bingfensanlu_1={//兵分三路
            {1, 7, 7, 1},
            {3, 7, 7, 5},
            {3, 2, 2, 5},
            {4, 1, 1, 6},
            {4, 0, 0, 6}
    };
    public static final int[][] jiezuxiandeng_1={//捷足先登
            {1, 7, 7, 1},
            {1, 7, 7, 1},
            {0, 2, 2, 0},
            {4, 5, 3, 6},
            {4, 5, 3, 6}
    };
    public static final int[][] zuoyoububing_1={//左右布兵
            {1, 7, 7, 1},
            {1, 7, 7, 1},
            {4, 2, 2, 6},
            {4, 1, 1, 6},
            {0, 2, 2, 0}
    };
    public static final int[][] weierbujian_1={//围而不坚
            {3, 7, 7, 1},
            {3, 7, 7, 1},
            {5, 2, 2, 1},
            {5, 4, 6, 1},
            {0, 4, 6, 0}
    };
    public static final int[][] chachinanfei_2={//插翅难飞
            {3, 7, 7, 1},
            {3, 7, 7, 1},
            {5, 5, 1, 1},
            {4, 2, 2, 6},
            {4, 0, 0, 6}
    };
    public static final int[][] shoukouruping_2={//守口如瓶
            {3, 7, 7, 5},
            {3, 7, 7, 5},
            {1, 4, 0, 1},
            {1, 4, 0, 1},
            {2, 2, 6, 6}
    };
    public static final int[][] jinzaizhichi_2={//近在咫尺
            {1, 5, 3, 6},
            {1, 5, 3, 6},
            {4, 4, 1, 1},
            {2, 2, 7, 7},
            {0, 0, 7, 7}
    };
    public static final int[][] wujiangbigong_3={//五将逼供
            {3, 3, 5, 5},
            {4, 7, 7, 6},
            {4, 7, 7, 6},
            {1, 2, 2, 1},
            {1, 0, 0, 1}
    };


    //public static final Image BACKGROUND=new ImageIcon("/resources/background.png").getImage();

    /*public static void main(String[] args) {
        // 创建主窗口
        JFrame frame = new JFrame("显示背景图片");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // 设置窗口大小

        // 创建自定义面板来绘制背景
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制背景图片（自动缩放以适应面板大小）
                g.drawImage(CAOCAO_IMAGE, 0, 0, getWidth(), getHeight(), this);
            }
        };

        // 添加面板到窗口
        frame.add(panel);

        // 居中显示窗口
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        int x=(int)(GRID_SIZE*7/5);
        System.out.println(x);
    }*/

}
