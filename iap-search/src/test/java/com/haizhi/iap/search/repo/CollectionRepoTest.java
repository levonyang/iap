package com.haizhi.iap.search.repo;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.search.conf.AppDataCollections;
import com.haizhi.iap.search.model.NewRegisteredCompany;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuding on 2018/7/10.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})
@WebAppConfiguration
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class CollectionRepoTest {

//    @Resource
//    private CollectionRepo collectionRepo;

    @Autowired
    private MongoDatabase mongoDatabase;

    @Autowired
    private NewRegisteredCompanyRepo newRegisteredCompanyRepo;

    @Autowired
    private DetailGraphRepo detailGraphRepo;

    @Test
    public void getCollectionByNameAndConditionTest() {
        System.out.println("a");
//        Param param = new Param();
//        String ret = collectionRepo.getCollectionByNameAndCondition("name", "keyname",
//                2, 3, "haizhi", Param param);
//        Assert.assertEquals(" AND d.key1 == 'value1'", ret);
    }

    @Test
    public void testFindAll() throws Exception {
        MongoCollection<Document> collection = mongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);
        Bson bson = new BasicDBObject();
        ((BasicDBObject) bson).append("company", 1).append("city", 1).append("123", 1);
        FindIterable<Document> documents = collection.find().projection(bson);
        MongoCursor<Document> iterator = documents.iterator();
        List<Document> documentList = Lists.newArrayList();
        while (iterator.hasNext()) {
            Document document = iterator.next();
            documentList.add(document);
        }
        documentList.forEach(System.out::println);
    }

    @Test
    public void testAggregate() throws Exception {
        MongoCollection<Document> collection = mongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);
        /*Document group = new Document().append("$group", new Document("_id", "$industry").append("industry", new Document("$first", "$industry"))
                .append("count", new Document("$sum", 1)));
        Document project = new Document().append("_id", 0).append("industry", 1)
                .append("count", 1);
        Bson sort = new Document("$sort", new Document("count", -1));
        Bson limit = new Document("$limit", 5);*/
        Document project = new Document().append("_id", 0).append("industry", 1)
                .append("count", 1);
        MongoCursor<Document> iterator = collection.aggregate(Lists.newArrayList(
//                Aggregates.group("$industry");
                Aggregates.match(Filters.in("industry", "批发业", "作业")),
                Aggregates.group(new Document("_id", "$industry"), Accumulators.sum("count", 1), Accumulators.first("industry", "$industry")),
                Aggregates.sort(Sorts.descending("count")),
                Aggregates.limit(5),
                Aggregates.project(project)
        )).iterator();
//        ArrayList<Bson> aggregate = Lists.newArrayList(group, sort, limit);
//        AggregateIterable<Document> aggregate1 = collection.aggregate(aggregate);
//        MongoCursor<Document> iterator = aggregate1.iterator();
        List<Document> result = Lists.newArrayList();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        System.out.println(JSON.toJSONString(Wrapper.ok(result)));
    }

    @Test
    public void testN() throws Exception {
        Set<String> groupEnterpriseList = Sets.newHashSet("深圳市小红月科技有限公司", "深圳市速迈通物流有限公司", "深圳市佳韬景实业有限公司"
                , "广州齐鸣信息技术服务有限公司", "惠州市聚鑫博科技有限公司");
        List<NewRegisteredCompany> byCompanyInGroupEnterprise = newRegisteredCompanyRepo.findByCompanyInGroupEnterprise(groupEnterpriseList, 0);
//        long l = newRegisteredCompanyRepo.countByCompanyInGroupEnterprise(groupEnterpriseList, 0);
//        System.out.println(l);
        byCompanyInGroupEnterprise.forEach(System.out::println);
    }

    /**
    * @description 测试从Arango获取指定公司的高管人员信息
    * @param
    * @return void
    * @author liulu
    * @date 2018/12/25
    */
    @Test
    public void testCompanyKeyPerson(){
        String company = "深圳市地铁集团有限公司";
        List<Map<String, Object>> result = detailGraphRepo.getGraphKeyPerson(company);
        System.out.println(JSON.toJSONString(result,true));
    }

}
