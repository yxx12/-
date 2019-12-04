package com.changggou;

import com.changgou.util.FileDFSClient;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTest {

    @Test
    public void download() throws IOException {
        String group_name = "group1";
        String remote_filename = "M00/00/00/wKjThF2psEuAASQOAAajH0UUMK0139.jpg";
        byte[] download = FileDFSClient.download(group_name, remote_filename);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("E:\\ 1.jpg"));
        IOUtils.write(download, fileOutputStream);
    }

    @Test
    public void delete() {
        String group_name = "group1";
        String remote_filename = "M00/00/00/wKjThF2oJ-qAEZ8zAA9NKtrbsQM069.jpg";
        FileDFSClient.deleteFile(group_name, remote_filename);
    }
}