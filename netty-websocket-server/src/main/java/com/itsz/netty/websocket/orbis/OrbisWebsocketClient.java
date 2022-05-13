package com.itsz.netty.websocket.orbis;

import io.netty.bootstrap.Bootstrap;
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
import net.iharder.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class OrbisWebsocketClient {

    public static final String SCHEMA = "ws";

    public static final String HOSTNAME = "kotrading.orbisfn.net/external";


    public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException {

        Credentials credentials = new PublicKeyCredentials("D:\\Jeremy\\spring\\hello-world\\netty-learning\\netty-websocket-server\\src\\main\\resources\\d9cc38f9-55d5-4ca6-80a2-37b64da603bb.pem");

        URI uri = new URI(SCHEMA + "://" + HOSTNAME + "/stream?auth=" + URLEncoder.encode(
                credentials.getScheme() + " " + Base64.encodeBytes(credentials.getToken().getBytes()), "ISO-8859-1"));


        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new HttpClientCodec());
                         ///   pipeline.addLast(new ChunkedWriteHandler());
                           // pipeline.addLast(new HttpObjectAggregator(8192));
                            pipeline.addLast(new WebSocketClientProtocolHandler(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders())));
                            pipeline.addLast(new OrbisWebSocketHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(HOSTNAME,443);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }

    }
}
