package com.tang.intellij.lua.debugger.mobdebug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class MobServer implements Runnable {

    private ServerSocket server;
    private Thread thread;

    public void start() throws IOException {
        if (server == null)
            server = new ServerSocket(8172);
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            Socket accept = server.accept();
            InputStreamReader reader = new InputStreamReader(accept.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(reader);

            OutputStreamWriter stream = new OutputStreamWriter(accept.getOutputStream());
            stream.write("STEP\n");
            stream.flush();
            stream.write("STEP\n");
            stream.flush();
            stream.write("STEP\n");
            stream.flush();
            stream.write("STEP\n");
            stream.flush();
            stream.write("STEP\n");
            while (true) {
                String line = bufferedReader.readLine();
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        thread.interrupt();
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
