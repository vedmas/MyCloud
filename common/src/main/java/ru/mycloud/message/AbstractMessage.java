package ru.MyCloud.message;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractMessage implements Serializable {
    public static class FileListMassage extends AbstractMessage {
        private List<String> listFile;

        public List<String> getListFile() {
            return listFile;
        }

        public FileListMassage(List<String> listFile) {
            this.listFile = listFile;
        }
    }
}
