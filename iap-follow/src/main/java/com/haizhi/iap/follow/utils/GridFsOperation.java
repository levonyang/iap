package com.haizhi.iap.follow.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/12 12:22
 */
public class GridFsOperation {

    private String gridfsDBName;

    private MongoClient mongo;

    private GridFS gridFS;

    public GridFsOperation(String gridfsDBName, MongoClient mongo) {
        this.gridfsDBName = gridfsDBName;
        this.mongo = mongo;
        this.gridFS = gridFS();
    }

    public String saveFile(byte[] bytes, String subDir, String fileName) {
        //默认覆盖上传
        return saveFile(bytes, subDir, fileName, true);
    }

    public String saveFile(byte[] bytes, String subDir, String fileName, boolean override) {
        String finalName = subDir + "/" + fileName;
        DBObject query = new BasicDBObject("filename", finalName);
        GridFSDBFile gridFSDBFile = this.gridFS.findOne(query);
        if (gridFSDBFile == null) {
            upload(bytes, finalName);
        } else if (override) {
            //覆盖的话,先删,然后上传
            this.gridFS.remove(gridFSDBFile);
            upload(bytes, finalName);
        }
        return finalName;
    }

    public GridFSDBFile getFile(String file) {
        DBObject query = new BasicDBObject("filename", file);
        GridFSDBFile gridFSDBFile = this.gridFS.findOne(query);
        return gridFSDBFile;
    }

    public void upload(byte[] bytes, String fileName) {
        GridFSInputFile gridFSInputFile = gridFS.createFile(bytes);
        gridFSInputFile.setFilename(fileName);
        //gridFSInputFile.setChunkSize();
        //gridFSInputFile.setContentType();
        //gridFSInputFile.setMetaData();
        gridFSInputFile.save();
    }

    public void delete(String file) {
        DBObject query = new BasicDBObject("filename", file);
        GridFSDBFile gridFSDBFile = this.gridFS.findOne(query);
        if (gridFSDBFile != null) {
            gridFS().remove(gridFSDBFile);
        }
    }

    public GridFS gridFS() {
        if (gridFS == null) {
            gridFS = new GridFS(mongo.getDB(gridfsDBName));
        }
        return gridFS;
    }
}
