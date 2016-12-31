package com.tang.intellij.lua.debugger.mobdebug;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.io.BaseOutputReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.concurrent.Future;

/**
 *
 * Created by TangZX on 2016/12/30.
 */
public class MobServer implements Runnable {

    public interface Listener {
        void handleResp(int code, String[] params);
    }

    class LuaDebugReader extends BaseOutputReader {
        LuaDebugReader(@NotNull InputStream inputStream, @Nullable Charset charset) {
            super(inputStream, charset);
            start(getClass().getName());
        }

        @Override
        protected void onTextAvailable(@NotNull String s) {
            String[] list = s.split(" ");
            String[] params = Arrays.copyOfRange(list, 1, list.length - 1);
            int code = Integer.parseInt(list[0]);
            MobServer.this.listener.handleResp(code, params);
        }

        @NotNull
        @Override
        protected Future<?> executeOnPooledThread(@NotNull Runnable runnable) {
            return ApplicationManager.getApplication().executeOnPooledThread(runnable);
        }
    }

    private final Object locker = new Object();
    private ServerSocket server;
    private Thread thread;
    private Thread threadSend;
    private Listener listener;
    private PriorityQueue<String> commands = new PriorityQueue<>();
    private LuaDebugReader debugReader;

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
            debugReader = new LuaDebugReader(accept.getInputStream(), Charset.defaultCharset());

            threadSend = new Thread(() -> {
                try {
                    OutputStreamWriter stream = new OutputStreamWriter(accept.getOutputStream());
                    boolean firstTime = true;

                    while (accept.isConnected()) {
                        String command;
                        synchronized (locker) {
                            while (commands.size() > 0) {
                                command = commands.poll();
                                stream.write(command + "\n");
                                stream.flush();
                            }
                            if (firstTime) {
                                firstTime = false;
                                stream.write("RUN\n");
                                stream.flush();
                            }
                        }
                    }

                    System.out.println("disconnect");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threadSend.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (thread != null)
            thread.interrupt();
        if (threadSend != null)
            threadSend.interrupt();
        if (debugReader != null)
            debugReader.stop();
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
}
