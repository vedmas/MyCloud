package ru.MyCloud.common;

import java.util.List;

public class FileListMassage extends AbstractMessage {
    private List<String> listFile;

    public List<String> getListFile() {
        return listFile;
    }

    public FileListMassage(List<String> listFile) {
        this.listFile = listFile;
    }
}
