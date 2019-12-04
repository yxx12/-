package com.changgou.util;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;


public class FileDFSClient {
    //初始化 fileDFS系统
    static {
        try {
            //获取配置文件的位置
            String path = new ClassPathResource("fdfs_client.conf").getPath();
            //加载配置文件
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传文件
     *
     * @param file_buff
     * @param file_ext_name
     * @param username
     * @return
     */
    public static String[] upload(byte[] file_buff, String file_ext_name, String username) {
        String[] uploadResults = null;
        try {
            NameValuePair[] meta_list = new NameValuePair[1];   // 附件备注
            meta_list[0] = new NameValuePair(username);

            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);

            uploadResults = storageClient.upload_file(file_buff, file_ext_name, meta_list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadResults;
    }

    /**
     * 文件下载
     *
     * @param group_name
     * @param remote_filename
     * @return
     */
    public static byte[] download(String group_name, String remote_filename) {
        try {
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);

            byte[] downloadFile = storageClient.download_file(group_name, remote_filename);
            return downloadFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取服务器地址
     *
     * @return
     */
    public static String getUrl() {
        try {
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();

            String ip = trackerServer.getInetSocketAddress().getAddress().getHostAddress();

            int port = ClientGlobal.getG_tracker_http_port();

            String url = "http://" + ip + ":" + port;
            return url;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除文件
     * @param group_name
     * @param remote_filename
     */
    public static void deleteFile(String group_name, String remote_filename) {

        try {
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);
            storageClient.delete_file(group_name, remote_filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
