package uk.ac.ebi.pride.ws.pride.models.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//{"statusCode":404,"statusMessage":"Not Found","httpMethod":"GET","detail":"No archived object found which has a Fire path of `/2021/12/PXD029252/internal/submission.px`"}⏎
//{"objectId":137498210,"fireOid":"03ef15c454734318a4da65e3e696242d","objectMd5":"2c3283715af9c0e0fcaafe5f99470629","objectSize":25945,"createTime":"2022-04-27 11:14:11","metadata":[],"filesystemEntry":{"path":"/2021/03/PXD023037/internal/submission.px","published":false}}⏎

@JsonIgnoreProperties(ignoreUnknown = true)
public class FireObject {
    private String objectId;
    private String fireOid;
    private String objectMd5;
    private String objectSize;
    private FireFileSystemEntry filesystemEntry;

    public FireObject() {
    }

    public FireObject(String objectId, String fireOid, String objectMd5, String objectSize, FireFileSystemEntry filesystemEntry) {
        this.objectId = objectId;
        this.fireOid = fireOid;
        this.objectMd5 = objectMd5;
        this.objectSize = objectSize;
        this.filesystemEntry = filesystemEntry;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getFireOid() {
        return fireOid;
    }

    public void setFireOid(String fireOid) {
        this.fireOid = fireOid;
    }

    public String getObjectMd5() {
        return objectMd5;
    }

    public void setObjectMd5(String objectMd5) {
        this.objectMd5 = objectMd5;
    }

    public String getObjectSize() {
        return objectSize;
    }

    public void setObjectSize(String objectSize) {
        this.objectSize = objectSize;
    }

    public FireFileSystemEntry getFilesystemEntry() {
        return filesystemEntry;
    }

    public void setFilesystemEntry(FireFileSystemEntry filesystemEntry) {
        this.filesystemEntry = filesystemEntry;
    }

    @Override
    public String toString() {
        return "FireObject{" +
                "objectId='" + objectId + '\'' +
                ", fireOid='" + fireOid + '\'' +
                ", objectMd5='" + objectMd5 + '\'' +
                ", objectSize='" + objectSize + '\'' +
                ", filesystemEntry=" + filesystemEntry +
                '}';
    }

    public class FireFileSystemEntry {
        private String path;
        private boolean published;

        public FireFileSystemEntry() {
        }

        public FireFileSystemEntry(String path, boolean published) {
            this.path = path;
            this.published = published;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isPublished() {
            return published;
        }

        public void setPublished(boolean published) {
            this.published = published;
        }

        @Override
        public String toString() {
            return "FireFileSystemEntry{" + "path='" + path + '\'' + ", published=" + published + '}';
        }
    }
}

