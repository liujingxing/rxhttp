package com.example.httpsender;


import java.util.List;

/**
 * User: ljx
 * Date: 2018/10/21
 * Time: 13:16
 */
public class PageList<T> {

    private int     totalPage;
    private List<T> list;

    public int getTotalPage() {
        return totalPage;
    }

    public List<T> getList() {
        return list;
    }

}
