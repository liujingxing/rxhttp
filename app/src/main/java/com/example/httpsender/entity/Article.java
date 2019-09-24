package com.example.httpsender.entity;

/**
 * 文章实体类
 * User: ljx
 * Date: 2019-09-24
 * Time: 10:23
 */
public class Article {


    /**
     * apkLink :
     * audit : 1
     * author :
     * chapterId : 76
     * chapterName : 项目架构
     * collect : false
     * courseId : 13
     * desc :
     * envelopePic :
     * fresh : true
     * id : 9300
     * link : https://mp.weixin.qq.com/s/_6p6vfce7m5E8AwGDg2cZg
     * niceDate : 10小时前
     * niceShareDate : 11小时前
     * origin :
     * prefix :
     * projectLink :
     * publishTime : 1569255115000
     * shareDate : 1569249942000
     * shareUser : ZYLAB
     * superChapterId : 74
     * superChapterName : 热门专题
     * tags : []
     * title : Android 开发中的架构模式 -- MVC / MVP / MVVM
     * type : 0
     * userId : 10577
     * visible : 1
     * zan : 0
     */

    private String apkLink;
    private int     audit;
    private String  author;
    private int     chapterId;
    private String  chapterName;
    private boolean collect;
    private int     courseId;
    private String  desc;
    private String  envelopePic;
    private boolean fresh;
    private int     id;
    private String  link;
    private String  niceDate;
    private String  niceShareDate;
    private String  origin;
    private String  prefix;
    private String  projectLink;
    private long    publishTime;
    private long    shareDate;
    private String  shareUser;
    private int     superChapterId;
    private String  superChapterName;
    private String  title;
    private int     type;
    private int     userId;
    private int     visible;
    private int     zan;

    public String getApkLink() {
        return apkLink;
    }

    public int getAudit() {
        return audit;
    }

    public String getAuthor() {
        return author;
    }

    public int getChapterId() {
        return chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public boolean isCollect() {
        return collect;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getDesc() {
        return desc;
    }

    public String getEnvelopePic() {
        return envelopePic;
    }

    public boolean isFresh() {
        return fresh;
    }

    public int getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getNiceDate() {
        return niceDate;
    }

    public String getNiceShareDate() {
        return niceShareDate;
    }

    public String getOrigin() {
        return origin;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getProjectLink() {
        return projectLink;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public long getShareDate() {
        return shareDate;
    }

    public String getShareUser() {
        return shareUser;
    }

    public int getSuperChapterId() {
        return superChapterId;
    }

    public String getSuperChapterName() {
        return superChapterName;
    }

    public String getTitle() {
        return title;
    }

    public int getType() {
        return type;
    }

    public int getUserId() {
        return userId;
    }

    public int getVisible() {
        return visible;
    }

    public int getZan() {
        return zan;
    }
}
