package com.whatsoeversky.minder.sys.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sys_dict_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysDictItem {
    @Id
    private String id;
    private String type;
    private String key;
    private String value;
    private Integer sort;
    private Boolean enabled;
}
