package org.wowtools.dao;

import org.h2.tools.Server;

import java.sql.SQLException;

/**
 * @author liuyu
 * @date 2018/1/23
 */
public class TcpServerCell {
    public static final Server tcpServer;

    static {
        try {
            tcpServer = Server
                    .createTcpServer(new String[]{"-tcpPort", "6999", "-tcpAllowOthers"});
            tcpServer.start();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init(){

    }
}
