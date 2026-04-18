package com.perflab;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.perflab.commands.CommandDispatcher;
import com.perflab.resp.RespParser;
import com.perflab.resp.RespValue;

public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket socket = new ServerSocket(6379)) {
            while (true) {
                try (Socket client = socket.accept()) {
                    InputStream is = client.getInputStream();
                    OutputStream os = client.getOutputStream();
                    while (true) {
                        RespValue request = RespParser.parse(is);
                        if (request == null) {
                            break;
                        }
                        RespValue response = CommandDispatcher.handle(request);
                        os.write(response.encode().getBytes(StandardCharsets.ISO_8859_1));
                        os.flush();
                    }
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getLocalizedMessage());
                }
            }
        }
    }

}