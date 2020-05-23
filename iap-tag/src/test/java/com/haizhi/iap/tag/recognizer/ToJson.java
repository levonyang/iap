package com.haizhi.iap.tag.recognizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haizhi.iap.tag.param.DataRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToJson {

    public static void main(String[] args) {

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

        /*TagCollection tagCollection = new TagCollection();
        tagCollection.setName("tag3");
        tagCollection.setEsName("esname");
        tagCollection.setIsDeleted(1);
        tagCollection.setCreateTime(new Date());
        tagCollection.setUpdateTime(new Date());
        tagCollection.setComment("test");
        String tagString = gson.toJson(tagCollection);
        System.out.println(tagString);*/

        /*List<TagDetailRequest> tagDetailRequestsList = new ArrayList<>();
        TagDetailRequest tagDetailRequest = new TagDetailRequest();
        tagDetailRequest.setParentId(1);
        tagDetailRequest.setFieldName("city");
        tagDetailRequest.setName("武汉");
        tagDetailRequest.setTagType(0);

        TagDetailRequest tagDetailRequest2 = new TagDetailRequest();
        tagDetailRequest2.setParentId(1);
        tagDetailRequest2.setFieldName("city");
        tagDetailRequest2.setName("北京");
        tagDetailRequest2.setTagType(0);

        TagDetailRequest tagDetailRequest3 = new TagDetailRequest();
        tagDetailRequest3.setParentId(2);
        tagDetailRequest3.setFieldName("val_registered_capital");
        tagDetailRequest3.setName("0_50000");
        tagDetailRequest3.setTagType(3);

        tagDetailRequestsList.add(tagDetailRequest);
        tagDetailRequestsList.add(tagDetailRequest2);
        tagDetailRequestsList.add(tagDetailRequest3);

        String tagString =  gson.toJson(tagDetailRequestsList);*/

        /**
         *  private Integer parentId;
            private Integer collectionId;
            private String name;
            private String fieldName;
            private Integer tagType;
            private Integer level;
            private String comment;
         */

        /*TagDetailAddRequest tagDetailAddRequest = new TagDetailAddRequest();
        tagDetailAddRequest.setParentId(0);
        tagDetailAddRequest.setCollectionId(4);
        tagDetailAddRequest.setName("国家");
        tagDetailAddRequest.setFieldName("country_s");
        tagDetailAddRequest.setTagType(0);
        tagDetailAddRequest.setLevel(0);
        tagDetailAddRequest.setComment("国家");

        String tagString =  gson.toJson(tagDetailAddRequest);

        System.out.println(tagString);*/

        /*FetchNewDataRequest fetchNewDataRequest = new FetchNewDataRequest();
        fetchNewDataRequest.setCollectionName("baidu_news");
        fetchNewDataRequest.setOffset(0);
        fetchNewDataRequest.setCount(10);
        String tagString =  gson.toJson(fetchNewDataRequest);
        System.out.println(tagString);*/

        /*QueryESByNameRequest queryESByNameRequest = new QueryESByNameRequest();
        queryESByNameRequest.setEsIndexName("enterprise_overview_test");
        queryESByNameRequest.setEsType("enterprise_overview_test");
        queryESByNameRequest.setFrom(0);
        queryESByNameRequest.setSize(10);
        String tagString =  gson.toJson(queryESByNameRequest);
        System.out.println(tagString);*/

        /*TagDetailSearchByNameRequest tagDetailSearchByNameRequest = new TagDetailSearchByNameRequest();

        List<TagDetailRequest> tagDetailRequestsList = new ArrayList<>();
        TagDetailRequest tagDetailRequest = new TagDetailRequest();
        tagDetailRequest.setParentId(1);
        tagDetailRequest.setFieldName("product_small_s");
        tagDetailRequest.setName("证券");
        tagDetailRequest.setTagType(0);

        TagDetailRequest tagDetailRequest2 = new TagDetailRequest();
        tagDetailRequest2.setParentId(1);
        tagDetailRequest2.setFieldName("product_small_s");
        tagDetailRequest2.setName("机械");
        tagDetailRequest2.setTagType(0);

        TagDetailRequest tagDetailRequest3 = new TagDetailRequest();
        tagDetailRequest3.setParentId(2);
        tagDetailRequest3.setFieldName("district_province_s");
        tagDetailRequest3.setName("江苏省");
        tagDetailRequest3.setTagType(0);

        tagDetailRequestsList.add(tagDetailRequest);
        tagDetailRequestsList.add(tagDetailRequest2);
        tagDetailRequestsList.add(tagDetailRequest3);

        tagDetailSearchByNameRequest.setEsIndexName("enterprise_overview_test");
        tagDetailSearchByNameRequest.setEsType("enterprise_overview_test");
        tagDetailSearchByNameRequest.setTagDetailRequestsList(tagDetailRequestsList);

        String tagString =  gson.toJson(tagDetailSearchByNameRequest);

        System.out.println(tagString);*/

        /*List<Map> listdata = new ArrayList<>();
        Map<String,String> mapdata = new HashMap<>();
        mapdata.put("id","0540779C25E7596349DA1F6856450507");
        mapdata.put("name_s","鸿翔一堂");
        mapdata.put("province_s","云南");
        mapdata.put("city_s","文山");
        listdata.add(mapdata);

        Map<String,String> mapdata0 = new HashMap<>();
        mapdata0.put("id","0540DDC62A9D9FACAD1D9342D31008F4");
        mapdata0.put("name_s","神马集团");
        mapdata0.put("province_s","湖北");
        mapdata0.put("city_s","宜昌");
        listdata.add(mapdata0);

        Map<String,String> mapdata1 = new HashMap<>();
        mapdata1.put("id","05429AC33864C4783966022F744B3194");
        mapdata1.put("name_s","热能设备");
        mapdata1.put("province_s","广东");
        mapdata1.put("city_s","东莞");
        listdata.add(mapdata1);

        Map<String,String> mapdata2 = new HashMap<>();
        mapdata2.put("id","0545C16F10BE68B5C478DA3EAEC0EEE5");
        mapdata2.put("name_s","池州市贵");
        mapdata2.put("province_s","安徽");
        mapdata2.put("city_s","池州");
        listdata.add(mapdata2);

        Map<String,String> mapdata3 = new HashMap<>();
        mapdata3.put("id","0546CF7274C203DBCFB3FC62D922C067");
        mapdata3.put("name_s","机械设备");
        mapdata3.put("province_s","广东");
        mapdata3.put("city_s","中山");
        listdata.add(mapdata3);

        Map<String,String> mapdata4 = new HashMap<>();
        mapdata4.put("id","0545E88F7731880B6620BD9A3FEB1C93");
        mapdata4.put("name_s","崇善堂中");
        mapdata4.put("province_s","湖南");
        mapdata4.put("city_s","邵阳");
        listdata.add(mapdata4);

        MapDataRequest mapDataRequest = new MapDataRequest();
        mapDataRequest.setDatalist(listdata);
        mapDataRequest.setEsIndexName("enterprise_overview_test");
        mapDataRequest.setEsType("enterprise_overview_test");

        String tagString =  gson.toJson(mapDataRequest);

        System.out.println(tagString);*/

        /*TagDetailAddRequest tagDetailAddRequest = new TagDetailAddRequest();
        tagDetailAddRequest.setParentId(0);
        tagDetailAddRequest.setCollectionId(2);
        tagDetailAddRequest.setName("产品");
        tagDetailAddRequest.setFieldName("product_s");
        tagDetailAddRequest.setTagType(0);
        tagDetailAddRequest.setLevel(0);
        tagDetailAddRequest.setComment("产品分类");

        String tagString =  gson.toJson(tagDetailAddRequest);

        System.out.println(tagString);*/

/*        List<Map> listdata = new ArrayList<>();
        Map<String,String> mapdata = new HashMap<>();
        mapdata.put("_id","0540779C25E7596349DA1F6856450507");
        mapdata.put("product_one_level","文化教育");
        listdata.add(mapdata);

        MapDataRequest mapDataRequest = new MapDataRequest();
        mapDataRequest.setDatalist(listdata);
        mapDataRequest.setEsIndexName("enterprise_overview_test");
        mapDataRequest.setEsType("enterprise_overview_test");
        mapDataRequest.setAction("update");

        String tagString =  gson.toJson(mapDataRequest);

        System.out.println(tagString);*/

        DataRequest dataRequest = new DataRequest();
        List<Map> listdata = new ArrayList<>();

        Map<String,String> mapdata = new HashMap<>();
        mapdata.put("_id","AV--tMFSGvXjpKol_vvQ");
        mapdata.put("product_one_level","文化教育");
        listdata.add(mapdata);

        dataRequest.setDatalist(listdata);
        dataRequest.setEsIndexName("enterprise_overview_test");
        dataRequest.setEsType("enterprise_overview_test");

        String tagString =  gson.toJson(dataRequest);

        System.out.println(tagString);

    }
}
