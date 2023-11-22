package com.asyncexport.boot.base;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseController<S extends BaseService> {

    @Autowired
    protected S baseService;

    public BaseController() {
    }

}
