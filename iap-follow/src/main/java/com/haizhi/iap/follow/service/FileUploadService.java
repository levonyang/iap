package com.haizhi.iap.follow.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by chenbo on 17/1/18.
 */
@Slf4j
@Service
public class FileUploadService {

    @Setter
    @Value("${gridfs.mongodb.database}")
    String gridfsDBName;

    @Setter
    @Autowired
    @Qualifier(value = "gridfsMongo")
    MongoClient mongo;

    private GridFS gridFS;

    public String saveFile(byte[] bytes, String subDir, String fileName) {
        //默认覆盖上传
        return saveFile(bytes, subDir, fileName, true);
    }

    public String saveFile(byte[] bytes, String subDir, String fileName, boolean override) {
        String finalName = subDir + "/" + fileName;
        DBObject query = new BasicDBObject("filename", finalName);
        GridFSDBFile gridFSDBFile = gridFS().findOne(query);
        if (gridFSDBFile == null) {
            upload(bytes, finalName);
        } else if (override) {
            //覆盖的话,先删,然后上传
            gridFS().remove(gridFSDBFile);
            upload(bytes, finalName);
        }
        return finalName;
    }

    public GridFSDBFile getFile(String file) {
        DBObject query = new BasicDBObject("filename", file);
        GridFSDBFile gridFSDBFile = gridFS().findOne(query);
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
        GridFSDBFile gridFSDBFile = gridFS().findOne(query);
        if (gridFSDBFile != null) {
            gridFS().remove(gridFSDBFile);
        }
    }


//    public String saveFile(byte[] bytes, String subDir, String fileName) {
//        GridFSBucket bucket = GridFSBuckets.create(mongoDatabase);
//        ObjectId fileId = bucket.uploadFromStream(subDir + "/" + fileName, new ByteArrayInputStream(bytes));
//        return subDir + "/" + fileName;
//    }
//
//    public void delete(String file) {
//        GridFSBucket bucket = GridFSBuckets.create(mongoDatabase);
//        GridFSFindIterable files = bucket.find(new BasicDBObject("filename", file));
//        bucket.delete(files.first().getObjectId());
//    }

    public GridFS gridFS() {
        if (gridFS == null) {
            gridFS = new GridFS(mongo.getDB(gridfsDBName));
        }
        return gridFS;
    }

}
