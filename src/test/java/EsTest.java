import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gx.pojo.Goods;
import com.gx.pojo.Item;
import com.gx.repository.GoodsReposity;
import com.gx.repository.ItemRepository;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-mvc.xml")
public class EsTest {
    @Autowired
    RestHighLevelClient client;
//    @Autowired
//    ElasticsearchTemplate template;
//    @Autowired
//    GoodsReposity goodsReposity;
//    @Autowired
//    ItemRepository itemRepository;

        @Test
        public void  add() throws IOException {
            IndexRequest indexRequest = new IndexRequest("goods_ku", "goods","1");
            Goods goods = new Goods();
            goods.setId(1L);
            goods.setBarnd("小米");
            goods.setCategory("手机");
            goods.setImage("www.baidu.com");
            goods.setName("小米max");
            goods.setTitle(" 小米手机不仅在国内畅销,在国外也受到欢迎,但美国除外。");
            String source = JSON.toJSONString(goods);
            indexRequest.source(source, XContentType.JSON);
            client.index(indexRequest);
        }

        @Test
        public void testQueyTemplate() throws IOException {
            String preTag = "<font color='#dd4b39'>";//google的色值
            String postTag = "</font>";

            //1 构建匹配条件
            QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("小米","brand","title");
            //2组合匹配条件
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(queryBuilder);
            //3创建查询
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.fetchSource(new String[]{"id","price","title","brand"},null);
            sourceBuilder.query(boolQueryBuilder);
            sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
            sourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.DESC));
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title").field("brand").preTags(preTag).postTags(postTag);
            sourceBuilder.from(0).size(10).highlighter(highlightBuilder);

            //4创建搜索Request
            SearchRequest request = new SearchRequest("goods_ku");
            request.types("goods");
            request.source(sourceBuilder);
            //5解析反馈结果
            SearchResponse response = client.search(request);
            SearchHits hits = response.getHits();
            List<Item> list = new ArrayList<Item>();
            for (SearchHit hit:hits) {
                Map tempSource = hit.getSourceAsMap();

                System.out.println(tempSource);

                Item item = JSON.parseObject(JSON.toJSONString(tempSource), Item.class);

                //获取对应的高亮域
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();

                //高亮字段
                HighlightField title = highlightFields.get("title");
                if (title != null) {
                    item.setTitle(title.fragments()[0].toString());
                }
                HighlightField brand = highlightFields.get("brand");
                if (brand != null) {
                    item.setBrand(brand.fragments()[0].toString());
                }
                list.add(item);
            }
            for (Item item : list) {
                System.out.println("item = " + item);
            }
    }

    @Test
    public void testIndex() {
        Goods goods = new Goods();
        goods.setId(1L);
        goods.setBarnd("小米");
        goods.setCategory("手机");
        goods.setImage("www.baidu.com");
        goods.setName("小米max");
        goods.setTitle("第三方更符合法规和规范化规范化股份");
        IndexQuery indexQuery = new IndexQueryBuilder().withIndexName("goods_ku")
                .withType("goods").withId("1").withObject(goods).build();
//        template.index(indexQuery);
//        goodsReposity.save(goods);
    }
    @Test
    public void testAdd() {


//            template.createIndex(Goods.class);
//        goodsReposity.save()
    }

