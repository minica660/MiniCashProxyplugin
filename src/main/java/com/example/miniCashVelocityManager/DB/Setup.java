package miniCashVelocityManager.DB;

import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public class Setup {

    private Connection cont = null;
    private final Logger logger;
    private final ConfigurationNode config;

    public Setup(Logger logger, ConfigurationNode config) {
        this.logger = logger;
        this.config = config;

    }

    public void connect() {

        try {
            if (cont != null && !cont.isClosed() && cont.isValid(3)) {
                return;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        final String URL = "jdbc:mysql://" + config.node("mysql","host").getString() + "/" + config.node("mysql","database").getString() + "?useSSL=false&autoReconnect=true&serverTimezone=Asia/Tokyo";
        final String USER = config.node("mysql","user").getString("root");
        final String PASS = config.node("mysql","password").getString("password");

        try {
            //db接続
            cont = getConnection(URL, USER, PASS);
            logger.info("データベースへの接続が完了しました");
//            //ステートメント生成
//            stmt = connect.createStatement();

        } catch (SQLException e) {

            logger.error("データベースへの接続に失敗しました: " + e.getMessage());
            //("プラグインを停止します");

        }


    }

    public void setupTable() {
        if (cont == null) {
            return;
        }

        //プレイヤーデータ保存テーブル
        String sql = "CREATE TABLE IF NOT EXISTS player_data ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "player_name VARCHAR(16) NOT NULL,"
                + "uuid VARCHAR(36) NOT NULL UNIQUE,"
                + "status VARCHAR(16) NOT NULL,"
                + "ip-address INT NOT NULL"
                + ");";
        try(PreparedStatement pstmt = cont.prepareStatement(sql)){

            pstmt.executeUpdate();

        }catch (SQLException e){
            logger.error("DB プレイヤーデータテーブル生成処理でエラーが発生しました");
            logger.error(e.getMessage());
        }



        String sql2 = "CREATE TABLE IF NOT EXISTS connection_log ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "player_name VARCHAR(16) NOT NULL,"
                + "uuid VARCHAR(36) NOT NULL,"
                + "ip-address VARCHAR(32) NOT NULL,"
                + "content2 VARCHAR(20)"
                + ");";
        try(PreparedStatement pstmt = cont.prepareStatement(sql2)){

            pstmt.executeUpdate();

        }catch (SQLException e){
            logger.error("DB接続ログ用テーブル生成処理でエラーが発生しました");
            logger.error(e.getMessage());
        }


        String sql3 = "CREATE TABLE IF NOT EXISTS command_log ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "player_name VARCHAR(16) NOT NULL,"
                + "uuid VARCHAR(36) NOT NULL,"
                + "command VARCHAR(20) NOT NULL,"
                + "check BOOLEAN NOT NULL,"
                + "server VARCHAR(15) NOT NULL"
                + ");";
        try(PreparedStatement pstmt = cont.prepareStatement(sql3)){

            pstmt.executeUpdate();

        }catch (SQLException e){
            logger.error("DBコマンドログ用テーブル生成処理でエラーが発生しました");
            logger.error(e.getMessage());
        }


        String sql4 = "CREATE TABLE IF NOT EXISTS banlist ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "player_name VARCHAR(16) NOT NULL,"
                + "uuid VARCHAR(36) NOT NULL,"
                + "ip-address VARCHAR(32) NOT NULL,"
                + "reason VARCHAR(25)"
                + ");";
        try(PreparedStatement pstmt = cont.prepareStatement(sql4)){

            pstmt.executeUpdate();

        }catch (SQLException e){
            logger.error("DBbanプレイヤーリスト用テーブル生成処理でエラーが発生しました");
            logger.error(e.getMessage());
        }


        String sql5 = "CREATE TABLE IF NOT EXISTS messagelog ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "player_name VARCHAR(16) NOT NULL,"
                + "uuid VARCHAR(36) NOT NULL,"
                + "ip-address VARCHAR(32) NOT NULL,"
                + "server VARCHAR(20),"
                + "content STRING NOT NULL"
                + ");";
        try(PreparedStatement pstmt = cont.prepareStatement(sql5)){

            pstmt.executeUpdate();

        }catch (SQLException e){
            logger.error("DBメッセージログ用テーブル生成処理でエラーが発生しました");
            logger.error(e.getMessage());
        }


    }




    public void disconnect() {
        try {
            if (cont != null && !cont.isClosed()) {
                cont.close();
                logger.info("データベースの接続を切断しました。");
            }
        }catch (SQLException e) {
            logger.error( "データベースの接続を閉じる際にエラーが発生しました", e.getMessage());
        }
    }
}
