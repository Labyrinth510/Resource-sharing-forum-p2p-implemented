package usage;

import java.io.Serializable;

public class ResourceInfo implements Serializable {
    private int f_id, lowestPrestige, pointsRequiredPerUnit;
    private long fileSize;
    private int groupMemberNumber;
    private String filename;
    private String[] tags;

    public ResourceInfo(int f_id, int lowestPrestige, int pointsRequiredPerUnit, long fileSize, String filename, String[] tags, int groupMemberNumber) {
        this.f_id = f_id;
        this.lowestPrestige = lowestPrestige;
        this.pointsRequiredPerUnit = pointsRequiredPerUnit;
        this.fileSize = fileSize;
        this.filename = filename;
        this.tags = tags;
        this.groupMemberNumber = groupMemberNumber;
    }

    public int getF_id() {
        return f_id;
    }

    public void setF_id(int f_id) {
        this.f_id = f_id;
    }

    public int getLowestPrestige() {
        return lowestPrestige;
    }

    public void setLowestPrestige(int lowestPrestige) {
        this.lowestPrestige = lowestPrestige;
    }

    public int getPointsRequiredPerUnit() {
        return pointsRequiredPerUnit;
    }

    public void setPointsRequiredPerUnit(int pointsRequiredPerUnit) {
        this.pointsRequiredPerUnit = pointsRequiredPerUnit;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getGroupMemberNumber() {
        return groupMemberNumber;
    }

    public void setGroupMemberNumber(int groupMemberNumber) {
        this.groupMemberNumber = groupMemberNumber;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}