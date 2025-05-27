package tool;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class MusicUtil {
    private static Clip bgmClip;

    // 播放背景音乐
    public static void playBGM(String path) {
        try {
            // 从资源文件加载音乐
            URL url = MusicUtil.class.getResource(path);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);

            // 获取音频格式并创建Clip
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioIn);

            // 设置循环播放并启动
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // 停止背景音乐
    public static void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }
}
