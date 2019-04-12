package com.gx.repository;

import com.gx.pojo.Goods;
import com.gx.pojo.Item;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsReposity extends ElasticsearchRepository<Goods,Long> {


}
