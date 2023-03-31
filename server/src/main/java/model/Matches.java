package model;

import java.util.List;

public class Matches {

    private List<String> matchList;

    public Matches(List<String> matchList) {
        this.matchList = matchList;
    }

    public List<String> getMatchList() {
        return matchList;
    }

    public void setMatchList(List<String> matchList) {
        this.matchList = matchList;
    }
}
