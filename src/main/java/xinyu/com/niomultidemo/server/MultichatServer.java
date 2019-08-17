package xinyu.com.niomultidemo.server;

import xinyu.com.niomultidemo.common.Constant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class MultichatServer {

    public void start () throws IOException {
        /**
         * 1、创建selector
         */
        Selector selector = Selector.open();
        /**
         * 2、创建serversocketchannel，并绑定端口
         */
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8000));
        /**
         * 3、将serversocketchannel，设置为非阻塞模式
         */
        serverSocketChannel.configureBlocking(false);
        /**
         * 4、将serversocketchannel，注册到selector上监听连接事件
         */
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功");
        /**
         * 5、循环调用selector的select方法，检测就绪的事件
         */
        while(true) {
            //获取可用的channel数量
            int readyChannels = selector.select();
            if (readyChannels==0) continue;
            /**
             * 6、调用selectkeys方法，获取就绪的channel集合
             */
            // 获取可用的channel集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator iterator = selectionKeys.iterator();
            while(iterator.hasNext()) {
                SelectionKey selectionKey = (SelectionKey)iterator.next();
                //移除当前的selectionKey
                iterator.remove();
                /**
                 * 7、判断就绪事件种类，调用业务的处理方法
                 */
                // 判断接入事件处理
                if (selectionKey.isAcceptable()) {
                    acceptHandler(serverSocketChannel, selector);
                }
                // 判断为可读事件处理
                if (selectionKey.isReadable()) {
                    readHandler(selectionKey, selector);
                }
            }
        }
    }

    /**
     * 接入事件处理器
     */
    private void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        /**
         * 1、创建socketchannel，与服务器端建立连接
         */
        SocketChannel socketChannel = serverSocketChannel.accept();
        /**
         * 2、设置socketchannel为非阻塞模式
         */
        socketChannel.configureBlocking(false);
        /**
         * 3、将socketchannel注册到selector中，监听可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);
        /**
         * 4、回复客户端提示信息
         */
        socketChannel.write(Charset.forName(Constant.charset).encode("success!!"));
    }

    /**
     * 可读事件处理器
     */
    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        /**
         * 1、从selectionKey中获取已经就绪的channel
         */
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        /**
         * 2、创建buffer
         */
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        /**
         * 3、循环读取客户端的请求信息
         */
        StringBuilder request = new StringBuilder();
        //socketChannel.read(byteBuffer)--socketChannel读取字节到byteBuffer中的字节数
        while(socketChannel.read(byteBuffer)>0) {
            // 此时byteBuffer 为写入模式，使用flip方法切换byteBuffer位读取模式
            byteBuffer.flip();
            //读取byteBuffer中的内容
            request.append(Charset.forName(Constant.charset).decode(byteBuffer));
        }
        /**
         * 4、将channel再次注册到selector上，监听可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);
        /**
         * 5、将客户端发送的请求信息，广播给其他客户端
         */
        if (request.toString().length() > 0) {
            System.out.println(request.toString());
            broadcast(selector, socketChannel, request.toString());
        }
    }

    /**
     * 将客户端发送的请求信息，广播给其他客户端
     */
    private void broadcast(Selector selector, SocketChannel sourceChannel, String request) {
        /**
         * 获取所有已接入的客户端channel
         */
        Set<SelectionKey> selectionKeys = selector.keys();

        selectionKeys.forEach(selectionKey -> {
            Channel targetChannel = selectionKey.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != sourceChannel) {
                try {
                    ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void main(String[] args) throws IOException {
        new MultichatServer().start();
    }
}
