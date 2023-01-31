package com.asyncexport.boot.entity;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageQuery<T> {
    protected long page = 1L;
    protected long size = 10L;
    protected String keyword;
    protected Map<String, Boolean> orders = new LinkedHashMap();
    protected T param;

    public Page convertPage() {
        Page pageRequest = new Page();
        pageRequest.setCurrent(this.page);
        pageRequest.setSize(this.size);
        if (!CollectionUtils.isEmpty(this.orders)) {
            this.orders.forEach((key, value) -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setColumn(StringUtils.camelToUnderline(key));
                orderItem.setAsc(value);
                pageRequest.addOrder(new OrderItem[]{orderItem});
            });
        }

        return pageRequest;
    }

    public PageQuery() {
    }

    public long getPage() {
        return this.page;
    }

    public long getSize() {
        return this.size;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public Map<String, Boolean> getOrders() {
        return this.orders;
    }

    public T getParam() {
        return this.param;
    }

    public void setPage(final long page) {
        this.page = page;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public void setKeyword(final String keyword) {
        this.keyword = keyword;
    }

    public void setOrders(final Map<String, Boolean> orders) {
        this.orders = orders;
    }

    public void setParam(final T param) {
        this.param = param;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof PageQuery)) {
            return false;
        } else {
            PageQuery<?> other = (PageQuery)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.getPage() != other.getPage()) {
                return false;
            } else if (this.getSize() != other.getSize()) {
                return false;
            } else {
                label52: {
                    Object this$keyword = this.getKeyword();
                    Object other$keyword = other.getKeyword();
                    if (this$keyword == null) {
                        if (other$keyword == null) {
                            break label52;
                        }
                    } else if (this$keyword.equals(other$keyword)) {
                        break label52;
                    }

                    return false;
                }

                Object this$orders = this.getOrders();
                Object other$orders = other.getOrders();
                if (this$orders == null) {
                    if (other$orders != null) {
                        return false;
                    }
                } else if (!this$orders.equals(other$orders)) {
                    return false;
                }

                Object this$param = this.getParam();
                Object other$param = other.getParam();
                if (this$param == null) {
                    if (other$param != null) {
                        return false;
                    }
                } else if (!this$param.equals(other$param)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PageQuery;
    }

    public int hashCode() {
        int result = 1;
        long $page = this.getPage();
        result = result * 59 + (int)($page >>> 32 ^ $page);
        long $size = this.getSize();
        result = result * 59 + (int)($size >>> 32 ^ $size);
        Object $keyword = this.getKeyword();
        result = result * 59 + ($keyword == null ? 43 : $keyword.hashCode());
        Object $orders = this.getOrders();
        result = result * 59 + ($orders == null ? 43 : $orders.hashCode());
        Object $param = this.getParam();
        result = result * 59 + ($param == null ? 43 : $param.hashCode());
        return result;
    }

    public String toString() {
        return "PageQuery(page=" + this.getPage() + ", size=" + this.getSize() + ", keyword=" + this.getKeyword() + ", orders=" + this.getOrders() + ", param=" + this.getParam() + ")";
    }
}
