package siongsng.rpmtwupdatemod.notice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class noticeGet {
    public static String get() {
        StringBuilder text = new StringBuilder();
        try {
            URL urlObject = new URL("https://raw.githubusercontent.com/SiongSng/ResourcePack-Mod-zh_tw/main/notice.txt");
            URLConnection uc = urlObject.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                text.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}