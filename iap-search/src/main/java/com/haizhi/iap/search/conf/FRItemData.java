package com.haizhi.iap.search.conf;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by chenbo on 17/2/20.
 */
@Data
@NoArgsConstructor
public class FRItemData {

    String title;

    List<FRItemDataListData> list;

}
