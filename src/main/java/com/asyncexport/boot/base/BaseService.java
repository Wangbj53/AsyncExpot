package com.asyncexport.boot.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public abstract class BaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {
    public BaseService() {
    }
}

