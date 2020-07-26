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


    public void setApkLink(String apkLink) {
        this.apkLink = apkLink;
    }

    public void setAudit(int audit) {
        this.audit = audit;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public void setCollect(boolean collect) {
        this.collect = collect;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setEnvelopePic(String envelopePic) {
        this.envelopePic = envelopePic;
    }

    public void setFresh(boolean fresh) {
        this.fresh = fresh;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setNiceDate(String niceDate) {
        this.niceDate = niceDate;
    }

    public void setNiceShareDate(String niceShareDate) {
        this.niceShareDate = niceShareDate;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setProjectLink(String projectLink) {
        this.projectLink = projectLink;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public void setShareDate(long shareDate) {
        this.shareDate = shareDate;
    }

    public void setShareUser(String shareUser) {
        this.shareUser = shareUser;
    }

    public void setSuperChapterId(int superChapterId) {
        this.superChapterId = superChapterId;
    }

    public void setSuperChapterName(String superChapterName) {
        this.superChapterName = superChapterName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public void setZan(int zan) {
        this.zan = zan;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Article article = (Article) o;

        return id == article.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
