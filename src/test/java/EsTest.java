import com.alibaba.fastjson.JSON;
import com.gx.pojo.Item;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-mvc.xml")
public class EsTest {
    @Autowired
    RestHighLevelClient client;

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
            SearchRequest request = new SearchRequest("linayi");
            request.types("item");
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
}
