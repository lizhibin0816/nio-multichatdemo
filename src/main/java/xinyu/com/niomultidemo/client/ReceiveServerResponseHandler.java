package xinyu.com.niomultidemo.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class ReceiveServerResponseHandler implements Runnable {

    private Selector selector;
    public ReceiveServerResponseHandler() {}
    public ReceiveServerResponseHandler(Selector selector) {
        this.selector = selector;
    }
    @Override
    public void run() {
        try {
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
                    // 判断为可读事件处理
                    if (selectionKey.isReadable()) {
                        readHandler(selectionKey, selector);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
         * 3、循环读取服务器端的请求信息
         */
        StringBuilder response = new StringBuilder();
        //socketChannel.read(byteBuffer)--socketChannel读取字节到byteBuffer中的字节数
        while(socketChannel.read(byteBuffer)>0) {
            // 此时byteBuffer 为写入模式，使用flip方法切换byteBuffer位读取模式
            byteBuffer.flip();
            //读取byteBuffer中的内容
            response.append(Charset.forName("UTF-8").decode(byteBuffer));
        }
        /**
         * 4、将channel再次注册到selector上，监听可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);
        /**
         * 5、将服务端返回信息，广播给其他客户端
         */
        if (response.toString().length()>0) {
            System.out.println(response.toString());
        }
    }
}
