
package com.justodit;


import com.justodit.dao.DiscussPostMapper;


import com.justodit.dao.elasticsearch.PostRepository;
import com.justodit.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class ElasticsearchTest {


    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    //单条数据插入
    @Test
    public void testInsert(){
        postRepository.save(discussMapper.selectDiscussPostById(241));
        postRepository.save(discussMapper.selectDiscussPostById(242));
        postRepository.save(discussMapper.selectDiscussPostById(243));

    }

    //多条数据插入
    @Test
    public void testInsertList(){
        postRepository.saveAll(discussMapper.selectDiscussPosts(101,0,100,0));
        postRepository.saveAll(discussMapper.selectDiscussPosts(103,0,100,0));
        postRepository.saveAll(discussMapper.selectDiscussPosts(102,0,100,0));
        postRepository.saveAll(discussMapper.selectDiscussPosts(111,0,100,0));
        postRepository.saveAll(discussMapper.selectDiscussPosts(112,0,100,0));
        postRepository.saveAll(discussMapper.selectDiscussPosts(131,0,100,0));
        postRepository.saveAll(discussMapper.selectDiscussPosts(132,0,100,0));
        postRepository.saveAll(discussMapper.selectDiscussPosts(133,0,100,0));
        postRepository.saveAll(discussMapper.selectDiscussPosts(134,0,100,0));


    }

    //修改数据
    @Test
    public void testUpdate(){
        DiscussPost post = discussMapper.selectDiscussPostById(231);
        post.setContent("我是新人,使劲灌水");
        postRepository.save(post);
    }

    //删除数据
    @Test
    public void testDelete(){
        //postRepository.deleteById(231);
        postRepository.deleteAll();
    }

    /**
     * 搜索  通过Repository搜索
     */
    @Test
    public void searchByRepository(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))//查询条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))//排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))//分页
                .withHighlightFields(
                        //高亮显示
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        //elasticTemplate.queryForPage(searchQuery,class,SearchResultMapper)
        //直接用repository会出现底层获取到了高亮显示的值,但是没有返回值,可以使用elasticTemplate解决

        Page<DiscussPost> page= postRepository.search(searchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost discussPost : page) {
            System.out.println(discussPost);
        }
    }

    /**
     * 搜索  通过Template搜索
     */
    @Test
    public void searchByTemplate(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))//查询条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))//排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))//分页
                .withHighlightFields(
                        //高亮显示
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        Page<DiscussPost> page = elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                SearchHits hits = response.getHits();
                //为空
                if (hits.getTotalHits() <=0){
                    return null;
                }
                //不为空
                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits){
                    //从中取值为String类型,然后转换类型
                    DiscussPost post = new DiscussPost();
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();//获取到的title为原始的title,没有高亮显示
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    //处理高亮显示结果  将原先获取的title和content覆盖掉
                    HighlightField titleField  = hit.getHighlightFields().get("title");
                    if (titleField != null){
                        post.setTitle(titleField.getFragments()[0].toString());
                    }
                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null){
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);
                }
                return new AggregatedPageImpl(list,pageable,hits.getTotalHits(),
                                response.getAggregations(),response.getScrollId(),hits.getMaxScore());
            }
        });
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost discussPost : page) {
            System.out.println(discussPost);
        }
    }
}

