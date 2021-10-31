package com.kanok.inserter.model;

public class UserCount {

    private final long userId;
    private final int topicCount;
    private final int postCount;
    private final int articleCount;

    public UserCount(long userId, int topicCount, int postCount, int articleCount) {
        this.userId = userId;
        this.topicCount = topicCount;
        this.postCount = postCount;
        this.articleCount = articleCount;
    }

    public long getUserId() {
        return userId;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public int getArticleCount() {
        return articleCount;
    }
}

