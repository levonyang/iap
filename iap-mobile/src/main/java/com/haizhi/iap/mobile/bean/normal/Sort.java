package com.haizhi.iap.mobile.bean.normal;

import com.haizhi.iap.mobile.enums.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class Sort
{
    private String field;
    private Direction direction = Direction.ASC;
}