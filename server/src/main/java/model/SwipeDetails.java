package model;

public class SwipeDetails {
    private String swiper;
    private String swipee;
    private String comment;

    private final static int MINIMUM = 1;
    private final static int SWIPER_MAX = 5000;
    private final static int SWIPEE_MAX = 1000000;
    private final static int COMMENT_MAX = 256;


    public SwipeDetails(String swiper, String swipee, String comment) {
        this.swiper = swiper;
        this.swipee = swipee;
        this.comment = comment;
    }

    public boolean isValid() {
        if (this.swiper == null || this.swipee == null) return false;
        if (!isBetween(this.swiper, MINIMUM, SWIPER_MAX)) return false;
        if (!isBetween(this.swipee, MINIMUM, SWIPEE_MAX)) return false;
        if (this.comment.length() > COMMENT_MAX) return false;
        return true;
    }

    private boolean isBetween(String str, int start, int end) {
        for (char ch : str.toCharArray()) {
            if (!Character.isDigit(ch)) return false;
        }
        int val = Integer.parseInt(str);
        if (val >= start && val <= end) return true;
        return false;
    }

    public String getSwiper() {
        return swiper;
    }

    public void setSwiper(String swiper) {
        this.swiper = swiper;
    }

    public String getSwipee() {
        return swipee;
    }

    public void setSwipee(String swipee) {
        this.swipee = swipee;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
