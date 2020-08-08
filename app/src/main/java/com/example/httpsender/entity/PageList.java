package com.example.httpsender.entity;


import java.util.ArrayList;
import java.util.List;

/**
 * User: ljx
 * Date: 2018/10/21
 * Time: 13:16
 */
public class PageList<T> {

    private int     curPage; //当前页数
    private int     pageCount; //总页数
    private int     total; //总条数
    private List<T> datas;

    public int getCurPage() {
        return curPage;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getTotal() {
        return total;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setDatas(ArrayList<T> datas) {
        this.datas = datas;
    }
}
