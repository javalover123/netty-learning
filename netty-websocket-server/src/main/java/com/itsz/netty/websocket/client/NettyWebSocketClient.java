package com.itsz.netty.websocket.client;

import com.itsz.netty.websocket.orbis.Credentials;
import com.itsz.netty.websocket.orbis.PublicKeyCredentials;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.iharder.Base64;
import net.minidev.json.JSONObject;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class NettyWebSocketClient {

    static final String URL = System.getProperty("url", "ws://127.0.0.1:8889/hello");
    public static final String WS = "ws";
    public static final String WSS = "wss";
    public static final String HOSTNAME = "kotrading.orbisfn.net/external";

    public static void main(String[] args) throws URISyntaxException, SSLException, InterruptedException, UnsupportedEncodingException {
        Credentials credentials = new PublicKeyCredentials("D:\\Jeremy\\spring\\hello-world\\netty-learning\\netty-websocket-server\\src\\main\\resources\\d9cc38f9-55d5-4ca6-80a2-37b64da603bb.pem");

      /*  URI uri = new URI(WSS + "://" + HOSTNAME + "/stream?auth=" + URLEncoder.encode(
                credentials.getScheme() + " " + Base64.encodeBytes(credentials.getToken().getBytes()), "ISO-8859-1"));*/

        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if (WS.equalsIgnoreCase(scheme)) {
                port = 80;
            } else if (WSS.equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!WS.equalsIgnoreCase(scheme) && !WSS.equalsIgnoreCase(scheme)) {
            System.err.println("Only WS(S) is supported.");
            return;
        }

        final boolean ssl = WSS.equalsIgnoreCase(scheme);
        final SslContext sslContext;
        if (ssl) {
            sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslContext = null;
        }

        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            if (sslContext != null) {
                                pipeline.addLast(sslContext.newHandler(socketChannel.alloc(), host, port));
                            }

                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            pipeline.addLast(new WebSocketClientProtocolHandler(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders())));
                            pipeline.addLast(new WebSocketClientHandler());
                        }
                    });

            Channel ch = bootstrap.connect(host, port).sync().channel();
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msg = console.readLine();
                if (msg == null) {
                    break;
                } else if ("bye".equals(msg.toLowerCase())) {
                    ch.writeAndFlush(new CloseWebSocketFrame());
                    ch.closeFuture().sync();
                    break;
                } else if ("ping".equals(msg.toLowerCase())) {
                    WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1}));
                    ch.writeAndFlush(frame);
                } else {
                    JSONObject sub = new JSONObject();
                    sub.put("action", "sub");
                    sub.put("symbols", "GOOGL");
                    WebSocketFrame frame = new TextWebSocketFrame(sub.toString());
                    ch.writeAndFlush(frame);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
