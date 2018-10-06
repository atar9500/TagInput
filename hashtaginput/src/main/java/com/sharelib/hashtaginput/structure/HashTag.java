package com.sharelib.hashtaginput.structure;

public class HashTag {

    /**
     * Data
     */
    private String mText;
    private long mTimestamp;

    /**
     * HashTag Methods
     */
    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    /**
     * Object Methods
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HashTag)) {
            return false;
        }
        HashTag hashTag = (HashTag) obj;
        return hashTag.mText.equalsIgnoreCase(mText);
    }
}
