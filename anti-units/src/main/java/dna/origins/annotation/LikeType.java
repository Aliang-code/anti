package dna.origins.annotation;

public enum LikeType {
    //默认无通配符
    NONE,
    //左%通配符
    LEFT_PERCENT,
    //右%通配符
    RIGHT__PERCENT,
    //双%通配符
    BOTH_PERCENT;

    LikeType() {
    }
}
