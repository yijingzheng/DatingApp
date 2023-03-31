package model;

public class MatchStats {

    private Integer numLikes;
    private Integer numDislikes;

    public MatchStats(Integer numLikes, Integer numDislikes) {
        this.numLikes = numLikes;
        this.numDislikes = numDislikes;
    }

    public Integer getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(Integer numLikes) {
        this.numLikes = numLikes;
    }

    public Integer getNumDislikes() {
        return numDislikes;
    }

    public void setNumDislikes(Integer numDislikes) {
        this.numDislikes = numDislikes;
    }
}