//    @Test
//    public void deleteIndex(){
//        //创建查询构建器
//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//        //查询条件
//        queryBuilder.withQuery(QueryBuilders.matchAllQuery());
//        SearchQuery build = queryBuilder.withPageable(PageRequest.of(0, 10)).build();
//        Page<Goods> goods = template.queryForPage(build, Goods.class);
//        List<Goods> content = goods.getContent();
//        for (Goods goods1 : content) {
//            System.out.println("goods1 = " + goods1);
//        }
//    }
//
//
//
//
//    @Test
//    public void testCreate() {
//        template.createIndex(Item.class);
//        template.putMapping(Item.class);
//
//    }
//
//    @Test
//    public void indexList() {
//        List<Item> list = new ArrayList<Item>();
//        list.add(new Item(2L, "坚果手机R1", " 手机", "锤子", 3699.00, "http://image.leyou.com/123.jpg"));
//        list.add(new Item(3L, "华为META11", " 手机", "华为1", 4499.00, "http://image.leyou.com/31.jpg"));
//        list.add(new Item(3L, "华为5META11", " 手机", "华为5", 4499.00, "http://image.leyou.com/31.jpg"));
//        list.add(new Item(4L, "苹果手机A12", " 手机", "华为2", 4498.00, "http://image.leyou.com/32.jpg"));
//        list.add(new Item(5L, "三星手机B13", " 手机", "华为3", 4496.00, "http://image.leyou.com/33.jpg"));
//        list.add(new Item(6L, "小米手机8", " 手机", "华为4", 4495.00, "http://image.leyou.com/34.jpg"));
//        list.add(new Item(7L, "小米手机8", " 手机", "小米", 4495.00, "http://image.leyou.com/34.jpg"));
//        // 接收对象集合，实现批量新增
//        itemRepository.saveAll(list);
//    }
//    @Test
//    public void teatFind() {
//        Iterable<Item> all = itemRepository.findAll();
//        for (Item item : all) {
//            System.out.println(item);
//        }
//    }
//    @Test
//    public void teatFindBy() {
//        List<Item> all = itemRepository.findByPriceBetween(3699d,4496d);
//        for (Item item : all) {
//            System.out.println(item);
//        }
//    }
//    @Test
//    public void teatFindByPage() {
//        //page总是从0开始，表示查询页，size指每页的期望行数。
//        Pageable page = PageRequest.of(0, 2);
//        List<Item> all = itemRepository.findByPriceBetween(3699d,4496d,page);
//        for (Item item : all) {
//            System.out.println(item);
//        }
//    }
//
//    @Test
//    public void teatQuery() {
//        //创建查询构建器
//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//        //结果过滤
//        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","price","title"}, null));
//        //查询条件
//        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "小米手机"));
//        //QueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "小米手机");
//        //排序
//        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
//        //分页
//        queryBuilder.withPageable(PageRequest.of(1,2));
//
//        Page<Item> page = itemRepository.search(queryBuilder.build());
//        long total = page.getTotalElements();
//        System.out.println("total = " + total);
//        int totalPages = page.getTotalPages();
//        System.out.println("totalPages = " + totalPages);
//        List<Item> content = page.getContent();
//        for (Item item : content) {
//            System.out.println(item);
//        }
//    }
//    //聚合
//    @Test
//    public void testAgg(){
//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//        String aggName = "popularBrand";
//        //聚合
//        queryBuilder.addAggregation(AggregationBuilders.terms(aggName).field("brand"));
//
//        //查询并返回聚合结果
//        AggregatedPage<Item> result = template.queryForPage(queryBuilder.build(), Item.class);
//
//        //解析聚合
//        Aggregations aggs = result.getAggregations();
//
//        //获取指定名称的聚合
//        StringTerms terms = aggs.get(aggName);
//
//        //获取桶
//        List<StringTerms.Bucket> buckets = terms.getBuckets();
//
//        for (StringTerms.Bucket bucket : buckets) {
//            System.out.println("Key = " + bucket.getKeyAsString());
//            System.out.println("DocCount = " + bucket.getDocCount());
//        }
//
//    }


    /**
     * 拼接在某属性的 set方法
     *
     * @param fieldName
     * @return String
     */
    private static String parSetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        int startIndex = 0;
        if (fieldName.charAt(0) == '_')
            startIndex = 1;
        return "set" + fieldName.substring(startIndex, startIndex + 1).toUpperCase()
                + fieldName.substring(startIndex + 1);
    }

    @Test
    public void testJson() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("小米");
        String s = JSONObject.toJSONString(Collections.singletonList(item), SerializerFeature.WriteNullStringAsEmpty);
        System.out.println(s);
    }
}
