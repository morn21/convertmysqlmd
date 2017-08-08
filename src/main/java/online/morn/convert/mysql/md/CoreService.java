package online.morn.convert.mysql.md;

import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CoreService {

    public CoreService(){
        try {
            Connection conn = DBManager.getConn();
            String str = getTables(conn);//获得表格数据
            DBManager.closeConn(conn);

            System.out.println(str);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转换数据库字符集
     * @param in
     * @param type
     * @return
     */
    public String convertDatabaseCharsetType(String in, String type) {
        String dbUser;
        if (in != null) {
            if (type.equals("oracle")) {
                dbUser = in.toUpperCase();
            } else if (type.equals("postgresql")) {
                dbUser = "public";
            } else if (type.equals("mysql")) {
                dbUser = null;
            } else if (type.equals("mssqlserver")) {
                dbUser = null;
            } else if (type.equals("db2")) {
                dbUser = in.toUpperCase();
            } else {
                dbUser = in;
            }
        } else {
            dbUser = "public";
        }
        return dbUser;
    }

    /**
     * 获得全部表格数据
     * @param conn
     * @return
     * @throws SQLException
     */
    private String getTables(Connection conn) throws SQLException {
        DatabaseMetaData dbMetData = conn.getMetaData();
        ResultSet rs = dbMetData.getTables(null, convertDatabaseCharsetType("root", "mysql"), null, new String[] { "TABLE", "VIEW" });

        StringBuffer returnBuffer = new StringBuffer();
        returnBuffer.append("----------------------------------------\n\n\n\n");
        while (rs.next()) {
            String str1 = rs.getString(1);
            String str2 = rs.getString(2);
            String str3 = rs.getString(3);
            String str4 = rs.getString(4);
            String str5 = rs.getString(5);
            if (str4 != null && (str4.equalsIgnoreCase("TABLE") || str4.equalsIgnoreCase("VIEW"))) {
                String tableName = rs.getString(3).toLowerCase();
                String tableRemarks = rs.getString("REMARKS");
                //System.out.println("tableName:" + tableName);
                //System.out.println("tableRemarks:" + tableRemarks);
                returnBuffer.append(tableName);
                if(StringUtils.isNotBlank(tableRemarks)){
                    returnBuffer.append("（").append(tableRemarks).append("）");
                }
                returnBuffer.append("\n\n");
                returnBuffer.append("| 字段名 | 类型 | 长度 | 含义 | 主外键 | 默认值 | 允许NULL | 备注 |\n");//表头
                returnBuffer.append("| ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |\n");

                // 根据表名提前表里面信息：
                ResultSet colRet = dbMetData.getColumns(null, "%", tableName, "%");
                while (colRet.next()) {
                    String columnName = colRet.getString("COLUMN_NAME");
                    String columnType = colRet.getString("TYPE_NAME");
                    int datasize = colRet.getInt("COLUMN_SIZE");
                    String remarks = colRet.getString("REMARKS");
                    int nullable = colRet.getInt("NULLABLE");
                    int digits = colRet.getInt("DECIMAL_DIGITS");

                    returnBuffer.append("|");
                    returnBuffer.append(columnName);//字段名
                    returnBuffer.append("|");
                    returnBuffer.append(columnType);//类型
                    returnBuffer.append("|");
                    returnBuffer.append(datasize);//长度
                    returnBuffer.append("|");
                    returnBuffer.append(remarks);//含义
                    returnBuffer.append("|");
                    if(columnName.equals("id")){
                        returnBuffer.append("主键");//主外键
                    }
                    returnBuffer.append("|");
                    if(columnName.equals("id")){
                        returnBuffer.append("AUTO_INCREMENT");//默认值
                    }
                    returnBuffer.append("|");
                    if(nullable == 0){
                        returnBuffer.append("不允许");//允许NULL
                    } else {
                        returnBuffer.append("允许");//允许NULL
                    }
                    returnBuffer.append("|");
                    returnBuffer.append("|\n");
                    //System.out.println(columnName + " " + columnType + " "+ datasize + " " + digits + " " + nullable + " " + remarks);
                }
                returnBuffer.append("\n\n\n");
            }
        }

        return returnBuffer.toString();
        // resultSet数据下标从1开始 ResultSet tableRet =
        //conn.getMetaData().getTables(null, null, "%", new String[] { "TABLE" });
        //while (tableRet.next()) {
        //  System.out.print(tableRet.getString(3) + "\t");
        //}
        //System.out.println();

    }
}
