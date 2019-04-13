package com.gx.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(indexName = "goods_ku", type = "goods", shards = 1,replicas = 1)
public class Goods {

    @Field(type = FieldType.Long)
    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Keyword, index = false)
    private String Image;
    @Field(type = FieldType.Keyword)
    private String barnd;
    @Field(type = FieldType.Keyword)
    String category;// 分类
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
}
