package com.itsz.netty.websocket.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.charset.Charset;
import java.time.LocalDateTime;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame webSocketFrame) throws Exception {
        System.out.println("receive message from client:"+ webSocketFrame.text());
        ctx.channel().writeAndFlush( new TextWebSocketFrame("服务器时间:"+ LocalDateTime.now()+" - "+ webSocketFrame.text()+"\n"));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接开启:"+ ctx.channel().remoteAddress()+"Id:"+ctx.channel().id().asLongText());
        ctx.channel().writeAndFlush( new TextWebSocketFrame("服务器时间:"+ LocalDateTime.now()+" - "+ "Hello,From Server \n"));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接关闭"+ ctx.channel().remoteAddress()+"Id:"+ctx.channel().id().asLongText());
    }
}
