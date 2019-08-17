package xinyu.com.niomultidemo.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class MultichatClient {

    public void start () throws IOException {
        /**
         * 1、连接服务器端
         */
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));

        /**
         * 3、接受服务器端响应
         * 启动一个线程 接受服务端返回响应
         */
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new ReceiveServerResponseHandler(selector)).start();

        /**
         * 2、向服务器端发送数据
         */
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextLine()) {
            String request = scanner.nextLine();
            if (request != null && request.length()>0) {
                socketChannel.write(Charset.forName("UTF-8").encode(request));
            }
        }
    }
}
