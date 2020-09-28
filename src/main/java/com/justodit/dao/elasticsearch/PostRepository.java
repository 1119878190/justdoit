package com.justodit.dao.elasticsearch;

import com.justodit.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

//等同于配置数据库的数据层，可以使用CrudRepository的子类 ElasticsearchCrudRepository实现更多复杂查询
@Repository
public interface PostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

}
