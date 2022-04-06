package xyz.suchdoge.webapi.dto.meme.filter;

public enum MemeOrderFilter {
    NEWEST, // all post, first newest
    OLDEST, // all post, first oldest
    LATEST_TIPPED, // all post, first with most recent donations
    MOST_TIPPED, // all post, first with most donations
    TOP_TIPPED_LAST_3_DAYS, // only with donation last 3 days, first with most donations
    TOP_TIPPED_LAST_WEEK, // only with donation last 7 days, first with most donations
    TOP_TIPPED_LAST_MONTH // only with donation last 30 days, first with most donations
}
