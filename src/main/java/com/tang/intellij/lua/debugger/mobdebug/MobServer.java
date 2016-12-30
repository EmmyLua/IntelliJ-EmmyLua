package com.tang.intellij.lua.debugger.mobdebug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class MobServer implements Runnable {

    public interface Listener {
        void handleResp(int code, String[] params);
    }

    private final Object locker = new Object();
    private ServerSocket server;
    private Thread thread;
    private Thread threadSend;
    private Thread threadRecv;
    private Listener listener;
    private PriorityQueue<String> commands = new PriorityQueue<>();

    public MobServer(Listener listener) {
        this.listener = listener;
    }

    public void start() throws IOException {
        if (server == null)
            server = new ServerSocket(8172);
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            final Socket accept = server.accept();
            threadSend = new Thread(() -> {
                try {
                    OutputStreamWriter stream = new OutputStreamWriter(accept.getOutputStream());
                    stream.write("STEP\n");
                    stream.flush();

                    while (accept.isConnected()) {
                        String command;
                        synchronized (locker) {
                            if (commands.size() > 0) {
                                command = commands.poll();
                                stream.write(command + "\n");
                                stream.flush();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threadSend.start();

            threadRecv = new Thread(() -> {
                try {
                    InputStreamReader reader = new InputStreamReader(accept.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] list = line.split(" ");
                        int code = Integer.parseInt(list[0]);
                        String[] params = Arrays.copyOfRange(list, 1, list.length);
                        listener.handleResp(code, params);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threadRecv.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        thread.interrupt();
        if (threadSend != null)
            threadSend.interrupt();
        if (threadRecv != null)
            threadRecv.interrupt();
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCommand(String command) {
        synchronized (locker) {
            commands.add(command);
        }
    }

    public void addBreakpoint(String file, int line) {
        addCommand(String.format("SETB %s %d", file, line));
    }
}
